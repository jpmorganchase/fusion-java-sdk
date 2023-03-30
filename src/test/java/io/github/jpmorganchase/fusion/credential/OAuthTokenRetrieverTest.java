package io.github.jpmorganchase.fusion.credential;


import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OAuthTokenRetrieverTest {

    private static final String GOOD_TOKEN = "{\"access_token\":\"%s\",\"token_type\":\"%s\",\"expires_in\":%s}";
    private static final String MISSING_TOKEN = "{\"token_type\":\"%s\",\"expires_in\":%s}";

    @Mock
    private Client httpClient;

    private TimeProvider timeProvider;

    private OAuthTokenRetriever tokenRetriever;

    private Credentials credentials;

    private Map<String, String> requestHeaders = new HashMap<>();

    String body;

    private String oAuthServerUrl;

    private String oAuthResponseBody;

    private BearerToken bearerToken;

    private OAuthException oAuthException;

    @Test
    public void testTokenRetrievalForSecretBasedCredentialsSucceeds(){

        givenTheCurrentTimeIs(2023, 3, 30, 10, 0, 0);
        givenTokenRetriever();
        givenSecretBasedCredentials("id", "secret", "resource", "https://auth/token");
        givenRequestHeader("Accept", "application/json");
        givenRequestHeader("Authorization", basicAuthValFor("id", "secret"));
        givenRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        givenBodyForSecretCredentials("resource");
        givenAuthServerUrl("https://auth/token");
        givenTheAuthServerResponse("my-oauth-token", "bearer", "3600");
        givenThePostCallToTheAuthServerIsSuccessful();

        whenRetrieveIsCalled();

        thenTheBearerTokenShouldBeEqualTo("my-oauth-token");
        //TODO Check the expiry is relative to current time defined above
    }

    @Test
    public void testTokenRetrievalForPasswordBasedCredentialsSucceeds(){

        givenTheCurrentTimeIs(2023, 3, 30, 10, 0, 0);
        givenTokenRetriever();
        givenPasswordBasedCredentials("some-user", "some-pass", "id", "resource", "https://auth/token");
        givenRequestHeader("Accept", "application/json");
        givenRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        givenBodyForPasswordCredentials("resource", "id", "some-user", "some-pass");
        givenAuthServerUrl("https://auth/token");
        givenTheAuthServerResponse("my-oauth-token", "bearer", "3600");
        givenThePostCallToTheAuthServerIsSuccessful();

        whenRetrieveIsCalled();

        thenTheBearerTokenShouldBeEqualTo("my-oauth-token");
        //TODO Check the expiry is relative to current time defined above
    }

    @Test
    public void testTokenRetrievalForDatasetBasedCredentialsSucceeds(){

        givenTheCurrentTimeIs(2023, 3, 30, 10, 0, 0);
        givenTokenRetriever();
        givenDatasetBasedCredentials("oauth-token", "common", "test", "https://fusion/token/catalogs/%s/datasets/%s/authorize/token");
        givenRequestHeader("Accept", "application/json");
        givenRequestHeader("Authorization", "Bearer oauth-token");
        givenAuthServerUrl("https://fusion/token/catalogs/common/datasets/test/authorize/token");
        givenTheAuthServerResponse("dataset-oauth-token", "bearer", "3600");
        givenTheGetCallToTheAuthServerIsSuccessful();

        whenRetrieveIsCalled();

        thenTheBearerTokenShouldBeEqualTo("dataset-oauth-token");
        //TODO Check the expiry is relative to current time defined above
    }

    @Test
    public void testTokenRetrievalWhenErrorResponseReturned(){

        givenTheCurrentTimeIs(2023, 3, 30, 10, 0, 0);
        givenTokenRetriever();
        givenSecretBasedCredentials("id", "secret", "resource", "https://auth/token");
        givenRequestHeader("Accept", "application/json");
        givenRequestHeader("Authorization", basicAuthValFor("id", "secret"));
        givenRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        givenBodyForSecretCredentials("resource");
        givenAuthServerUrl("https://auth/token");
        givenTheAuthServerResponse("my-oauth-token", "bearer", "3600");
        givenThePostCallToTheAuthServerReturnsAnError(400);

        whenRetrieveIsCalledExceptionShouldBeThrown();

        thenTheExceptionMessageShouldBeAsExpected("Error response received from OAuth server with status code 400");

    }

    @Test
    public void testTokenRetrievalForBearerCredentialsFails(){

        givenTheCurrentTimeIs(2023, 3, 30, 10, 0, 0);
        givenTokenRetriever();
        givenBearerBasedCredentials("my-oauth-token");

        whenRetrieveIsCalledExceptionShouldBeThrown();

        thenTheExceptionMessageShouldBeAsExpected("Unable to retrieve token, unsupported credential type " + credentials.getClass().getName());

    }

    @Test
    public void testTokenRetrievalWhenAccessTokenIsMissing(){

        givenTheCurrentTimeIs(2023, 3, 30, 10, 0, 0);
        givenTokenRetriever();
        givenSecretBasedCredentials("id", "secret", "resource", "https://auth/token");
        givenRequestHeader("Accept", "application/json");
        givenRequestHeader("Authorization", basicAuthValFor("id", "secret"));
        givenRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        givenBodyForSecretCredentials("resource");
        givenAuthServerUrl("https://auth/token");
        givenTheAuthServerResponseForMissingToken("bearer", "3600");
        givenThePostCallToTheAuthServerIsSuccessful();

        whenRetrieveIsCalledExceptionShouldBeThrown();

        thenTheExceptionMessageShouldBeAsExpected("Unable to parse bearer token in response from OAuth server");
        thenTheExceptionResponseShouldBeAsExpected();

    }

    private void thenTheExceptionMessageShouldBeAsExpected(String message) {
        assertThat(oAuthException.getMessage(), is(equalTo(message)));
    }

    private void thenTheExceptionResponseShouldBeAsExpected() {
        assertThat(oAuthException.getResponse(), is(equalTo(oAuthResponseBody)));
    }

    private void givenDatasetBasedCredentials(String token, String catalog, String dataset, String authServerUrl) {
        this.credentials = new OAuthDatasetCredentials(token, catalog, dataset, authServerUrl);
    }

    private void givenBodyForPasswordCredentials(String resource, String id, String username, String password) {
        this. body = String.format(
                "grant_type=password&resource=%1$s&client_id=%2$s&username=%3$s&password=%4$s",
                resource, id, username, password);
    }

    private void givenPasswordBasedCredentials(String username, String password, String id, String resource, String authServerUrl) {
        this.credentials = new OAuthPasswordBasedCredentials(id, resource, authServerUrl, username, password);
    }

    private void thenTheBearerTokenShouldBeEqualTo(String token) {
        assertThat(bearerToken.getToken(), is(equalTo(token)));
    }

    private void whenRetrieveIsCalledExceptionShouldBeThrown() {
        oAuthException = Assertions.assertThrows(OAuthException.class, () -> tokenRetriever.retrieve(credentials));
    }

    private void whenRetrieveIsCalled() {
         bearerToken = tokenRetriever.retrieve(credentials);
    }

    private void givenThePostCallToTheAuthServerIsSuccessful() {
        HttpResponse<String> response = HttpResponse.<String>builder().body(oAuthResponseBody).build();
        given(httpClient.post(oAuthServerUrl, requestHeaders, body)).willReturn(response);
    }

    private void givenThePostCallToTheAuthServerReturnsAnError(int statusCode) {
        HttpResponse<String> response = HttpResponse.<String>builder().statusCode(statusCode).build();
        given(httpClient.post(oAuthServerUrl, requestHeaders, body)).willReturn(response);
    }

    private void givenTheGetCallToTheAuthServerIsSuccessful() {
        HttpResponse<String> response = HttpResponse.<String>builder().body(oAuthResponseBody).build();
        given(httpClient.get(oAuthServerUrl, requestHeaders)).willReturn(response);
    }

    private void givenAuthServerUrl(String oAuthServerUrl) {
        this.oAuthServerUrl = oAuthServerUrl;
    }

    private void givenTheAuthServerResponse(String token, String type, String expiresIn) {
        oAuthResponseBody = String.format(GOOD_TOKEN, token, type, expiresIn);
    }

    private void givenTheAuthServerResponseForMissingToken(String type, String expiresIn) {
        oAuthResponseBody = String.format(MISSING_TOKEN, type, expiresIn);
    }


    private void givenBodyForSecretCredentials(String resource) {
        body = String.format("grant_type=client_credentials&aud=%1s", resource);
    }

    private void givenRequestHeader(String headerKey, String headerVal) {
        requestHeaders.put(headerKey, headerVal);
    }

    private String basicAuthValFor(String id, String secret) {
        String auth = id + ":" + secret;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    private void givenTokenRetriever() {
        this.tokenRetriever = new OAuthTokenRetriever(httpClient, timeProvider);
    }

    private void givenTheCurrentTimeIs(int year, int month, int day, int hour, int minute, int seconds) {
        timeProvider = new IncrementingTimeProvider(year,month, day, hour, minute, seconds);
    }


    private void givenSecretBasedCredentials(String id, String secret, String resource, String authServerUrl) {
        credentials = new OAuthSecretBasedCredentials(id, resource, authServerUrl, secret);
    }

    private void givenBearerBasedCredentials(String token) {
        credentials = new BearerTokenCredentials(token);
    }


}