package io.github.jpmorganchase.fusion.api;

import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.oauth.credential.BearerTokenCredentials;
import io.github.jpmorganchase.fusion.oauth.provider.SessionTokenProvider;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;

/**
 * Class that manages calls to the API. Intended to be called from multithreaded code.
 */
@AllArgsConstructor
public class FusionAPIManager implements APIManager {

    private static final String DEFAULT_FOLDER = "downloads";

    private final Client httpClient;
    private final SessionTokenProvider sessionTokenProvider;

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

    private <T> void checkResponseStatus(HttpResponse<T> response) throws APICallException {
        if (response.isError()) {
            throw new APICallException(response.getStatusCode());
        }
    }
}
