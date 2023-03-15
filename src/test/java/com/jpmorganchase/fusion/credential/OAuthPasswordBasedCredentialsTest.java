package com.jpmorganchase.fusion.credential;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.jpmorganchase.fusion.http.Client;
import com.jpmorganchase.fusion.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OAuthPasswordBasedCredentialsTest {

    private OAuthPasswordBasedCredentials credentials;

    @Mock
    private Client client;

    private static final String tokenJson =
            "{\"access_token\":\"my-oauth-generated-token\",\"token_type\":\"bearer\",\"expires_in\":3600}";

    private static final String missingTokenJson = "{\"token_type\":\"bearer\",\"expires_in\":3600}";

    private static final String shortExpiryTokenJson =
            "{\"access_token\":\"my-short-expiry-oauth-generated-token\",\"token_type\":\"bearer\",\"expires_in\":1}";

    @Test
    void retrievalOfOAuthToken() throws Exception {
        credentials = new OAuthPasswordBasedCredentials(
                "aClientID", "aUsername", "aPassword", "aResource", "http://localhost:8080/oAuth", client);

        Map<String, String> expectedOAuthHeaders = new HashMap<>();
        expectedOAuthHeaders.put("Accept", "application/json");
        expectedOAuthHeaders.put("Content-Type", "application/x-www-form-urlencoded");

        HttpResponse<String> oAuthResponse =
                HttpResponse.<String>builder().body(tokenJson).build();

        when(client.post(
                        "http://localhost:8080/oAuth",
                        expectedOAuthHeaders,
                        "grant_type=password&resource=aResource&client_id=aClientID&username=aUsername&password=aPassword"))
                .thenReturn(oAuthResponse);

        String response = credentials.getBearerToken();

        assertThat(response, is(equalTo("my-oauth-generated-token")));
        assertThat(credentials.getTokenRefreshes(), is(equalTo(1)));
    }

    @Test
    void retrievalOfOAuthTokenResultsInCorrectExceptionWhenAnInvalidResponseIsReceived() throws Exception {
        credentials = new OAuthPasswordBasedCredentials(
                "aClientID", "aUsername", "aPassword", "aResource", "http://localhost:8080/oAuth", client);

        Map<String, String> expectedOAuthHeaders = new HashMap<>();
        expectedOAuthHeaders.put("Accept", "application/json");
        expectedOAuthHeaders.put("Content-Type", "application/x-www-form-urlencoded");

        HttpResponse<String> invalidOAuthResponse =
                HttpResponse.<String>builder().body(missingTokenJson).build();

        when(client.post(
                        "http://localhost:8080/oAuth",
                        expectedOAuthHeaders,
                        "grant_type=password&resource=aResource&client_id=aClientID&username=aUsername&password=aPassword"))
                .thenReturn(invalidOAuthResponse);

        OAuthException thrown = assertThrows(OAuthException.class, () -> {
            credentials.getBearerToken();
        });

        assertThat(thrown.getResponse(), is(equalTo(missingTokenJson)));
        assertThat(thrown.getMessage(), is(equalTo("Unable to parse bearer token in response from OAuth server")));
    }

    @Test
    // TODO: The when call needs to be refactored to remove this suppression
    @SuppressWarnings("unchecked")
    void refreshOfOauthTokenAfterExpiry() throws Exception {
        IncrementingTimeProvider timeProvider = new IncrementingTimeProvider(2023, 3, 14, 12, 0, 0);

        credentials = new OAuthPasswordBasedCredentials(
                "aClientID",
                "aUsername",
                "aPassword",
                "aResource",
                "http://localhost:8080/oAuth",
                client,
                timeProvider);

        Map<String, String> expectedOAuthHeaders = new HashMap<>();
        expectedOAuthHeaders.put("Accept", "application/json");
        expectedOAuthHeaders.put("Content-Type", "application/x-www-form-urlencoded");

        HttpResponse<String> shortExpiryOauthResponse =
                HttpResponse.<String>builder().body(shortExpiryTokenJson).build();

        HttpResponse<String> regularOauthResponse =
                HttpResponse.<String>builder().body(tokenJson).build();

        when(client.post(
                        "http://localhost:8080/oAuth",
                        expectedOAuthHeaders,
                        "grant_type=password&resource=aResource&client_id=aClientID&username=aUsername&password=aPassword"))
                .thenReturn(shortExpiryOauthResponse, regularOauthResponse);

        String firstResponse = credentials.getBearerToken();
        timeProvider.increment(30);
        String secondResponse = credentials.getBearerToken();

        assertThat(firstResponse, is(equalTo("my-short-expiry-oauth-generated-token")));
        assertThat(secondResponse, is(equalTo("my-oauth-generated-token")));
        assertThat(credentials.getTokenRefreshes(), is(equalTo(2)));
    }

    @Test
    void reuseOfOAuthTokenBeforeExpiry() throws Exception {
        IncrementingTimeProvider timeProvider = new IncrementingTimeProvider(2023, 3, 14, 12, 0, 0);

        credentials = new OAuthPasswordBasedCredentials(
                "aClientID",
                "aUsername",
                "aPassword",
                "aResource",
                "http://localhost:8080/oAuth",
                client,
                timeProvider);

        Map<String, String> expectedOAuthHeaders = new HashMap<>();
        expectedOAuthHeaders.put("Accept", "application/json");
        expectedOAuthHeaders.put("Content-Type", "application/x-www-form-urlencoded");

        HttpResponse<String> regularOauthResponse =
                HttpResponse.<String>builder().body(tokenJson).build();

        when(client.post(
                        "http://localhost:8080/oAuth",
                        expectedOAuthHeaders,
                        "grant_type=password&resource=aResource&client_id=aClientID&username=aUsername&password=aPassword"))
                .thenReturn(regularOauthResponse);

        String firstResponse = credentials.getBearerToken();
        timeProvider.increment(3569);
        String secondResponse = credentials.getBearerToken();

        assertThat(firstResponse, is(equalTo("my-oauth-generated-token")));
        assertThat(secondResponse, is(equalTo("my-oauth-generated-token")));
        assertThat(credentials.getTokenRefreshes(), is(equalTo(1)));
    }

    @Test
    void requiresAuthHeaderIsFalse() {
        credentials = new OAuthPasswordBasedCredentials(
                "aClientID", "aUsername", "aPassword", "aResource", "http://localhost:8080/oAuth", client);
        assertThat(credentials.requiresAuthHeader(), is(false));
    }

    @Test
    void authHeaderIsNull() {
        credentials = new OAuthPasswordBasedCredentials(
                "aClientID", "aUsername", "aPassword", "aResource", "http://localhost:8080/oAuth", client);
        assertThat(credentials.getAuthHeader(), is(nullValue()));
    }
}
