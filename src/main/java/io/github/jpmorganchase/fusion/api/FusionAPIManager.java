package io.github.jpmorganchase.fusion.api;

import com.google.gson.GsonBuilder;
import io.github.jpmorganchase.fusion.digest.DigestDescriptor;
import io.github.jpmorganchase.fusion.digest.DigestProducer;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.model.MultipartTransferContext;
import io.github.jpmorganchase.fusion.model.Operation;
import io.github.jpmorganchase.fusion.model.UploadedPart;
import io.github.jpmorganchase.fusion.model.UploadedPartContext;
import io.github.jpmorganchase.fusion.oauth.credential.BearerTokenCredentials;
import io.github.jpmorganchase.fusion.oauth.provider.DatasetTokenProvider;
import io.github.jpmorganchase.fusion.oauth.provider.SessionTokenProvider;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that manages calls to the API. Intended to be called from multithreaded code.
 */
public class FusionAPIManager implements APIManager {

    private static final String DEFAULT_FOLDER = "downloads";
    private static final String PART_UPLOAD_PATH = "%s/operations/upload?operationId=%s&partNumber=%d";
    private static final String COMPLETE_PART_UPLOAD_PATH = "%s/operations/upload?operationId=%s";
    private final SessionTokenProvider sessionTokenProvider;
    private final DatasetTokenProvider datasetTokenProvider;
    private final Client httpClient;
    private final DigestProducer digestProducer;

    public FusionAPIManager(
            Client httpClient,
            SessionTokenProvider sessionTokenProvider,
            DatasetTokenProvider datasetTokenProvider,
            DigestProducer digestProducer) {
        this.sessionTokenProvider = sessionTokenProvider;
        this.datasetTokenProvider = datasetTokenProvider;
        this.httpClient = httpClient;
        this.digestProducer = digestProducer;
    }

    public void updateBearerToken(String token) {
        sessionTokenProvider.updateCredentials(new BearerTokenCredentials(token));
    }

    /**
     * Call the API with the path provided and return the JSON response.
     *
     * @param apiPath appended to the base URL
     */
    @Override
    public String callAPI(String apiPath) throws APICallException {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + sessionTokenProvider.getSessionBearerToken());

