package io.github.jpmorganchase.fusion.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.*;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.jpmorganchase.fusion.digest.AlgoSpecificDigestProducer;
import io.github.jpmorganchase.fusion.digest.DigestDescriptor;
import io.github.jpmorganchase.fusion.digest.DigestProducer;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.oauth.credential.BearerTokenCredentials;
import io.github.jpmorganchase.fusion.oauth.credential.Credentials;
import io.github.jpmorganchase.fusion.oauth.provider.DatasetTokenProvider;
import io.github.jpmorganchase.fusion.oauth.provider.SessionTokenProvider;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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

    @Mock
    private SessionTokenProvider sessionTokenProvider;

    @Mock
    private DatasetTokenProvider datasetTokenProvider;

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

    private MultipartTransferContext multipartTransferContext;

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

    @Test
    void successfulFileUpload() {
        givenFusionApiManager();
        givenApiPath("http://localhost:8080/test");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "simple_dataset", "dataset-token");
        givenUploadFile("upload-test.csv");
        givenUploadBody("A,B,C\n1,2,3\n4,5,6\n7,8,9");
        givenRequestHeader("accept", "*/*");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");
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
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "simple_dataset", "dataset-token");
        givenUploadBody("A,B,C\n1,2,3\n4,5,6\n7,8,9");
        givenRequestHeader("accept", "*/*");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");
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

    @Test
    void successfullyInitiateMultipartUpload() {

        givenFusionApiManager();
        givenApiPath("http://localhost:8080/test/catalogs/common/datasets/test-dataset/datasetseries/2023-03-19/distributions/csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "test-dataset", "dataset-token");
        givenUploadFile("large-upload-test.csv");
        givenRequestHeader("accept", "*/*");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");
        givenCallToClientToInitiateTransferIsSuccessful("some-operation-id-aa");

        whenFusionApiManagerIsCalledToInitiateMultipartUpload();


        thenOperationIdShouldMatch("some-operation-id-aa");
        thenMultipartTransferContextShouldAllowToProgress();

    }

    @Test
    void successfullyUploadParts(){

        givenSha256DigestProducer();
        givenFusionApiManager();
        givenApiPath("http://localhost:8080/test/catalogs/common/datasets/test-dataset/datasetseries/2023-03-19/distributions/csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "test-dataset", "dataset-token");
        givenUploadFile("large-upload-test.csv");

        givenRequestHeader("accept", "*/*");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");
        givenRequestHeader("Content-Type", "application/octet-stream");

        givenMultipartTransferContextStatusIsStarted("my-op-id");

        givenCallToClientToUploadPart(1, "QOFxhmCbpMEDsB6ZWpQGstjqGYrKbSx6FsStJgTK5HA=");
        givenCallToClientToUploadPart(2, "BrRfmO2ryteC4a6+HeOMDwuxXOif0z3qRJ5BDWXKrXg=");
        givenCallToClientToUploadPart(3, "TuKiXOkmJKwfK6luEz3XTKkevrsn2WC8YukQ/pEa6MA=");
        givenCallToClientToUploadPart(4, "bQh4+hfqaWNwCLE1tEJzlqOJ7ZWVDzWQJOsjPaT7Xsk=");

        whenFusionApiManagerIsCalledToUploadParts();

        //then
        thenMultipartTransferContextStatusShouldBeTransferred();

    }

    @Test
    void successfullyCompleteMultipartUpload() {

        givenSha256DigestProducer();
        givenFusionApiManager();
        givenApiPath("http://localhost:8080/test/catalogs/common/datasets/test-dataset/datasetseries/2023-03-19/distributions/csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "test-dataset", "dataset-token");

        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");
        givenRequestHeader("x-jpmc-distribution-from-date", "2023-05-18");
        givenRequestHeader("x-jpmc-distribution-to-date", "2023-05-18");
        givenRequestHeader("x-jpmc-distribution-created-date", "2023-05-18");
        givenRequestHeader("Digest", "SHA-256=kdNJFWpnN7XnQx02fsMYlZ5CxZzdCXvI4GEBIoqh/Wk=");

        givenMultipartTransferContextStatusIsStarted("my-op-id");
        givenPartHasBeenUploaded(1, "provider-gen-part-id-1", "base64-checksum-1", "digest-as-bytes-1".getBytes());
        givenPartHasBeenUploaded(2, "provider-gen-part-id-2", "base64-checksum-2", "digest-as-bytes-2".getBytes());
        givenPartHasBeenUploaded(3, "provider-gen-part-id-3", "base64-checksum-3", "digest-as-bytes-3".getBytes());
        givenPartHasBeenUploaded(4, "provider-gen-part-id-4", "base64-checksum-4", "digest-as-bytes-4".getBytes());
        givenMultipartTransferContextStatusIsTransferred(8 * (1024 * 1024), 33554432, 4);
        givenCallToClientToCompleteMultipartUpload();

        whenFusionApiManagerIsCalledToCompleteMultipartTransfer();

        thenMultipartTransferStatusShouldBeComplete();

    }

    private void thenMultipartTransferStatusShouldBeComplete() {
        assertThat(multipartTransferContext.getStatus(), is(equalTo(MultipartTransferContext.MultipartTransferStatus.COMPLETED)));
    }

    private void givenCallToClientToCompleteMultipartUpload() {

        String body = new GsonBuilder().create().toJson(multipartTransferContext.uploadedParts());
        String path = String.format("/operations/upload?operationId=%s", multipartTransferContext.getOperation().getOperationId());

        when(client.post(eq(apiPath + path), eq(requestHeaders), eq(body)))
                .thenReturn(HttpResponse.<String>builder().statusCode(200).build());
    }

    private void whenFusionApiManagerIsCalledToCompleteMultipartTransfer() {

        multipartTransferContext = fusionAPIManager.callAPIToCompleteMultipartUpload(
                multipartTransferContext,
                apiPath,
                "common",
                "test-dataset",
                "2023-05-18",
                "2023-05-18",
                "2023-05-18");
    }

    private void givenMultipartTransferContextStatusIsTransferred(int chunkSize, int totalBytes, int partCount) {
        multipartTransferContext.transferred(chunkSize, totalBytes, partCount);
    }

    private void givenPartHasBeenUploaded(int partCnt, String partIdentifier, String partChecksum, byte[] partChecksumBytes) {
        multipartTransferContext.partUploaded(UploadedPartContext.builder()
                .part(UploadedPart.builder()
                    .partNumber(String.valueOf(partCnt))
                    .partIdentifier(partIdentifier)
                    .partDigest(partIdentifier)
                    .build())
                .digest(partChecksumBytes)
                .partCount(partCnt).build());
    }

    private void givenSha256DigestProducer() {
        this.digestProducer = AlgoSpecificDigestProducer.builder().sha256().build();
    }

    private void givenMultipartTransferContextStatusIsStarted(String opId) {
        Operation op = new Operation(opId);
        multipartTransferContext = MultipartTransferContext.started(op);
    }

    private void givenCallToClientToUploadPart(int partCnt, String digest) {

        String path = String.format("/operations/upload?operationId=%s&partNumber=%d", multipartTransferContext.getOperation().getOperationId(), partCnt);

        Map<String, String> headers = new HashMap<>(requestHeaders);
        headers.put("Digest", "SHA-256="+digest);

        UploadedPart uploadedPart = UploadedPart.builder()
                .partNumber(String.valueOf(partCnt))
                .partIdentifier("provider-gen-part-id-"+partCnt)
                .partDigest(digest)
                .build();

        String body = new GsonBuilder().create().toJson(uploadedPart);

        when(client.put(eq(apiPath + path), eq(headers), isNotNull()))
                .thenReturn(HttpResponse.<String>builder().body(body).statusCode(200).build());
    }

    private void thenMultipartTransferContextStatusShouldBeTransferred() {
        assertThat(multipartTransferContext.getStatus(), is(equalTo(MultipartTransferContext.MultipartTransferStatus.TRANSFERRED)));
    }

    @SneakyThrows
    private void whenFusionApiManagerIsCalledToUploadParts() {
        InputStream data = Files.newInputStream(new File(fileName).toPath());
        multipartTransferContext = fusionAPIManager.callAPIToUploadParts(multipartTransferContext, apiPath, data);
    }

    private void thenMultipartTransferContextShouldAllowToProgress() {
        assertThat(multipartTransferContext.canProceedToTransfer(), is(equalTo(true)));
    }

    private void thenOperationIdShouldMatch(String expected) {
        assertThat(multipartTransferContext.getOperation().getOperationId(), is(equalTo(expected)));
    }

    private void whenFusionApiManagerIsCalledToInitiateMultipartUpload() {
        multipartTransferContext = fusionAPIManager.callAPIToInitiateMultipartUpload(apiPath);
    }

    private void givenCallToClientToInitiateTransferIsSuccessful(String operationId) {
        when(client.post(eq(apiPath + "/operationType/upload"), eq(requestHeaders), isNull()))
                .thenReturn(HttpResponse.<String>builder().body("{\"operationId\": \"" + operationId + "\"}").statusCode(200).build());
    }

    private void givenDatasetBearerToken(String catalog, String dataset, String token) {
        given(datasetTokenProvider.getDatasetBearerToken(catalog, dataset)).willReturn(token);
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
        // TODO : Fix these tests - they need to define catalog and dataset
        httpStatus = fusionAPIManager.callAPIFileUpload(
                apiPath, fileName, "common", "simple_dataset", fromDate, toDate, createdDate);
    }

    private void whenFusionApiManagerIsCalledToUploadFileFromStream(
            String fromDate, String toDate, String createdDate) {
        httpStatus = fusionAPIManager.callAPIFileUpload(
                apiPath,
                new ByteArrayInputStream(uploadBody),
                "common",
                "simple_dataset",
                fromDate,
                toDate,
                createdDate);
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
        fusionAPIManager = new FusionAPIManager(client, sessionTokenProvider, datasetTokenProvider, digestProducer);
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
