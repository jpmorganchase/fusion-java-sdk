package io.github.jpmorganchase.fusion.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.google.common.collect.Lists;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.oauth.provider.SessionTokenProvider;
import java.io.*;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FusionApiManagerTest {

    private FusionAPIManager fusionAPIManager;

    @Mock
    private Client client;

    @Mock
    private SessionTokenProvider sessionTokenProvider;

    private String apiPath;

    private Map<String, String> requestHeaders = new HashMap<>();

    private byte[] downloadBody;

    private Map<String, List<String>> responseHeaders = new HashMap<>();

    private InputStream responseStream;

    private File tempOutputFile;

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
        given(sessionTokenProvider.getSessionBearerToken()).willReturn(token);
    }

    @Test
    void failureForResourceNotFound() throws Exception {
        givenFusionApiManager();
        givenApiPath("http://localhost:8080/test");
        givenSessionBearerToken("my-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenCallToClientToGetReturnsNotFound();
        whenFusionApiManagerIsCalledThenExceptionShouldBeThrown();
        thenExceptionMessageShouldMatchExpected("The requested resource does not exist.");
    }

    @Test
    void successfulFileDownload() throws Exception {
        givenFusionApiManager();
        givenApiPath("http://localhost:8080/test");
        givenSessionBearerToken("my-token");
        givenTempFile("fusion-test-", ".csv");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenDownloadBody("A,B,C\n1,2,3");
        givenResponseHeader("Content-Type", "text/csv");
        givenResponseHeader("Content-Disposition", "attachment; filename=test-testFile.csv");
        givenCallToClientToGetInputStreamIsSuccessfully();
        whenFusionApiManagerIsCalledToDownloadFileToPath();
        thenTheFileContentsShouldMatchExpected();
        finallyDeleteTempFile();
    }

    @Test
    void successfulFileDownloadAsStream() throws Exception {
        givenFusionApiManager();
        givenApiPath("http://localhost:8080/test");
        givenSessionBearerToken("my-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenDownloadBody("A,B,C\n1,2,3");
        givenResponseHeader("Content-Type", "text/csv");
        givenResponseHeader("Content-Disposition", "attachment; filename=test-testFile.csv");
        givenCallToClientToGetInputStreamIsSuccessfully();
        whenFusionApiManagerIsCalledToDownloadFile();
        thenTheDownloadBodyShouldMatchExpected();
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

    private void finallyDeleteTempFile() {
        tempOutputFile.deleteOnExit();
    }

    private void thenTheFileContentsShouldMatchExpected() throws Exception {
        byte[] outputBytes = new byte[(int) tempOutputFile.length()];
        try (FileInputStream fis = new FileInputStream(tempOutputFile)) {
            fis.read(outputBytes);
        }

        assertThat(outputBytes, is(equalTo(downloadBody)));
    }

    private void whenFusionApiManagerIsCalledToDownloadFileToPath() {
        fusionAPIManager.callAPIFileDownload(apiPath, tempOutputFile.getAbsolutePath());
    }

    private void givenTempFile(String prefix, String suffix) throws Exception {
        tempOutputFile = File.createTempFile(prefix, suffix);
    }

    private void thenTheDownloadBodyShouldMatchExpected() throws Exception {
        byte[] outputBytes = new byte[(int) downloadBody.length];
        responseStream.read(outputBytes);

        assertThat(outputBytes, is(equalTo(downloadBody)));
    }

    private void whenFusionApiManagerIsCalledToDownloadFile() {
        responseStream = fusionAPIManager.callAPIFileDownload(apiPath);
    }

    private void givenCallToClientToGetInputStreamIsSuccessfully() {
        HttpResponse<InputStream> expectedHttpResponse = HttpResponse.<InputStream>builder()
                .statusCode(200)
                .body(new ByteArrayInputStream(downloadBody))
                .headers(responseHeaders)
                .build();

        when(client.getInputStream(apiPath, requestHeaders)).thenReturn(expectedHttpResponse);
    }

    private void givenApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    private void givenResponseHeader(String headerKey, String headerValue) {
        responseHeaders.put(headerKey, Lists.newArrayList(headerValue));
    }

    private void givenDownloadBody(String body) {
        this.downloadBody = body.getBytes();
    }

    private void givenRequestHeader(String headerKey, String headerValue) {
        requestHeaders.put(headerKey, headerValue);
    }

    private void givenFusionApiManager() {
        fusionAPIManager = new FusionAPIManager(client, sessionTokenProvider);
    }
}
