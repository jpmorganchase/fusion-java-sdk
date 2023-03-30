package io.github.jpmorganchase.fusion.api;

import io.github.jpmorganchase.fusion.credential.*;
import io.github.jpmorganchase.fusion.digest.AlgoSpecificDigestProducer;
import io.github.jpmorganchase.fusion.digest.DigestDescriptor;
import io.github.jpmorganchase.fusion.digest.DigestProducer;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.http.JdkClient;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that manages calls to the API. Intended to be called from multithreaded code.
 */
public class FusionAPIManager implements APIManager {

    private static final String DEFAULT_FOLDER = "downloads";
    private final OAuthSessionTokenProvider sessionTokenProvider;
    private final OAuthDatasetTokenProvider datasetTokenProvider;
    private final Client httpClient;
    private final DigestProducer digestProducer;

    /**
     * Create a new FusionAPIManager object to handle connections to the API.
     * Sets the bearer token
     *
     * @param credentials a credentials file with OAuth parameters.
     */
    public FusionAPIManager(Credentials credentials, String rootUrl) {
        this.sessionTokenProvider = new OAuthCredentialAwareSessionTokenProvider(credentials);
        this.datasetTokenProvider = new OAuthProviderAwareDatasetTokenProvider(rootUrl, sessionTokenProvider);
        this.httpClient = new JdkClient();
        this.digestProducer = AlgoSpecificDigestProducer.builder().sha256().build();
    }

    public FusionAPIManager(Credentials credentials, Client httpClient, String rootUrl) {
        this.sessionTokenProvider = new OAuthCredentialAwareSessionTokenProvider(credentials);
        this.datasetTokenProvider = new OAuthProviderAwareDatasetTokenProvider(rootUrl, sessionTokenProvider);
        this.httpClient = httpClient;
        this.digestProducer = AlgoSpecificDigestProducer.builder().sha256().build();
    }

    public FusionAPIManager(Credentials credentials, Client httpClient, String rootUrl, DigestProducer digestProducer) {
        this.sessionTokenProvider = new OAuthCredentialAwareSessionTokenProvider(credentials);
        this.datasetTokenProvider = new OAuthProviderAwareDatasetTokenProvider(rootUrl, sessionTokenProvider);
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

    private <T> void checkResponseStatus(HttpResponse<T> response) throws APICallException {
        if (response.isError()) {
            throw new APICallException(response.getStatusCode());
        }
    }
}
