package io.github.jpmorganchase.fusion.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.oauth.provider.FusionTokenProvider;
import io.github.jpmorganchase.fusion.oauth.provider.SessionTokenProvider;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("SameParameterValue")
@ExtendWith(MockitoExtension.class)
public class FusionApiManagerTest {

    private FusionAPIManager fusionAPIManager;

    @Mock
    private Client client;

    @Mock
    private FusionTokenProvider fusionTokenProvider;

    private String apiPath;

    private final Map<String, String> requestHeaders = new HashMap<>();

    private APICallException thrown;

    private String responseBody;

    private String actualResponse;

    @Test
    void successfulGetCall() {
        givenFusionApiManager();
        givenApiPath("http://localhost:8080/test");
        givenSessionBearerToken("my-token");
        givenResponseBody("sample response");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenCallToClientToGetIsSuccessful();
        WhenFusionApiManagerIsCalledToGet();
        thenTheResponseBodyShouldMatchExpected();
    }

    private void givenSessionBearerToken(String token) {
        given(fusionTokenProvider.getSessionBearerToken()).willReturn(token);
    }

    @Test
    void failureForResourceNotFound() {
        givenFusionApiManager();
        givenApiPath("http://localhost:8080/test");
        givenSessionBearerToken("my-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenCallToClientToGetReturnsNotFound();
        whenFusionApiManagerIsCalledThenExceptionShouldBeThrown();
        thenExceptionMessageShouldMatchExpected("The requested resource does not exist.");
    }

    private void thenTheResponseBodyShouldMatchExpected() {
        assertThat(actualResponse, is(equalTo(responseBody)));
    }

    private void WhenFusionApiManagerIsCalledToGet() {
        actualResponse = fusionAPIManager.callAPI(apiPath);
    }

    private void givenResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    private void givenCallToClientToGetIsSuccessful() {
        HttpResponse<String> expectedHttpResponse = HttpResponse.<String>builder()
                .statusCode(200)
                .body(responseBody)
                .build();
        when(client.get(apiPath, requestHeaders)).thenReturn(expectedHttpResponse);
    }

    private void thenExceptionMessageShouldMatchExpected(String message) {
        assertThat(thrown.getMessage(), is(equalTo(message)));
    }

    private void whenFusionApiManagerIsCalledThenExceptionShouldBeThrown() {
        thrown = assertThrows(
                APICallException.class,
                () -> fusionAPIManager.callAPI(apiPath),
                "Expected APICallException but none thrown");
    }

    private void givenCallToClientToGetReturnsNotFound() {
        HttpResponse<String> expectedHttpResponse = HttpResponse.<String>builder()
                .statusCode(HttpURLConnection.HTTP_NOT_FOUND)
                .build();
        when(client.get(apiPath, requestHeaders)).thenReturn(expectedHttpResponse);
    }

    private void givenApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    private void givenRequestHeader(String headerKey, String headerValue) {
        requestHeaders.put(headerKey, headerValue);
    }

    private void givenFusionApiManager() {
        fusionAPIManager = FusionAPIManager.builder()
                .httpClient(client)
                .tokenProvider(fusionTokenProvider)
                .build();
    }
}
