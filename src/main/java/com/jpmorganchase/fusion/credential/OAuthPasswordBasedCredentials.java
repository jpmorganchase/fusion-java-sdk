package com.jpmorganchase.fusion.credential;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class OAuthPasswordBasedCredentials implements IFusionCredentials{

    private final String clientId;
    private final String resource;
    private final URL authServerUrl;
    private final String username;
    private final String password;
    private String bearerToken;
    private long bearerTokenExpiry;
    private int tokenRefreshes;


    public OAuthPasswordBasedCredentials(String clientId, String username, String password, String resource, String authServerUrl) throws MalformedURLException {
        this.clientId = clientId;
        this.username = username;
        this.resource = resource;
        this.password = password;
        this.authServerUrl = new URL(authServerUrl);
        tokenRefreshes = 0;
    }

    @Override
    public synchronized String getBearerToken() throws IOException {
        String content;

        if(bearerToken == null) {

            //Check if an obtained token has expired.
            if (System.currentTimeMillis() < this.bearerTokenExpiry){
                return this.bearerToken;
            }

            content = String.format("grant_type=password&resource=%1$s&client_id=%2$s&username=%3$s&password=%4$s",
                    this.resource, this.clientId,
                    this.username, this.password);

            BufferedReader reader = null;
            HttpURLConnection connection = null;

            try {
                if (this.useProxy()){
                    connection = (HttpURLConnection) authServerUrl.openConnection(null);//TODO: This needs fixed when proxy logic is done
                }else {
                    connection = (HttpURLConnection) authServerUrl.openConnection();
                }
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
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
                OAuthServerResponse oAuthServerResponse = OAuthServerResponse.fromJson(response);
                this.bearerToken = oAuthServerResponse.getAccessToken();

                //Get the token expiry time
                Calendar calendar = Calendar.getInstance();
                int seconds = oAuthServerResponse.getExpiresIn();
                calendar.add(Calendar.SECOND, seconds-30);
                this.bearerTokenExpiry = calendar.getTimeInMillis();
                tokenRefreshes++;
                System.out.println("Token expires at: " + calendar.getTime());
                System.out.println("Number of token refreshes: "+ this.tokenRefreshes);

            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return this.bearerToken;
    }

    @Override
    public boolean useProxy() {
        return false;
    }
}
