package com.jpmorganchase.fusion;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that manages calls to the API
 */
public class FusionAPIManager {

    private static final String DEFAULT_FOLDER = "downloads";
    private static final Pattern patternToken = Pattern.compile(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*");
    private static final Pattern patternExpiry = Pattern.compile(".*\"expires_in\"\\s*:\\s*([^\"]+)}.*");

    private final FusionCredentials sessionCredentials;
    private String bearerToken;
    private int tokenRefreshes = 0;
    private long bearerTokenExpiry;
    private Proxy proxy;

    /**
     * Given a credentials file, returns a new API manager, this could be replaced by a singleton object.
     * @param credentials an object holding API credentials
     * @return a new API session manager
     */
    public static FusionAPIManager getAPIManager(FusionCredentials credentials){
        return new FusionAPIManager(credentials);
    }

    /**
     * Create a new FusionAPIManager object to handle connections to the API.
     * Sets the bearer token
     * @param credentials a credentials file with OAuth parameters.
     */
    private FusionAPIManager(FusionCredentials credentials){
        this.sessionCredentials = credentials;
        if ( credentials.useProxy() ){
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(credentials.getProxyAddress(), credentials.getProxyPort()));
        }
    }

    /**
     * Get an API access token from the OAuth server
     * @return the bearer token as a string
     */
    private String getBearerToken() throws IOException {

        String auth;
        String content;

        if(bearerToken == null) {

            //Check if an obtained token has expired.
            if (System.currentTimeMillis() < this.bearerTokenExpiry){
                return this.bearerToken;
            }

            if(sessionCredentials.isGrantTypePassword()){
                auth = sessionCredentials.getUsername() + ":" + sessionCredentials.getPassword();
                content = String.format("grant_type=password&resource=%1$s&client_id=%2$s&username=%3$s&password=%4$s",
                        sessionCredentials.getResource(), sessionCredentials.getClientID(),
                        sessionCredentials.getUsername(), sessionCredentials.getPassword());

            }else{
                auth = sessionCredentials.getClientID() + ":" + sessionCredentials.getClientSecret();
                content = String.format("grant_type=client_credentials&aud=%1s", sessionCredentials.getResource());

            }

            String authentication = Base64.getEncoder().encodeToString(auth.getBytes());
            String authURL = sessionCredentials.getAuthServerURL();
            BufferedReader reader = null;
            HttpsURLConnection connection = null;

            try {
                URL url = new URL(authURL);
                if (sessionCredentials.useProxy()){
                    connection = (HttpsURLConnection) url.openConnection(proxy);
                }else {
                    connection = (HttpsURLConnection) url.openConnection();
                }
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                if(!sessionCredentials.isGrantTypePassword()) {
                    connection.setRequestProperty("Authorization", "Basic " + authentication);
                }
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Accept", "application/json");
                PrintStream os = new PrintStream(connection.getOutputStream());
                os.print(content);
                os.close();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                String response = out.toString();

                //Get the bearer token
                Matcher matcher = patternToken.matcher(response);
                if (matcher.matches() && matcher.groupCount() > 0) {
                    this.bearerToken = matcher.group(1);
                }

                //Get the token expiry time
                Matcher expiryMatcher = patternExpiry.matcher(response);
                if (expiryMatcher.matches() && expiryMatcher.groupCount() > 0) {
                    Calendar calendar = Calendar.getInstance();
                    int seconds = Integer.parseInt(expiryMatcher.group(1));
                    calendar.add(Calendar.SECOND, seconds-30);
                    this.bearerTokenExpiry = calendar.getTimeInMillis();
                    tokenRefreshes++;
                    System.out.println("Token expires at: " + calendar.getTime());
                    System.out.println("Number of token refreshes: "+ this.tokenRefreshes);
                }

            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return this.bearerToken;
    }

    /**
     * Call the API with the path provided and return the JSON response.
     * @param apiPath appended to the base URL
     */
    public String callAPI(String apiPath) throws APICallException, IOException {

        bearerToken = this.getBearerToken();

        BufferedReader reader;
        String response;

        URL url = new URL(apiPath);

        HttpsURLConnection connection;
        if (sessionCredentials.useProxy()){
            connection = (HttpsURLConnection) url.openConnection(proxy);
        }else {
            connection = (HttpsURLConnection) url.openConnection();
        }

        connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        //Check the HTTP response code
        int httpCode = connection.getResponseCode();
        if (httpCode != 200){
            throw new APICallException(httpCode);
        }

        StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);

        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }

        response = out.toString();
        return response;
    }


    /**
     * Calls the API to retrieve file data and saves to disk
     * @param apiPath the URL of the API endpoint to call
     * @param downloadFolder the folder to save the download file
     * @param fileName the filename
     */
    public void callAPIFileDownload(String apiPath, String downloadFolder, String fileName) throws IOException  {

        bearerToken = this.getBearerToken();

        try {

            URL url = new URL(apiPath);

            HttpsURLConnection connection;
            if (sessionCredentials.useProxy()){
                connection = (HttpsURLConnection) url.openConnection(proxy);
            }else {
                connection = (HttpsURLConnection) url.openConnection();
            }

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

        } catch (Exception e) {
            e.printStackTrace();
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

        bearerToken = this.getBearerToken();
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
            connection.setRequestMethod("POST");
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

        }finally

        {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return httpCode;

    }
}
