package io.github.jpmorganchase.fusion.api.operations;

import static io.github.jpmorganchase.fusion.api.tools.ResponseChecker.checkResponseStatus;

import com.google.gson.GsonBuilder;
import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.FusionException;
import io.github.jpmorganchase.fusion.api.context.MultipartTransferContext;
import io.github.jpmorganchase.fusion.api.context.UploadedPartContext;
import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.exception.ApiInputValidationException;
import io.github.jpmorganchase.fusion.api.exception.FileUploadException;
import io.github.jpmorganchase.fusion.api.request.UploadRequest;
import io.github.jpmorganchase.fusion.api.response.UploadedParts;
import io.github.jpmorganchase.fusion.digest.AlgoSpecificDigestProducer;
import io.github.jpmorganchase.fusion.digest.DigestDescriptor;
import io.github.jpmorganchase.fusion.digest.DigestProducer;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.oauth.exception.OAuthException;
import io.github.jpmorganchase.fusion.oauth.provider.FusionTokenProvider;
import io.github.jpmorganchase.fusion.parsing.APIResponseParser;
import io.github.jpmorganchase.fusion.parsing.GsonAPIResponseParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Builder
@Getter
public class FusionAPIUploadOperations implements APIUploadOperations {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String UPLOAD_FAILED_EXCEPTION_MSG =
            "Exception encountered while attempting to upload part, please try again";

    private static final String INITIATE_MULTIPART_UPLOAD_PATH = "/operationType/upload";

    private static final String PART_UPLOAD_PATH = "%s/operations/upload?operationId=%s&partNumber=%d";

    private static final String FINALISE_MULTIPART_UPLOAD_PATH = "%s/operations/upload?operationId=%s";

    private final Client httpClient;

    private final FusionTokenProvider fusionTokenProvider;

    private final DigestProducer digestProducer;

    @Builder.Default
    private final APIResponseParser responseParser = new GsonAPIResponseParser();

    /**
     * Max size in MB of data allowed for a single part upload.
     * if 32MB was the max size then 32 would be provided.
     * <p>
     * See {@link FusionConfiguration} for default values.
     */
    int singlePartUploadSizeLimit;

    /**
     * Upload part chunk size. Defaults to 16MB.
     * If a value such as 8MB is required, then client would set this value to 8
     * See {@link FusionConfiguration} for default values.
     */
    int uploadPartSize;

    /**
     * Size of Thread-Pool to be used for uploading chunks of a multipart file
     * See {@link FusionConfiguration} for default values.
     */
    int uploadThreadPoolSize;

    /**
     * Max size of in-flux data that can be read at a given time.
     * See {@link FusionConfiguration} for default values.
     */
    long maxInFluxDataSize;

    /**
     * Call the API upload endpoint to load a distribution
     *
     * @param apiPath     the API URL
     * @param fileName    the path to the distribution on the local filesystem
     * @param fromDate    the earliest date that data is contained in the upload (in form yyyy-MM-dd).
     * @param toDate      the latest date that data is contained in the upload (in form yyyy-MM-dd).
     * @param createdDate the creation date for the data is contained in the upload (in form yyyy-MM-dd).
     * @param headers      http headers to be provided in the request.  For the headers with multiple instances, the value should be a comm-separated list
     * @throws ApiInputValidationException if the specified file cannot be read
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileUploadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    @Override
    public void callAPIFileUpload(
            String apiPath,
            String fileName,
            String catalogName,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate,
            Map<String, String> headers)
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
                .headers(headers)
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
     * @param headers      http headers to be provided in the request.  For the headers with multiple instances, the value should be a comm-separated list
     * @throws ApiInputValidationException if the specified file cannot be read
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileUploadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    @Override
    public void callAPIFileUpload(
            String apiPath,
            InputStream data,
            String catalogName,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate,
            Map<String, String> headers)
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
                .headers(headers)
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

        Map<String, String> requestHeaders = ur.getHeaders();
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

        Map<String, String> requestHeaders = ur.getHeaders();
        requestHeaders.put("accept", "*/*");
        setSecurityHeaders(ur, requestHeaders);

        HttpResponse<String> startResponse = httpClient.post(startUploadPath, requestHeaders, null);

        checkResponseStatus(startResponse);

