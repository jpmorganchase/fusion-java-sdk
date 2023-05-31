package io.github.jpmorganchase.fusion.api;

import com.google.gson.GsonBuilder;
import io.github.jpmorganchase.fusion.api.context.MultipartTransferContext;
import io.github.jpmorganchase.fusion.api.context.UploadedPartContext;
import io.github.jpmorganchase.fusion.api.request.UploadRequest;
import io.github.jpmorganchase.fusion.api.response.UploadedParts;
import io.github.jpmorganchase.fusion.digest.DigestDescriptor;
import io.github.jpmorganchase.fusion.digest.DigestProducer;
import io.github.jpmorganchase.fusion.exception.FusionException;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.oauth.exception.OAuthException;
import io.github.jpmorganchase.fusion.oauth.provider.DatasetTokenProvider;
import io.github.jpmorganchase.fusion.oauth.provider.SessionTokenProvider;
import io.github.jpmorganchase.fusion.parsing.APIResponseParser;
import io.github.jpmorganchase.fusion.parsing.GsonAPIResponseParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FusionAPIUploader implements APIUploader {

    private static final String UPLOAD_FAILED_EXCEPTION_MSG =
            "Exception encountered while attempting to upload part, please try again";

    private static final String INITIATE_MULTIPART_UPLOAD_PATH = "/operationType/upload";

    private static final String PART_UPLOAD_PATH = "%s/operations/upload?operationId=%s&partNumber=%d";

    private static final String FINALISE_MULTIPART_UPLOAD_PATH = "%s/operations/upload?operationId=%s";

    private final Client httpClient;

    private final SessionTokenProvider sessionTokenProvider;

    private final DatasetTokenProvider datasetTokenProvider;

    private final DigestProducer digestProducer;

    @Builder.Default
    private final APIResponseParser responseParser = new GsonAPIResponseParser();

    /**
     * Max size in MB of data allowed for a single part upload.
     * if 32MB was the max size then 32 would be provided.
     * <p>
     * Defaults to 50MB aka 50.
     */
    @Builder.Default
    int singlePartUploadSizeLimit = 50;

    /**
     * Upload part chunk size. Defaults to 16MB.
     * If a value such as 8MB is required, then client would set this value to 8
     */
    @Builder.Default
    int uploadPartSize = 8;

    /**
     * Size of Thread-Pool to be used for uploading chunks of a multipart file
     * Defaults to number of available processors.
     */
    @Builder.Default
    int uploadThreadPoolSize = Runtime.getRuntime().availableProcessors();

    /**
     * Call the API upload endpoint to load a distribution
     *
     * @param apiPath     the API URL
     * @param fileName    the path to the distribution on the local filesystem
     * @param fromDate    the earliest date that data is contained in the upload (in form yyyy-MM-dd).
     * @param toDate      the latest date that data is contained in the upload (in form yyyy-MM-dd).
     * @param createdDate the creation date for the data is contained in the upload (in form yyyy-MM-dd).
     */
    @Override
    public void callAPIFileUpload(
            String apiPath,
            String fileName,
            String catalogName,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate)
            throws APICallException {

        callAPIFileUpload(UploadRequest.builder()
                .fromFile(fileName)
                .apiPath(apiPath)
                .catalog(catalogName)
                .dataset(dataset)
                .fromDate(fromDate)
                .toDate(toDate)
                .createdDate(createdDate)
                .maxSinglePartFileSize(singlePartUploadSizeLimit)
                .build());
    }

    /**
     * Call the API upload endpoint to load a distribution
     *
     * @param apiPath     the API URL
     * @param data        InputStream for the data to be uploaded
     * @param fromDate    the earliest date that data is contained in the upload (in form yyyy-MM-dd).
     * @param toDate      the latest date that data is contained in the upload (in form yyyy-MM-dd).
     * @param createdDate the creation date for the data is contained in the upload (in form yyyy-MM-dd).
     */
    @Override
    public void callAPIFileUpload(
            String apiPath,
            InputStream data,
            String catalogName,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate)
            throws APICallException {

        callAPIFileUpload(UploadRequest.builder()
                .fromStream(data)
                .apiPath(apiPath)
                .catalog(catalogName)
                .dataset(dataset)
                .fromDate(fromDate)
                .toDate(toDate)
                .createdDate(createdDate)
                .maxSinglePartFileSize(singlePartUploadSizeLimit)
                .build());
    }

    protected void callAPIFileUpload(UploadRequest uploadRequest) {
        if (uploadRequest.isMultiPartUploadCandidate()) {
            performMultiPartUpload(uploadRequest);
        } else {
            performSinglePartUpload(uploadRequest);
        }
    }

    protected void performSinglePartUpload(UploadRequest ur) {

        DigestDescriptor upload = digestProducer.execute(ur.getData());

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("accept", "*/*");
        requestHeaders.put("Content-Type", "application/octet-stream");
        requestHeaders.put("Content-Length", String.valueOf(upload.getSize()));
        setSecurityHeaders(ur, requestHeaders);
        setDistributionHeaders(ur, upload, requestHeaders);

        HttpResponse<String> response =
                httpClient.put(ur.getApiPath(), requestHeaders, new ByteArrayInputStream(upload.getContent()));

        checkResponseStatus(response);
    }

    protected void performMultiPartUpload(UploadRequest ur) {

        MultipartTransferContext mtx = callAPIToInitiateMultiPartUpload(ur);
        try {
            if (mtx.canProceedToTransfer()) {
                mtx = callAPIToUploadParts(mtx, ur);
                if (mtx.canProceedToComplete()) {
                    callAPIToCompleteMultiPartUpload(mtx, ur);
                }
            }
        } catch (ApiInputValidationException | APICallException | OAuthException e) {
            callAPIToAbortMultiPartUpload(mtx, ur);
            throw e;
        }
    }

    protected MultipartTransferContext callAPIToInitiateMultiPartUpload(UploadRequest ur) {
        String startUploadPath = ur.getApiPath() + INITIATE_MULTIPART_UPLOAD_PATH;

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("accept", "*/*");
        setSecurityHeaders(ur, requestHeaders);

        HttpResponse<String> startResponse = httpClient.post(startUploadPath, requestHeaders, null);

        checkResponseStatus(startResponse);

        return MultipartTransferContext.started(responseParser.parseOperationResponse(startResponse.getBody()));
    }

    protected MultipartTransferContext callAPIToUploadParts(MultipartTransferContext mtx, UploadRequest ur) {

        int chunkSize = uploadPartSize * (1024 * 1024);

        byte[] buffer = new byte[chunkSize];
        int partCnt = 1;
        int totalBytes = 0;

        ExecutorService executor = Executors.newFixedThreadPool(uploadThreadPoolSize);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            int bytesRead;
            while ((bytesRead = ur.getData().read(buffer)) != -1) {
                final int currentPartCnt = partCnt;
                final int currentBytesRead = bytesRead;
                byte[] taskBuffer = Arrays.copyOf(buffer, bytesRead);

                futures.add(CompletableFuture.runAsync(
                        () -> mtx.partUploaded(
                                callAPIToUploadPart(mtx, ur, taskBuffer, currentBytesRead, currentPartCnt)),
                        executor));

                partCnt++;
                totalBytes += bytesRead;
            }

            for (CompletableFuture<Void> future : futures) {
                future.get();
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            throw handleExceptionThrownWhenAttemptingToUploadParts(e);
        } finally {
            executor.shutdown();
        }

        return mtx.transferred(chunkSize, totalBytes, partCnt);
    }

    protected UploadedPartContext callAPIToUploadPart(
            MultipartTransferContext mtx, UploadRequest ur, byte[] part, int read, int partNo) {

        String partTransferPath = String.format(
                PART_UPLOAD_PATH, ur.getApiPath(), mtx.getOperation().getOperationId(), partNo);

        DigestDescriptor digestOfPart = digestProducer.execute(
                new ByteArrayInputStream(ByteBuffer.wrap(part, 0, read).array()));

        Map<String, String> requestHeaders = new HashMap<>();
        setSecurityHeaders(ur, requestHeaders);
        requestHeaders.put("accept", "*/*");
        requestHeaders.put("Content-Type", "application/octet-stream");
        requestHeaders.put("Digest", "SHA-256=" + digestOfPart.getChecksum());

        HttpResponse<String> partResponse =
                httpClient.put(partTransferPath, requestHeaders, new ByteArrayInputStream(digestOfPart.getContent()));

        checkResponseStatus(partResponse);

        return UploadedPartContext.builder()
                .digest(digestOfPart.getRawChecksum())
                .part(responseParser.parseUploadPartResponse(partResponse.getBody()))
                .partNo(partNo)
                .build();
    }

    protected MultipartTransferContext callAPIToCompleteMultiPartUpload(
            MultipartTransferContext mtx, UploadRequest ur) {

        String completeTransferPath = String.format(
                FINALISE_MULTIPART_UPLOAD_PATH,
                ur.getApiPath(),
                mtx.getOperation().getOperationId());
        DigestDescriptor digestOfDigests = digestProducer.execute(mtx.digests());

        Map<String, String> requestHeaders = new HashMap<>();
        setSecurityHeaders(ur, requestHeaders);
        setDistributionHeaders(ur, digestOfDigests, requestHeaders);

        HttpResponse<String> completeResponse =
                httpClient.post(completeTransferPath, requestHeaders, serializeToJson(mtx.uploadedParts()));

        checkResponseStatus(completeResponse);
        return mtx.completed();
    }

    protected MultipartTransferContext callAPIToAbortMultiPartUpload(MultipartTransferContext mtx, UploadRequest ur) {
        String completeTransferPath = String.format(
                FINALISE_MULTIPART_UPLOAD_PATH,
                ur.getApiPath(),
                mtx.getOperation().getOperationId());

        Map<String, String> requestHeaders = new HashMap<>();
        setSecurityHeaders(ur, requestHeaders);

        HttpResponse<String> completeResponse = httpClient.delete(completeTransferPath, requestHeaders, null);

        checkResponseStatus(completeResponse);
        return mtx.aborted();
    }

    private FusionException handleExceptionThrownWhenAttemptingToUploadParts(Exception ex) {
        if (ex.getCause() instanceof FusionException) {
            return (FusionException) ex.getCause();
        }

        Throwable cause = (null != ex.getCause() ? ex.getCause() : ex);
        return new FileDownloadException(UPLOAD_FAILED_EXCEPTION_MSG, cause);
    }

    private void setSecurityHeaders(UploadRequest ur, Map<String, String> requestHeaders) {
        requestHeaders.put("Authorization", "Bearer " + sessionTokenProvider.getSessionBearerToken());
        requestHeaders.put(
                "Fusion-Authorization",
                "Bearer " + datasetTokenProvider.getDatasetBearerToken(ur.getCatalog(), ur.getDataset()));
    }

    private void setDistributionHeaders(UploadRequest ur, DigestDescriptor digest, Map<String, String> requestHeaders) {
        requestHeaders.put("x-jpmc-distribution-from-date", ur.getFromDate());
        requestHeaders.put("x-jpmc-distribution-to-date", ur.getToDate());
        requestHeaders.put("x-jpmc-distribution-created-date", ur.getCreatedDate());
        requestHeaders.put("Digest", "SHA-256=" + digest.getChecksum());
    }

    private <T> void checkResponseStatus(HttpResponse<T> response) throws APICallException {
        if (response.isError()) {
            throw new APICallException(response.getStatusCode());
        }
    }

    private String serializeToJson(UploadedParts parts) {
        return new GsonBuilder().create().toJson(parts);
    }
}
