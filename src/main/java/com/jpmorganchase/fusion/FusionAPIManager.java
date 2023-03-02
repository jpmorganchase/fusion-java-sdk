package com.jpmorganchase.fusion;

import com.jpmorganchase.fusion.credential.IFusionCredentials;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Class that manages calls to the API. Intended to be called from multi-threaded code.
 */
public class FusionAPIManager {

    private static final String DEFAULT_FOLDER = "downloads";
    private static final Pattern patternToken = Pattern.compile(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*");
    private static final Pattern patternExpiry = Pattern.compile(".*\"expires_in\"\\s*:\\s*([^\"]+)}.*");
    private static FusionAPIManager apiManager;

    private final IFusionCredentials sessionCredentials;
    private String bearerToken;
    private int tokenRefreshes = 0;
    private long bearerTokenExpiry;
    private Proxy proxy;

    /**
     * Given a credentials file, returns a new API manager, this could be replaced by a singleton object.
     * @param credentials an object holding API credentials
     * @return a new API session manager
     */
    public static synchronized FusionAPIManager getAPIManager(IFusionCredentials credentials) {
        //TODO: REvisit this logic
        //if (apiManager == null){
            apiManager = new FusionAPIManager(credentials);
        //}
        return apiManager;
    }

    /**
     * Create a new FusionAPIManager object to handle connections to the API.
     * Sets the bearer token
     * @param credentials a credentials file with OAuth parameters.
     */
    private FusionAPIManager(IFusionCredentials credentials){
        this.sessionCredentials = credentials;
        if ( credentials.useProxy() ){
            //TODO: implement
            throw new RuntimeException("Proxy logic not working yet");
            //proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(credentials.getProxyAddress(), credentials.getProxyPort()));
        }
    }


    /**
     * Call the API with the path provided and return the JSON response.
     * @param apiPath appended to the base URL
     */
    public String callAPI(String apiPath) throws APICallException, IOException {

        bearerToken = sessionCredentials.getBearerToken();

        BufferedReader reader;
        String response;

        URL url = new URL(apiPath);

        HttpURLConnection connection;
        if (sessionCredentials.useProxy()){
            connection = (HttpURLConnection) url.openConnection(proxy);
        }else {
            connection = (HttpURLConnection) url.openConnection();
        }

        try{
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");

            //Check the HTTP response code
            int httpCode = connection.getResponseCode();

            if (httpCode != 200){
                throw new APICallException(httpCode);
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));



            StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);

            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }

            response = out.toString();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return response;
    }


    /**
     * Calls the API to retrieve file data and saves to disk
     * @param apiPath the URL of the API endpoint to call
     * @param downloadFolder the folder to save the download file
     * @param fileName the filename
     */
    public void callAPIFileDownload(String apiPath, String downloadFolder, String fileName) throws IOException  {

        bearerToken = sessionCredentials.getBearerToken();
        URL url = new URL(apiPath);
        HttpsURLConnection connection;

        if (sessionCredentials.useProxy()){
            connection = (HttpsURLConnection) url.openConnection(proxy);
        }else {
            connection = (HttpsURLConnection) url.openConnection();
        }

        try {

            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");

            BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
            FileOutputStream fileOutput = new FileOutputStream(fileName);

            byte[] buf = new byte[1024];
            int len;
            while ((len=input.read(buf)) != -1){
                fileOutput.write(buf, 0, len);
            }

            fileOutput.close();
            input.close();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    /**
     * Calls the API to retrieve file data and saves to disk in the default location
     * @param apiPath the URL of the API endpoint to call
     * @param fileName the filename to save into the default folder.
     */
    public void callAPIFileDownload(String apiPath, String fileName) throws IOException {
        this.callAPIFileDownload(apiPath, DEFAULT_FOLDER, fileName);
    }

    /**
     * Call the API upload endpoint to load a distribution
     * @param apiPath the API URL
     * @param fileName the path to the distribution on the local filesystem
     * @param fromDate the earliest date that data is contained in the upload (in form yyyy-MM-dd).
     * @param toDate the latest date that data is contained in the upload (in form yyyy-MM-dd).
     * @param createdDate the creation date for the data is contained in the upload (in form yyyy-MM-dd).
     * @return the HTTP status code - will return 200 if successful
     */
    public int callAPIFileUpload(String apiPath, String fileName, String fromDate, String toDate, String createdDate) throws APICallException, IOException, NoSuchAlgorithmException {

        bearerToken = sessionCredentials.getBearerToken();
        int httpCode;

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(Files.readAllBytes(Paths.get(fileName)));
        String myChecksum = Base64.getEncoder().encodeToString(md.digest());

        URL url = new URL(apiPath);

        HttpsURLConnection connection;
        if (sessionCredentials.useProxy()){
            connection = (HttpsURLConnection) url.openConnection(proxy);
        }else {
            connection = (HttpsURLConnection) url.openConnection();
        }

        try{
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("x-jpmc-distribution-from-date", fromDate);
            connection.setRequestProperty("x-jpmc-distribution-to-date", toDate);
            connection.setRequestProperty("x-jpmc-distribution-created-date", createdDate);
            connection.setRequestProperty("Digest", "md5="+myChecksum);
            PrintStream os = new PrintStream(connection.getOutputStream());
            Files.copy(new File(fileName).toPath(), os);
            os.close();
            httpCode = connection.getResponseCode();

            if (httpCode != 200) {
                throw new APICallException(httpCode);
            }

        }finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return httpCode;

    }
}
