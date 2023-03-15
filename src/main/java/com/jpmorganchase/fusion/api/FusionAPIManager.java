package com.jpmorganchase.fusion.api;

import com.jpmorganchase.fusion.credential.BearerTokenCredentials;
import com.jpmorganchase.fusion.credential.Credentials;
import com.jpmorganchase.fusion.http.Client;
import com.jpmorganchase.fusion.http.HttpResponse;
import com.jpmorganchase.fusion.http.JdkClient;
import java.io.*;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;

/**
 * Class that manages calls to the API. Intended to be called from multithreaded code.
 */
public class FusionAPIManager implements APIManager {

    private static final String DEFAULT_FOLDER = "downloads";
    private Credentials sessionCredentials;
    private final Client httpClient;

    /**
     * Create a new FusionAPIManager object to handle connections to the API.
     * Sets the bearer token
     *
     * @param credentials a credentials file with OAuth parameters.
     */
    public FusionAPIManager(Credentials credentials) {
        this.sessionCredentials = credentials;
        this.httpClient = new JdkClient();
    }

    public FusionAPIManager(Credentials credentials, Client httpClient) {
        this.sessionCredentials = credentials;
        this.httpClient = httpClient;
    }

    public void updateBearerToken(String token) {
        if (sessionCredentials instanceof BearerTokenCredentials) {
            this.sessionCredentials = new BearerTokenCredentials(token);
        } else {
            throw new ApiInputValidationException(String.format(
                    "Cannot update bearer token for credentials of type %s",
                    sessionCredentials.getClass().getName()));
        }
    }

    /**
     * Call the API with the path provided and return the JSON response.
     *
     * @param apiPath appended to the base URL
     */
    @Override
    public String callAPI(String apiPath) throws APICallException {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + sessionCredentials.getBearerToken());

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
        requestHeaders.put("Authorization", "Bearer " + sessionCredentials.getBearerToken());

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
    public int callAPIFileUpload(String apiPath, String fileName, String fromDate, String toDate, String createdDate)
            throws APICallException {

        InputStream fileInputStream;
        try {
            fileInputStream = Files.newInputStream(new File(fileName).toPath());
        } catch (IOException e) {
            throw new ApiInputValidationException(
                    String.format("File does not exist at supplied input location: %s", fileName), e);
        }

        return callAPIFileUpload(apiPath, fileInputStream, fromDate, toDate, createdDate);
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
    @SneakyThrows(NoSuchAlgorithmException.class)
    @Override
    public int callAPIFileUpload(String apiPath, InputStream data, String fromDate, String toDate, String createdDate)
            throws APICallException {

        DigestInputStream dis = new DigestInputStream(data, MessageDigest.getInstance("MD5"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int length;
        while (true) {
            try {
                if ((length = dis.read(buf)) == -1) break;
            } catch (IOException e) {
                throw new ApiInputValidationException("Failed to read data from input", e);
            }
            baos.write(buf, 0, length);
        }

        String myChecksum =
                Base64.getEncoder().encodeToString(dis.getMessageDigest().digest());

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("accept", "*/*");
        requestHeaders.put("Authorization", "Bearer " + sessionCredentials.getBearerToken());
        requestHeaders.put("Content-Type", "application/octet-stream");
        requestHeaders.put("x-jpmc-distribution-from-date", fromDate);
        requestHeaders.put("x-jpmc-distribution-to-date", toDate);
        requestHeaders.put("x-jpmc-distribution-created-date", createdDate);
        requestHeaders.put("Digest", "md5=" + myChecksum);

        HttpResponse<String> response =
                httpClient.put(apiPath, requestHeaders, new ByteArrayInputStream(baos.toByteArray()));

        checkResponseStatus(response);

        return response.getStatusCode();
    }

    // TODO: Clean up error handling
    private <T> void checkResponseStatus(HttpResponse<T> response) throws APICallException {
        int httpCode = response.getStatusCode();
        if (httpCode != 200) {
            throw new APICallException(httpCode);
        }
    }
}