        HttpResponse<String> response = httpClient.get(apiPath, requestHeaders);
        checkResponseStatus(response);
        return response.getBody();
    }

    /**
     * Calls the API to retrieve file data and saves to disk
     *
     * @param apiPath        the URL of the API endpoint to call
     * @param downloadFolder the folder to save the download file
     * @param fileName       the filename
     */
    @Override
    public void callAPIFileDownload(String apiPath, String downloadFolder, String fileName)
            throws APICallException, FileDownloadException {

        try (BufferedInputStream input = new BufferedInputStream(callAPIFileDownload(apiPath));
                FileOutputStream fileOutput = new FileOutputStream(fileName)) {

            byte[] buf = new byte[1024];
            int len;
            while ((len = input.read(buf)) != -1) {
                fileOutput.write(buf, 0, len);
            }
        } catch (IOException e) {
            throw new FileDownloadException("Failure downloading file", e);
        }
    }

    /**
     * Calls the API to retrieve file data and saves to disk in the default location
     *
     * @param apiPath  the URL of the API endpoint to call
     * @param fileName the filename to save into the default folder.
     */
    @Override
    public void callAPIFileDownload(String apiPath, String fileName) throws APICallException {
        this.callAPIFileDownload(apiPath, DEFAULT_FOLDER, fileName);
    }

    /**
     * Calls the API to retrieve file data and returns as an InputStream
     *
     * @param apiPath the URL of the API endpoint to call
     */
    @Override
    public InputStream callAPIFileDownload(String apiPath) throws APICallException {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + sessionTokenProvider.getSessionBearerToken());

        HttpResponse<InputStream> response = httpClient.getInputStream(apiPath, requestHeaders);

        checkResponseStatus(response);
        return response.getBody();
    }

    /**
     * Call the API upload endpoint to load a distribution
     *
     * @param apiPath     the API URL
     * @param fileName    the path to the distribution on the local filesystem
     * @param fromDate    the earliest date that data is contained in the upload (in form yyyy-MM-dd).
     * @param toDate      the latest date that data is contained in the upload (in form yyyy-MM-dd).
     * @param createdDate the creation date for the data is contained in the upload (in form yyyy-MM-dd).
     * @return the HTTP status code - will return 200 if successful
     */
    @Override
    public int callAPIFileUpload(
            String apiPath,
            String fileName,
            String catalogName,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate)
            throws APICallException {

        InputStream fileInputStream;
        try {
            fileInputStream = Files.newInputStream(new File(fileName).toPath());
        } catch (IOException e) {
            throw new ApiInputValidationException(
                    String.format("File does not exist at supplied input location: %s", fileName), e);
        }

        return callAPIFileUpload(apiPath, fileInputStream, catalogName, dataset, fromDate, toDate, createdDate);
    }

    /**
     * Call the API upload endpoint to load a distribution
     *
     * @param apiPath     the API URL
     * @param data        InputStream for the data to be uploaded
     * @param fromDate    the earliest date that data is contained in the upload (in form yyyy-MM-dd).
     * @param toDate      the latest date that data is contained in the upload (in form yyyy-MM-dd).
     * @param createdDate the creation date for the data is contained in the upload (in form yyyy-MM-dd).
     * @return the HTTP status code - will return 200 if successful
     */
    // TODO: in the file case we probably dont want to do it like this - just read the file to calculate the digest and
    // then pass down the FileInputStream
    @Override
    public int callAPIFileUpload(
            String apiPath,
            InputStream data,
            String catalogName,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate)
            throws APICallException {

        DigestDescriptor upload = digestProducer.execute(data);

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("accept", "*/*");
        requestHeaders.put("Authorization", "Bearer " + sessionTokenProvider.getSessionBearerToken());
        requestHeaders.put(
                "Fusion-Authorization", "Bearer " + datasetTokenProvider.getDatasetBearerToken(catalogName, dataset));
        requestHeaders.put("Content-Type", "application/octet-stream");
        requestHeaders.put("x-jpmc-distribution-from-date", fromDate);
        requestHeaders.put("x-jpmc-distribution-to-date", toDate);
        requestHeaders.put("x-jpmc-distribution-created-date", createdDate);
        requestHeaders.put("Content-Length", String.valueOf(upload.getSize()));
        requestHeaders.put("Digest", "SHA-256=" + upload.getChecksum());

        HttpResponse<String> response =
                httpClient.put(apiPath, requestHeaders, new ByteArrayInputStream(upload.getContent()));

        checkResponseStatus(response);

        return response.getStatusCode();
    }

    @SneakyThrows
    public int callAPIFileUploadForMultipart(String apiPath,
                                             InputStream data,
                                             String catalogName,
                                             String dataset,
                                             String fromDate,
                                             String toDate,
                                             String createdDate) {


        MultipartTransferContext mtx = callAPIToInitiateMultipartUpload(apiPath);
        if (mtx.canProceedToTransfer()) {
            mtx = callAPIToUploadParts(mtx, apiPath, data);
            if (mtx.canProceedToComplete()){
                mtx = callAPIToCompleteMultipartUpload(mtx, apiPath, catalogName, dataset, fromDate, toDate, createdDate);
            }
        }

        return 200;
    }

    protected MultipartTransferContext callAPIToCompleteMultipartUpload(MultipartTransferContext mtx,
                                                                        String operationId,
                                                                        String apiPath,
                                                                        String catalogName,
                                                                        String dataset,
                                                                        String fromDate,
                                                                        String toDate,
                                                                        String createdDate) {

        String completeTransferPath = String.format(COMPLETE_PART_UPLOAD_PATH, apiPath, operationId);
        DigestDescriptor digestOfDigests = digestProducer.execute(mtx.digests());

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + sessionTokenProvider.getSessionBearerToken());
        requestHeaders.put(
                "Fusion-Authorization", "Bearer " + datasetTokenProvider.getDatasetBearerToken(catalogName, dataset));
        requestHeaders.put("x-jpmc-distribution-from-date", fromDate);
        requestHeaders.put("x-jpmc-distribution-to-date", toDate);
        requestHeaders.put("x-jpmc-distribution-created-date", createdDate);
        requestHeaders.put("Digest", "SHA-256=" + digestOfDigests.getChecksum());

        String body = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create()
                .toJson(mtx.getParts());

        HttpResponse<String> completeResponse = httpClient.post(completeTransferPath, requestHeaders, body);
        if (completeResponse.isError()){
            //TODO : knighto - what do we want to do with this exception
            throw new APICallException(completeResponse.getStatusCode());
        }

        return mtx.competed();
    }

    @SneakyThrows
    protected MultipartTransferContext callAPIToUploadParts(MultipartTransferContext mtx, String apiPath, InputStream data) {
        //TODO : knighto - should we make this configurable
        int chunkSize = 8 * (1024 * 1024);

        byte[] buffer = new byte[chunkSize];
        int read;
        int partCnt = 1;
        int totalBytes = 0;

        while ((read = data.read(buffer)) != -1) {

            //TODO knighto - exception handling; just now this will throw a runtime; what should we do?
            UploadedPartContext partCtx = callAPIToUploadPart(apiPath, mtx.getOperation().getOperationId(), buffer, read, partCnt);
            mtx.partUploaded(partCtx);

            partCnt += 1;
            totalBytes += read;
        }

        return mtx.transferred(chunkSize, totalBytes, partCnt);
    }

    protected UploadedPartContext callAPIToUploadPart(String apiPath, String operationId, byte[] part, int read, int partCnt){

        String partTransferPath = String.format(PART_UPLOAD_PATH, apiPath, operationId, partCnt);
        DigestDescriptor digestOfPart = digestProducer.execute(new ByteArrayInputStream(ByteBuffer.wrap(part, 0, read).array()));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("accept", "*/*");
        requestHeaders.put("Content-Type", "application/octet-stream");
        requestHeaders.put("Authorization", "Bearer " + sessionTokenProvider.getSessionBearerToken());
        requestHeaders.put("Fusion-Authorization", "Bearer " + datasetTokenProvider.getDatasetBearerToken("common", "test-dataset"));
        requestHeaders.put("Digest", "SHA-256="+digestOfPart.getChecksum());

        HttpResponse<String> partResponse = httpClient.put(partTransferPath, requestHeaders, new ByteArrayInputStream(digestOfPart.getContent()));
        if (partResponse.isError()){
            //TODO : knighto; this is going to fail the entire upload; should we try this part again
            throw new APICallException(partResponse.getStatusCode());
        }

        return UploadedPartContext.builder()
                .digest(digestOfPart.getRawChecksum())
                .parts(new GsonBuilder().create().fromJson(partResponse.getBody(), UploadedPart.class))
                .build();
    }


    protected MultipartTransferContext callAPIToInitiateMultipartUpload(String apiPath) {
        String startUploadPath = apiPath + "/operationType/upload";
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("accept", "*/*");
        requestHeaders.put("Authorization", "Bearer " + sessionTokenProvider.getSessionBearerToken());
        requestHeaders.put("Fusion-Authorization", "Bearer " + datasetTokenProvider.getDatasetBearerToken("common", "test-dataset"));
        HttpResponse<String> startResponse = httpClient.post(startUploadPath, requestHeaders, null);

        if (startResponse.isError()) {
            throw new APICallException(startResponse.getStatusCode());
        }

        //TODO : Knighto; doesn't feel right that we are hand cranking the Gson Builder; bake this into the response parser
        return MultipartTransferContext.started(new GsonBuilder().create().fromJson(startResponse.getBody(), Operation.class));
    }

    /**
     * TODO : Ian; this seems like an fileTransferUtils candidate;
     *
     * @param stream the data to be uploaded
     * @return true if the data to be uploaded is > 50MB
     * @throws IOException on error reading bytes from stream
     */
    public boolean isUploadGreaterThan50MB(InputStream stream) throws IOException {
        final int CHUNK_SIZE = 1024 * 1024; // 1MB
        long totalSize = 0;
        byte[] buffer = new byte[CHUNK_SIZE];
        int bytesRead;
        while ((bytesRead = stream.read(buffer)) != -1) {
            totalSize += bytesRead;
            if (totalSize >= 50 * CHUNK_SIZE) {
                return true;
            }
        }
        return false;
    }

    private <T> void checkResponseStatus(HttpResponse<T> response) throws APICallException {
        if (response.isError()) {
            throw new APICallException(response.getStatusCode());
        }
    }
}
