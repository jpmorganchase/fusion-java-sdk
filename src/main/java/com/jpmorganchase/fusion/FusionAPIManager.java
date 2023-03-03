package com.jpmorganchase.fusion;

import com.jpmorganchase.fusion.credential.FusionCredentials;
import com.jpmorganchase.fusion.http.Client;
import com.jpmorganchase.fusion.http.HttpResponse;
import com.jpmorganchase.fusion.http.JdkClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that manages calls to the API. Intended to be called from multi-threaded code.
 */
public class FusionAPIManager {

    private static final String DEFAULT_FOLDER = "downloads";
    private static FusionAPIManager apiManager;
    private final FusionCredentials sessionCredentials;


    //TODO: Make injectable
    //private final Client httpClient = new JdkClient(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8081)));
    private final Client httpClient = new JdkClient();

    /**
     * Given a credentials file, returns a new API manager, this could be replaced by a singleton object.
     *
     * @param credentials an object holding API credentials
     * @return a new API session manager
     */
    public static synchronized FusionAPIManager getAPIManager(FusionCredentials credentials) {
        //TODO: REvisit this logic
        //if (apiManager == null){
        apiManager = new FusionAPIManager(credentials);
        //}
        return apiManager;
    }

    /**
     * Create a new FusionAPIManager object to handle connections to the API.
     * Sets the bearer token
     *
     * @param credentials a credentials file with OAuth parameters.
     */
    private FusionAPIManager(FusionCredentials credentials) {
        this.sessionCredentials = credentials;
        if (credentials.useProxy()) {
            //TODO: implement
            throw new RuntimeException("Proxy logic not working yet");
            //proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(credentials.getProxyAddress(), credentials.getProxyPort()));
        }
    }


    /**
     * Call the API with the path provided and return the JSON response.
     *
     * @param apiPath appended to the base URL
     */
    public String callAPI(String apiPath) throws APICallException, IOException {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + sessionCredentials.getBearerToken());

        HttpResponse<String> response = httpClient.get(apiPath, requestHeaders);

        int httpCode = response.getStatusCode();
        //TODO: Better response code handling?
        if (response.getStatusCode() != 200) {
            throw new APICallException(httpCode);
        }

        return response.getBody();
    }


    /**
     * Calls the API to retrieve file data and saves to disk
     *
     * @param apiPath        the URL of the API endpoint to call
     * @param downloadFolder the folder to save the download file
     * @param fileName       the filename
     */
    public void callAPIFileDownload(String apiPath, String downloadFolder, String fileName) throws IOException, APICallException {

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + sessionCredentials.getBearerToken());

        HttpResponse<InputStream> response = httpClient.getInputStream(apiPath, requestHeaders);

        int httpCode = response.getStatusCode();
        // TODO: Better response code handling?
        if (response.getStatusCode() != 200) {
            throw new APICallException(httpCode);
        }

        BufferedInputStream input = new BufferedInputStream(response.getBody());
        FileOutputStream fileOutput = new FileOutputStream(fileName);

        byte[] buf = new byte[1024];
        int len;
        while ((len = input.read(buf)) != -1) {
            fileOutput.write(buf, 0, len);
        }

        fileOutput.close();
        input.close();

    }

    /**
     * Calls the API to retrieve file data and saves to disk in the default location
     *
     * @param apiPath  the URL of the API endpoint to call
     * @param fileName the filename to save into the default folder.
     */
    public void callAPIFileDownload(String apiPath, String fileName) throws IOException, APICallException {
        this.callAPIFileDownload(apiPath, DEFAULT_FOLDER, fileName);
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
    //TODO: Sort out error handling
    public int callAPIFileUpload(String apiPath, String fileName, String fromDate, String toDate, String createdDate) throws APICallException, IOException, NoSuchAlgorithmException {

        InputStream fileInputStream = Files.newInputStream(new File(fileName).toPath());

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(Files.readAllBytes(Paths.get(fileName)));
        String myChecksum = Base64.getEncoder().encodeToString(md.digest());

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("accept", "*/*");
        requestHeaders.put("Authorization", "Bearer " + sessionCredentials.getBearerToken());
        requestHeaders.put("Content-Type", "application/octet-stream");
        requestHeaders.put("x-jpmc-distribution-from-date", fromDate);
        requestHeaders.put("x-jpmc-distribution-to-date", toDate);
        requestHeaders.put("x-jpmc-distribution-created-date", createdDate);
        requestHeaders.put("Digest", "md5=" + myChecksum);

        HttpResponse<String> response = httpClient.put(apiPath, requestHeaders, fileInputStream);
        //TODO: Close stuff?

        return response.getStatusCode();
    }
}
