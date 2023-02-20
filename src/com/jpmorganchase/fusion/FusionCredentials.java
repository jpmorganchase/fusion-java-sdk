package com.jpmorganchase.fusion;

import java.io.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 */
public class FusionCredentials {

    //Constants definitions
    private static String NO_PROXY = "";
    private static String DEFAULT_CREDENTIALS_FILE = "config/client_credentials.json";

    private String clientID;
    private String clientSecret;
    private String resource;
    private String authServerURL;
    private String proxies;
    private String username;
    private String password;
    private boolean grantTypePassword = false;

    /**
     * Returns the URL of an authentication service providing an OIDC token
     * @return the authentication server URL
     */
    public String getAuthServerURL(){
        return this.authServerURL;
    }

    /**
     * Returns an OIDC client ID
     * @return OIDC client ID
     */
    public String getClientID(){
        return this.clientID;
    }

    /**
     * Returns an OIDC client secret
     * @return OIDC client secret
     */
    public String getClientSecret(){
        return this.clientSecret;
    }

    /**
     * Returns an OAuth resource (audience)
     * @return OAuth aud
     */
    public String getResource(){
        return this.resource;
    }


    /**
     * Create a new object to hold credentials required to authenticate
     * @param aClientID an OIDC client id
     * @param aClientSecret an OIDC client secret
     * @param aResource an OAuth audience
     * @param anAuthServerURL the URL for the authentication server
     * @param theProxies proxies addresses, required if connecting from behind a firewall.
     */
    public FusionCredentials(String aClientID, String aClientSecret, String aResource, String anAuthServerURL, String theProxies){

        this.clientID = aClientID;
        this.clientSecret = aClientSecret;
        this.resource = aResource;
        this.authServerURL = anAuthServerURL;
        this.proxies = theProxies;

    }

    /**
     * Create a new object to hold credentials required to authenticate
     * @param aClientID an OIDC client id
     * @param aClientSecret an OIDC client secret
     * @param aResource an OAuth audience
     * @param anAuthServerURL the URL for the authentication server
     */
    public FusionCredentials(String aClientID, String aClientSecret, String aResource, String anAuthServerURL){

        this(aClientID, aClientSecret,aResource,anAuthServerURL, NO_PROXY);

    }

    /**
     * Create a new object to hold credentials required to authenticate
     * @param aClientID an OIDC client id
     * @param aClientSecret an OIDC client secret
     * @param theProxies proxies addresses, required if connecting from behind a firewall.
     */
    public FusionCredentials(String aClientID, String aClientSecret, String theProxies){

        this(aClientID, aClientSecret,"JPMC:URI:RS-93742-Fusion-PROD","https://authe.jpmorgan.com/as/token.oauth2", theProxies);

    }

    /**
     * Create a new object to hold credentials required to authenticate
     * @param aClientID an OIDC client id
     * @param aClientSecret an OIDC client secret
     */
    public FusionCredentials(String aClientID, String aClientSecret){

        this(aClientID, aClientSecret,"JPMC:URI:RS-93742-Fusion-PROD","https://authe.jpmorgan.com/as/token.oauth2", NO_PROXY);

    }

    /**
     * Create a new object to hold credentials required to authenticate
     * @param aClientID an OIDC client id
     * @param username a valid username
     * @param password a valid password
     * @param aResource an OAuth audience
     * @param anAuthServerURL the URL for the authentication server
     * @param theProxies proxies addresses, required if connecting from behind a firewall.
     */
    public FusionCredentials(String aClientID, String username, String password, String aResource, String anAuthServerURL, String theProxies){

        this.grantTypePassword = true;
        this.clientID = aClientID;
        this.username = username;
        this.password = password;
        this.resource = aResource;
        this.authServerURL = anAuthServerURL;
        this.proxies = theProxies;

    }

    /**
     * Writes the credentials to a JSON object in a file.
     * @param fullPathFileName the full path and filename to save the credentials
     * @return
     */
    public int writeCredentialsFile(String fullPathFileName){

        JSONObject json = new JSONObject();
        json.put("client_id", this.clientID);
        json.put("client_secret", this.clientSecret);
        json.put("resource", this.resource);
        json.put("auth_url", this.authServerURL);

        File credentialFile = new File(fullPathFileName);
        try{
            credentialFile.getParentFile().mkdirs();
            credentialFile.createNewFile();
        }catch(IOException e){
            e.printStackTrace();
        }

        try(PrintWriter pw = new PrintWriter(new FileWriter(credentialFile))){
            pw.write(json.toJSONString());
        }catch(IOException e){
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Writes the credentials to a JSON object in the default path and filename.
     * @return
     */
    public int writeCredentialsFile(){
        return this.writeCredentialsFile(DEFAULT_CREDENTIALS_FILE);
    }

    /**
     * Reads saved credentials from a file
     * @param fullPathFileName the full path and filename from where to read the credentials
     * @return a credentials object
     */
    public static FusionCredentials readCredentialsFile(String fullPathFileName){

        JSONParser jsonParser = new JSONParser();
        String json = "";
        String clientID = "";
        String clientSecret = "";
        String resource = "";
        String authURL = "";

        try (FileReader reader = new FileReader(fullPathFileName))
        {
            //Read JSON file
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

            clientID = (String) jsonObject.get("client_id");
            System.out.println("clientID:"+clientID);
            clientSecret = (String) jsonObject.get("client_secret");
            System.out.println("secret:"+clientSecret);
            resource = (String) jsonObject.get("resource");
            System.out.println("resource:"+resource);
            authURL = (String) jsonObject.get("auth_url");
            System.out.println("auth URL:"+authURL);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new FusionCredentials(clientID,clientSecret,resource,authURL);
    }

    /**
     * Reads saved credentials from the default path and filename
     * @return a credentials object
     */
    public static FusionCredentials readCredentialsFile(){
        return readCredentialsFile(DEFAULT_CREDENTIALS_FILE);
    }

    /**
     * Authenticate with password
     * @return true if password authentication
     */
    public boolean isGrantTypePassword() {
        return grantTypePassword;
    }

    /**
     * Get the username for authentication
     * @return a username
     */
    public String getUsername(){
        return this.username;
    }

    /**
     * Get a password for authentication
     * @return a password
     */
    public String getPassword(){
        return this.password;
    }
}
