package io.github.jpmorganchase.fusion.credential;

import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.http.JdkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OAuthTokenRetriever implements TokenRetriever {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Client httpClient;
    private final TimeProvider timeProvider;

    public OAuthTokenRetriever() {
        this(new JdkClient(), new SystemTimeProvider());
    }

    public OAuthTokenRetriever(Client httpClient, TimeProvider timeProvider) {
        this.httpClient = httpClient;
        this.timeProvider = timeProvider;

    }

    @Override
    public BearerToken retrieve(Credentials credentials) {

        switch (credentials.getCredentialType()) {
            case SECRET:
                return retrieveWithSecretCredentials((OAuthSecretBasedCredentials) credentials);
            case PASSWORD:
                return retrieveWithPasswordCredentials((OAuthPasswordBasedCredentials) credentials);
            case DATASET:
                return retrieveWithDatasetCredentials((OAuthDatasetCredentials) credentials);
            default:
                throw new OAuthException(String.format("Unable to retrieve token, unsupported credential type %s", credentials.getClass().getName()), "Unable to initiate request");
        }

    }


    public BearerToken retrieveWithPasswordCredentials(OAuthPasswordBasedCredentials credentials){
        Map<String, String> requestHeaders = initRequestHeaders();
        requestHeaders.put("Content-Type", "application/x-www-form-urlencoded");

        String body = String.format(
                "grant_type=password&resource=%1$s&client_id=%2$s&username=%3$s&password=%4$s",
                credentials.getResource(), credentials.getClientId(), credentials.getUsername(), credentials.getPassword());

        return retrieve(credentials.getAuthServerUrl(), requestHeaders,  new ArrayList<>(), body);
    }

    public BearerToken retrieveWithSecretCredentials(OAuthSecretBasedCredentials credentials){
        Map<String, String> requestHeaders = initRequestHeaders();
        String auth = credentials.getClientId() + ":" + credentials.getClientSecret();
        String authVal = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        requestHeaders.put("Authorization", authVal);
        requestHeaders.put("Content-Type", "application/x-www-form-urlencoded");

        String body = String.format("grant_type=client_credentials&aud=%1s", credentials.getResource());

        return retrieve(credentials.getAuthServerUrl(), requestHeaders, new ArrayList<>(), body);
    }

    public BearerToken retrieveWithDatasetCredentials(OAuthDatasetCredentials credentials){
        Map<String, String> requestHeaders = initRequestHeaders();
        requestHeaders.put("Authorization", "Bearer " + credentials.getToken());

        return retrieve(credentials.getAuthServerUrl(), requestHeaders, Arrays.asList(credentials.getCatalog(), credentials.getDataset()), null);
    }


    private BearerToken retrieve(String authServerUrl, Map<String, String> requestHeaders, List<String> pathParams, String body){


        HttpResponse<String> response = executeRequest(authServerUrl, requestHeaders, pathParams, body);
        if (response.isError()) {
            throw new OAuthException(
                    String.format(
                            "Error response received from OAuth server with status code %s",
                            response.getStatusCode()),
                    response.getBody());
        }

        OAuthServerResponse oAuthServerResponse = OAuthServerResponse.fromJson(response.getBody());
        if (oAuthServerResponse.getAccessToken() == null) {
            String message = "Unable to parse bearer token in response from OAuth server";
            logger.error(String.format(message)); // Don't log the response body in case it has a token
            throw new OAuthException(message, response.getBody());
        }


        return BearerToken.of(oAuthServerResponse, timeProvider.currentTimeMillis());
    }

    private HttpResponse<String> executeRequest(String authServerUrl, Map<String, String> requestHeaders, List<String> pathParams, String body){

        if (pathParams.size() > 0) {
            return httpClient.get(fusionAuthServerUrlForDataset(authServerUrl, pathParams), requestHeaders);
        }
        return httpClient.post(authServerUrl, requestHeaders, body);

    }

    private String fusionAuthServerUrlForDataset(String fusionAuthServerUrl, List<String> urlPathParams) {
        return String.format(fusionAuthServerUrl, urlPathParams.toArray());
    }

    private Map<String, String> initRequestHeaders(){
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept", "application/json");
        return requestHeaders;
    }

}