        return MultipartTransferContext.started(responseParser.parseOperationResponse(startResponse.getBody()));
    }

    protected MultipartTransferContext callAPIToUploadParts(MultipartTransferContext mtx, UploadRequest ur) {

        int chunkSize = uploadPartSize * (1024 * 1024);
        long maxInFluxBytes = maxInFluxDataSize * (1024L * 1024L);

        byte[] buffer = new byte[chunkSize];
        int partCnt = 1;
        int totalBytes = 0;
        int inFluxBytes = 0;

        ExecutorService executor = Executors.newFixedThreadPool(uploadThreadPoolSize);
        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            int bytesRead;
            while ((bytesRead = ur.getData().read(buffer)) != -1) {

                logger.debug(
                        "Creating upload task for part number {}, bytes read for this part {}", partCnt, bytesRead);

                final int currentPartCnt = partCnt;
                final int currentBytesRead = bytesRead;
                byte[] taskBuffer = Arrays.copyOf(buffer, bytesRead);

                if (inFluxBytes > maxInFluxBytes) {
                    inFluxBytes = easeDataPressure(futures);
                }

                futures.add(CompletableFuture.runAsync(
                        () -> mtx.partUploaded(
                                callAPIToUploadPart(mtx, ur, taskBuffer, currentBytesRead, currentPartCnt)),
                        executor));

                partCnt++;
                totalBytes += bytesRead;
                inFluxBytes += bytesRead;
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

    private int easeDataPressure(List<CompletableFuture<Void>> futures)
            throws InterruptedException, ExecutionException {

        logger.debug("Reached max in-flux bytes - easing pressure");
        for (CompletableFuture<Void> future : futures) {
            future.get();
        }
        logger.debug("Max in-flux bytes handled - pressure eased");
        futures.clear();
        return 0;
    }

    protected UploadedPartContext callAPIToUploadPart(
            MultipartTransferContext mtx, UploadRequest ur, byte[] part, int read, int partNo) {

        String partTransferPath = String.format(
                PART_UPLOAD_PATH, ur.getApiPath(), mtx.getOperation().getOperationId(), partNo);

        DigestDescriptor digestOfPart = digestProducer.execute(
                new ByteArrayInputStream(ByteBuffer.wrap(part, 0, read).array()));

        Map<String, String> requestHeaders = ur.getHeaders();
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

        Map<String, String> requestHeaders = ur.getHeaders();
        requestHeaders.put("Content-Type", "application/json");
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

        Map<String, String> requestHeaders = ur.getHeaders();
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
        return new FileUploadException(UPLOAD_FAILED_EXCEPTION_MSG, cause);
    }

    private void setSecurityHeaders(UploadRequest ur, Map<String, String> requestHeaders) {
        requestHeaders.put("Authorization", "Bearer " + fusionTokenProvider.getSessionBearerToken());
        requestHeaders.put(
                "Fusion-Authorization",
                "Bearer " + fusionTokenProvider.getDatasetBearerToken(ur.getCatalog(), ur.getDataset()));
    }

    private void setDistributionHeaders(UploadRequest ur, DigestDescriptor digest, Map<String, String> requestHeaders) {
        requestHeaders.put("x-jpmc-distribution-from-date", ur.getFromDate());
        requestHeaders.put("x-jpmc-distribution-to-date", ur.getToDate());
        requestHeaders.put("x-jpmc-distribution-created-date", ur.getCreatedDate());
        requestHeaders.put("Digest", "SHA-256=" + digest.getChecksum());
    }

    private String serializeToJson(UploadedParts parts) {
        return new GsonBuilder().create().toJson(parts);
    }

    public static FusionAPIUploadOperationsBuilder builder() {
        return new CustomFusionAPIUploadOperationsBuilder();
    }

    public static class FusionAPIUploadOperationsBuilder {

        protected FusionConfiguration configuration =
                FusionConfiguration.builder().build();
        protected Client httpClient;
        protected FusionTokenProvider fusionTokenProvider;
        protected DigestProducer digestProducer;
        protected APIResponseParser responseParser;
        int singlePartUploadSizeLimit;
        int uploadPartSize;
        int uploadThreadPoolSize;
        long maxInFluxDataSize;

        public FusionAPIUploadOperationsBuilder configuration(FusionConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        @SuppressWarnings("PIT")
        private FusionAPIUploadOperationsBuilder singlePartUploadSizeLimit(int singlePartUploadSizeLimit) {
            this.singlePartUploadSizeLimit = singlePartUploadSizeLimit;
            return this;
        }

        @SuppressWarnings("PIT")
        private FusionAPIUploadOperationsBuilder uploadPartSize(int uploadPartSize) {
            this.uploadPartSize = uploadPartSize;
            return this;
        }

        @SuppressWarnings("PIT")
        private FusionAPIUploadOperationsBuilder uploadThreadPoolSize(int uploadThreadPoolSize) {
            this.uploadThreadPoolSize = uploadThreadPoolSize;
            return this;
        }

        @SuppressWarnings("PIT")
        private FusionAPIUploadOperationsBuilder maxInFluxDataSize(long maxInFluxDataSize) {
            this.maxInFluxDataSize = maxInFluxDataSize;
            return this;
        }
    }

    private static class CustomFusionAPIUploadOperationsBuilder extends FusionAPIUploadOperationsBuilder {
        @Override
        public FusionAPIUploadOperations build() {
            this.singlePartUploadSizeLimit = configuration.getSinglePartUploadSizeLimit();
            this.uploadPartSize = configuration.getUploadPartSize();
            this.uploadThreadPoolSize = configuration.getUploadThreadPoolSize();
            this.maxInFluxDataSize = configuration.getMaxInFluxDataSize();

            if (Objects.isNull(digestProducer)) {
                this.digestProducer = AlgoSpecificDigestProducer.builder()
                        .digestAlgorithm(configuration.getDigestAlgorithm())
                        .build();
            }

            return super.build();
        }
    }
}
