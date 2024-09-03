package io.github.jpmorganchase.fusion.api.request;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.response.GetPartResponse;
import io.github.jpmorganchase.fusion.api.response.Head;
import io.github.jpmorganchase.fusion.api.stream.IntegrityCheckingInputStream;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.oauth.provider.FusionTokenProvider;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings({"SameParameterValue", "unchecked"})
@ExtendWith(MockitoExtension.class)
class PartFetcherTest {

    @Mock
    Client client;

    @Mock
    FusionTokenProvider credentials;

    DownloadRequest dr;

    PartRequest pr;

    PartFetcher testee;

    GetPartResponse actual;

    Map<String, List<String>> responseHeaders = new HashMap<>();

    HttpResponse<InputStream> httpResponse;
    HttpResponse badResponse;

    APICallException exception;

    @Test
    public void testFetchPartForSinglePartDownloadWithoutHeaders() throws Exception {

        // Given
        givenPartFetcher();
        givenDownloadRequest("foo", "bar", "http://foobar.com/v1/some/resource");
        givenPartRequestForSinglePartDownload("Om6weQ85rIfJTzhWst0sXREOaBFgImGpqSPTuyOtyLc=");
        givenCallToGetSessionBearerReturns("session-token");
        givenCallToGetDatasetBearerReturns("foo", "bar", "dataset-token");
        givenSinglePartResponseHeaders("3");
        givenCallToGetInputStreamForSinglePartDownload(
                "data", "http://foobar.com/v1/some/resource", "session-token", "dataset-token");

        // when
        whenFetchIsInvoked();

        // Then
        thenStreamShouldBeAsExpected();
        thenStreamDataShouldBeAsExpected("data");
        thenChecksumInHeadShouldBeAsExpected("Om6weQ85rIfJTzhWst0sXREOaBFgImGpqSPTuyOtyLc=");
    }

    @Test
    public void testFetchPartForSinglePartDownloadWithHeaders() throws Exception {

        // Given
        givenPartFetcher();
        Map<String, String> headers = givenHeader("foo", "bar");
        givenDownloadRequest("foo", "bar", "http://foobar.com/v1/some/resource", headers);
        givenPartRequestForSinglePartDownload("Om6weQ85rIfJTzhWst0sXREOaBFgImGpqSPTuyOtyLc=");
        givenCallToGetSessionBearerReturns("session-token");
        givenCallToGetDatasetBearerReturns("foo", "bar", "dataset-token");
        givenSinglePartResponseHeaders("3");
        givenCallToGetInputStreamForSinglePartDownload(
                "data", "http://foobar.com/v1/some/resource", "session-token", "dataset-token", headers);

        // when
        whenFetchIsInvoked();

        // Then
        thenStreamShouldBeAsExpected();
        thenStreamDataShouldBeAsExpected("data");
        thenChecksumInHeadShouldBeAsExpected("Om6weQ85rIfJTzhWst0sXREOaBFgImGpqSPTuyOtyLc=");
    }

    @Test
    public void testFetchPartForMultipartDownloadWithoutHeaders() throws Exception {

        // Given
        givenPartFetcher();
        givenDownloadRequest("foo", "bar", "http://foobar.com/v1/some/resource");
        givenPartRequestForMultiPartDownload(2);
        givenCallToGetSessionBearerReturns("session-token");
        givenCallToGetDatasetBearerReturns("foo", "bar", "dataset-token");
        givenResponseHeadersForMultipart(
                "version-1", "Om6weQ85rIfJTzhWst0sXREOaBFgImGpqSPTuyOtyLc=", "5", "23", "bytes 0-4/23");
        givenCallToGetInputStreamReturnsSuccess(
                "data",
                "http://foobar.com/v1/some/resource/operationType/download?downloadPartNumber=2",
                "session-token",
                "dataset-token");

        // when
        whenFetchIsInvoked();

        // Then
        thenStreamShouldBeAsExpected();
        thenStreamDataShouldBeAsExpected("data");
        thenChecksumInHeadShouldBeAsExpected("Om6weQ85rIfJTzhWst0sXREOaBFgImGpqSPTuyOtyLc=");
    }

