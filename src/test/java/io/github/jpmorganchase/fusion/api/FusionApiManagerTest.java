package io.github.jpmorganchase.fusion.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.google.common.collect.Lists;
import io.github.jpmorganchase.fusion.credential.BearerTokenCredentials;
import io.github.jpmorganchase.fusion.credential.Credentials;
import io.github.jpmorganchase.fusion.digest.DigestDescriptor;
import io.github.jpmorganchase.fusion.digest.DigestProducer;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FusionApiManagerTest {

    private Credentials credentials = new BearerTokenCredentials("my-token");
    private FusionAPIManager fusionAPIManager;

    @Mock
    private Client client;

    @Mock
    private DigestProducer digestProducer;

    private String apiPath;

    private Map<String, String> requestHeaders = new HashMap<>();

    private DigestDescriptor digestDescriptor;

    private byte[] uploadBody;

    private int httpStatus;

    private String fileName;

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
        givenResponseBody("sample response");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenCallToClientToGetIsSuccessful();
        WhenFusionApiManagerIsCalledToGet();
        thenTheResponseBodyShouldMatchExpected();
    }

    @Test
    void failureForResourceNotFound() throws Exception {
        givenFusionApiManager();
        givenApiPath("http://localhost:8080/test");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenCallToClientToGetReturnsNotFound();
        whenFusionApiManagerIsCalledThenExceptionShouldBeThrown();
        thenExceptionMessageShouldMatchExpected("The requested resource does not exist.");
    }

    @Test
    void successfulFileDownload() throws Exception {
        givenFusionApiManager();
        givenApiPath("http://localhost:8080/test");
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
        givenRequestHeader("Authorization", "Bearer my-token");
        givenDownloadBody("A,B,C\n1,2,3");
        givenResponseHeader("Content-Type", "text/csv");
        givenResponseHeader("Content-Disposition", "attachment; filename=test-testFile.csv");
        givenCallToClientToGetInputStreamIsSuccessfully();
        whenFusionApiManagerIsCalledToDownloadFile();
        thenTheDownloadBodyShouldMatchExpected();
    }

    @Test
    void successfulFileUpload() {
        givenFusionApiManager();
        givenApiPath("http://localhost:8080/test");
        givenUploadFile("upload-test.csv");
        givenUploadBody("A,B,C\n1,2,3\n4,5,6\n7,8,9");
        givenRequestHeader("accept", "*/*");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Content-Type", "application/octet-stream");
        givenRequestHeader("x-jpmc-distribution-from-date", "2023-03-01");
        givenRequestHeader("x-jpmc-distribution-to-date", "2023-03-02");
        givenRequestHeader("x-jpmc-distribution-created-date", "2023-03-03");
        givenRequestHeader("Digest", "SHA-256=k0IH+I4DpJla6wabZBNCUEMSBZtS2seC/9ixCa3KnZE=");
        givenRequestHeader("Content-Length", "23");
        givenCallToProduceDigestReturnsDigestDescriptor("k0IH+I4DpJla6wabZBNCUEMSBZtS2seC/9ixCa3KnZE=");
        givenCallToClientToUploadIsSuccessful();
        whenFusionApiManagerIsCalledToUploadFileFromPath("2023-03-01", "2023-03-02", "2023-03-03");
        thenHttpStatusShouldIndicateSuccess();
    }

    @Test
    void successfulFileUploadWithStream() {
        givenFusionApiManager();
        givenApiPath("http://localhost:8080/test");
        givenUploadBody("A,B,C\n1,2,3\n4,5,6\n7,8,9");
        givenRequestHeader("accept", "*/*");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Content-Type", "application/octet-stream");
        givenRequestHeader("x-jpmc-distribution-from-date", "2023-03-01");
        givenRequestHeader("x-jpmc-distribution-to-date", "2023-03-02");
        givenRequestHeader("x-jpmc-distribution-created-date", "2023-03-03");
        givenRequestHeader("Digest", "SHA-256=k0IH+I4DpJla6wabZBNCUEMSBZtS2seC/9ixCa3KnZE=");
        givenRequestHeader("Content-Length", "23");
        givenCallToProduceDigestReturnsDigestDescriptor("k0IH+I4DpJla6wabZBNCUEMSBZtS2seC/9ixCa3KnZE=");
        givenCallToClientToUploadIsSuccessful();
        whenFusionApiManagerIsCalledToUploadFileFromStream("2023-03-01", "2023-03-02", "2023-03-03");
        thenHttpStatusShouldIndicateSuccess();
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

    private void givenUploadFile(String resource) {
        this.fileName = getPathFromResource(resource);
    }

    private void thenHttpStatusShouldIndicateSuccess() {
        assertThat(httpStatus, is(equalTo(200)));
    }

    private void whenFusionApiManagerIsCalledToUploadFileFromPath(String fromDate, String toDate, String createdDate) {
        httpStatus = fusionAPIManager.callAPIFileUpload(apiPath, fileName, fromDate, toDate, createdDate);
    }

    private void whenFusionApiManagerIsCalledToUploadFileFromStream(
            String fromDate, String toDate, String createdDate) {
        httpStatus = fusionAPIManager.callAPIFileUpload(
                apiPath, new ByteArrayInputStream(uploadBody), fromDate, toDate, createdDate);
    }

    private void givenCallToClientToUploadIsSuccessful() {
        when(client.put(eq(apiPath), eq(requestHeaders), argThat(bodyEquals(uploadBody))))
                .thenReturn(HttpResponse.<String>builder().statusCode(200).build());
    }

    private void givenUploadBody(String uploadbody) {
        this.uploadBody = uploadbody.getBytes();
    }

    private void givenCallToProduceDigestReturnsDigestDescriptor(String digest) {
        digestDescriptor = DigestDescriptor.builder()
                .checksum(digest)
                .size(uploadBody.length)
                .content(uploadBody)
                .build();

        when(digestProducer.execute(argThat(bodyEquals(uploadBody)))).thenReturn(digestDescriptor);
    }

    private void givenRequestHeader(String headerKey, String headerValue) {
        requestHeaders.put(headerKey, headerValue);
    }

    private void givenFusionApiManager() {
        fusionAPIManager = new FusionAPIManager(credentials, client, digestProducer);
    }

    private static String getPathFromResource(String resourceName) {
        URL url = FusionApiManagerTest.class.getResource(resourceName);
        try {
            Path path = Paths.get(url.toURI());
            return path.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to locate test data", e);
        }
    }

    private static ArgumentMatcher<InputStream> bodyEquals(byte[] body) {
        return new InputStreamArgumentMatcher(new ByteArrayInputStream(body));
    }

    @RequiredArgsConstructor
    private static class InputStreamArgumentMatcher implements ArgumentMatcher<InputStream> {

        private final InputStream left;

        @Override
        public boolean matches(InputStream right) {

            String leftBody = body(left);
            if (Objects.nonNull(right)) {
                String rightBody = body(right);
                return leftBody.equals(rightBody);
            }

            return false;
        }

        private String body(InputStream stream) {
            try {
                int n = stream.available();
                byte[] bytes = new byte[n];
                stream.read(bytes, 0, n);
                return new String(bytes, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                Assertions.fail(ex);
            }
            return "";
        }
    }
}