    @Test
    public void testFetchPartForMultipartDownloadWithHeaders() throws Exception {

        // Given
        givenPartFetcher();
        Map<String, String> headers = givenHeader("foo", "bar");
        givenDownloadRequest("foo", "bar", "http://foobar.com/v1/some/resource", headers);
        givenPartRequestForMultiPartDownload(2);
        givenCallToGetSessionBearerReturns("session-token");
        givenCallToGetDatasetBearerReturns("foo", "bar", "dataset-token");
        givenResponseHeadersForMultipart(
                "version-1", "Om6weQ85rIfJTzhWst0sXREOaBFgImGpqSPTuyOtyLc=", "5", "23", "bytes 0-4/23");
        givenCallToGetInputStreamReturnsSuccess(
                "data",
                "http://foobar.com/v1/some/resource/operationType/download?downloadPartNumber=2",
                "session-token",
                "dataset-token", headers);

        // when
        whenFetchIsInvoked();

        // Then
        thenStreamShouldBeAsExpected();
        thenStreamDataShouldBeAsExpected("data");
        thenChecksumInHeadShouldBeAsExpected("Om6weQ85rIfJTzhWst0sXREOaBFgImGpqSPTuyOtyLc=");
    }

    @Test
    public void testFetchPartForHead() throws Exception {

        // Given
        givenPartFetcher();
        givenDownloadRequest("foo", "bar", "http://foobar.com/v1/some/resource");
        givenPartRequestForHead();
        givenCallToGetSessionBearerReturns("session-token");
        givenCallToGetDatasetBearerReturns("foo", "bar", "dataset-token");
        givenResponseHeadersForHead("version-1", "Om6weQ85rIfJTzhWst0sXREOaBFgImGpqSPTuyOtyLc=", "5", "23");
        givenCallToGetInputStreamReturnsSuccess(
                "data", "http://foobar.com/v1/some/resource/operationType/download", "session-token", "dataset-token");

        // when
        whenFetchIsInvoked();

        // Then
        thenStreamShouldBeAsExpected();
        thenStreamDataShouldBeAsExpected("data");
        thenChecksumInHeadShouldBeAsExpected("Om6weQ85rIfJTzhWst0sXREOaBFgImGpqSPTuyOtyLc=");
    }

    @Test
    public void testFetchThrowsExceptionWhenHttpResponseInError() {

        // Given
        givenPartFetcher();
        givenDownloadRequest("foo", "bar", "http://foobar.com/v1/some/resource");
        givenPartRequestForMultiPartDownload(2);
        givenCallToGetSessionBearerReturns("session-token");
        givenCallToGetDatasetBearerReturns("foo", "bar", "dataset-token");
        givenCallToGetInputStreamReturnsFailure(
                "bad-data",
                "http://foobar.com/v1/some/resource/operationType/download?downloadPartNumber=2",
                "session-token",
                "dataset-token");

        // when
        whenFetchIsInvokedWithException();

        // Then
        thenTheExceptionShouldBeAsExpected("bad-data", 400);
    }

    private Map<String, String> givenHeader(String key, String value){
        return new HashMap<String, String>(){{put(key, value);}};
    }

    private void givenPartRequestForSinglePartDownload(String checksum) {
        pr = PartRequest.builder()
                .partNo(1)
                .downloadRequest(dr)
                .head(Head.builder().checksum(checksum).build())
                .build();
    }

    private void givenPartRequestForMultiPartDownload(int partNo) {
        pr = PartRequest.builder().partNo(partNo).downloadRequest(dr).build();
    }

    private void givenPartRequestForHead() {
        pr = PartRequest.builder().partNo(0).downloadRequest(dr).build();
    }

    private void thenChecksumInHeadShouldBeAsExpected(String checksum) {
        assertThat(actual.getHead().getChecksum(), equalTo(checksum));
    }

    private void thenTheExceptionShouldBeAsExpected(String expected, int statusCode) {
        assertThat(exception.getMessage(), equalTo(expected));
        assertThat(exception.getResponseCode(), equalTo(statusCode));
    }

    private void thenStreamDataShouldBeAsExpected(String data) throws Exception {
        byte[] bytes = new byte[data.getBytes().length];
        actual.getContent().read(bytes);

        assertThat(bytes, equalTo(data.getBytes()));
    }

    private void thenStreamShouldBeAsExpected() {
        assertThat(this.actual.getContent(), instanceOf(IntegrityCheckingInputStream.class));
    }

    private void whenFetchIsInvoked() {
        actual = testee.fetch(pr);
    }

    private void whenFetchIsInvokedWithException() {
        exception = Assertions.assertThrows(APICallException.class, () -> testee.fetch(pr));
    }

    private void givenCallToGetInputStreamReturnsSuccess(
            String data, String path, String sessionToken, String datasetToken) {
        givenCallToGetInputStream(data, path, givenAuthHeaders(sessionToken, datasetToken));
    }

    private void givenCallToGetInputStreamReturnsSuccess(
            String data, String path, String sessionToken, String datasetToken, Map<String, String> headers) {
        givenCallToGetInputStream(data, path, givenAuthHeaders(sessionToken, datasetToken, headers));
    }

    private void givenCallToGetInputStreamForSinglePartDownload(
            String data, String path, String sessionToken, String datasetToken) {
        givenCallToGetInputStream(data, path, givenAuthHeaders(sessionToken, datasetToken));
    }

    private void givenCallToGetInputStreamForSinglePartDownload(
            String data, String path, String sessionToken, String datasetToken, Map<String, String> headers) {
        givenCallToGetInputStream(data, path, givenAuthHeaders(sessionToken, datasetToken, headers));
    }

    private void givenCallToGetInputStream(String data, String path, Map<String, String> requestHeaders) {
        httpResponse = HttpResponse.<InputStream>builder()
                .body(new ByteArrayInputStream(data.getBytes()))
                .headers(responseHeaders)
                .statusCode(200)
                .build();

        Mockito.when(client.getInputStream(path, requestHeaders)).thenReturn(httpResponse);
    }

    private void givenCallToGetInputStreamReturnsFailure(
            String error, String path, String sessionToken, String datasetToken) {

        Map<String, String> requestHeaders = givenAuthHeaders(sessionToken, datasetToken);

        badResponse = HttpResponse.builder()
                .body("{\"error\":\"" + error + "\"}")
                .statusCode(400)
                .build();

        Mockito.when(client.getInputStream(path, requestHeaders)).thenReturn(badResponse);
    }

    private void givenResponseHeadersForHead(String version, String checksum, String partCount, String contentLength) {
        addResponseHeader("x-jpmc-version-id", version);
        addResponseHeader("x-jpmc-checksum-sha256", checksum);
        addResponseHeader("x-jpmc-mp-parts-count", partCount);
        addResponseHeader("Content-Length", contentLength);
    }

    private void givenResponseHeadersForMultipart(
            String version, String checksum, String partCount, String contentLength, String contentRange) {
        addResponseHeader("x-jpmc-version-id", version);
        addResponseHeader("x-jpmc-checksum-sha256", checksum);
        addResponseHeader("x-jpmc-mp-parts-count", partCount);
        addResponseHeader("Content-Length", contentLength);
        addResponseHeader("Content-Range", contentRange);
    }

    private void givenSinglePartResponseHeaders(String contentLength) {
        addResponseHeader("Content-Length", contentLength);
    }

    private void addResponseHeader(String key, String value) {
        List<String> values = new ArrayList<>();
        values.add(value);
        responseHeaders.put(key, values);
    }

    @NotNull
    private static Map<String, String> givenAuthHeaders(String sessionToken, String datasetToken) {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + sessionToken);
        requestHeaders.put("Fusion-Authorization", "Bearer " + datasetToken);
        return requestHeaders;
    }

    private static Map<String, String> givenAuthHeaders(String sessionToken, String datasetToken, Map<String, String> requestHeaders) {
        requestHeaders.put("Authorization", "Bearer " + sessionToken);
        requestHeaders.put("Fusion-Authorization", "Bearer " + datasetToken);
        return requestHeaders;
    }

    private void givenCallToGetDatasetBearerReturns(String catalog, String dataset, String token) {
        Mockito.when(credentials.getDatasetBearerToken(catalog, dataset)).thenReturn(token);
    }

    private void givenCallToGetSessionBearerReturns(String token) {
        Mockito.when(credentials.getSessionBearerToken()).thenReturn(token);
    }

    private void givenPartFetcher() {
        testee = PartFetcher.builder().client(client).credentials(credentials).build();
    }

    private void givenDownloadRequest(String catalog, String dataset, String apiPath) {
        dr = DownloadRequest.builder()
                .catalog(catalog)
                .dataset(dataset)
                .apiPath(apiPath)
                .build();
    }

    private void givenDownloadRequest(String catalog, String dataset, String apiPath, Map<String, String> headers) {
        dr = DownloadRequest.builder()
                .catalog(catalog)
                .dataset(dataset)
                .apiPath(apiPath)
                .headers(headers)
                .build();
    }
}
