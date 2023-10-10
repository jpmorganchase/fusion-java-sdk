package io.github.jpmorganchase.fusion.api.request;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.response.GetPartResponse;
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

    PartFetcher testee;

    GetPartResponse actual;

    HttpResponse<InputStream> httpResponse;
    HttpResponse badResponse;

    APICallException exception;

    @Test
    public void testFetchReturnsStreamAsExpected() throws Exception {

        // Given
        givenDownloadRequest("foo", "bar");
        givenPartFetcher();
        givenCallToGetSessionBearerReturns("session-token");
        givenCallToGetDatasetBearerReturns("foo", "bar", "dataset-token");
        givenCallToGetInputStreamReturnsSuccess(
                "data", "Om6weQ85rIfJTzhWst0sXREOaBFgImGpqSPTuyOtyLc=", "foo/bar/1", "session-token", "dataset-token");

        // when
        whenFetchIsInvoked("foo/bar/1");

        // Then
        thenStreamShouldBeAsExpected();
        thenStreamDataShouldBeAsExpected("data");
        thenChecksumInHeadShouldBeAsExpected("Om6weQ85rIfJTzhWst0sXREOaBFgImGpqSPTuyOtyLc=");
    }

    @Test
    public void testFetchThrowsExceptionWhenHttpResponseInError() {

        // Given
        givenDownloadRequest("foo", "bar");
        givenPartFetcher();
        givenCallToGetSessionBearerReturns("session-token");
        givenCallToGetDatasetBearerReturns("foo", "bar", "dataset-token");
        givenCallToGetInputStreamReturnsFailure("bad-data", "foo/bar/1", "session-token", "dataset-token");

        // when
        whenFetchIsInvokedWithException("foo/bar/1");

        // Then
        thenTheExceptionShouldBeAsExpected("bad-data", 400);
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

    private void whenFetchIsInvoked(String path) {
        actual = testee.fetch(path);
    }

    private void whenFetchIsInvokedWithException(String path) {
        exception = Assertions.assertThrows(APICallException.class, () -> testee.fetch(path));
    }

    private void givenCallToGetInputStreamReturnsSuccess(
            String data, String checksum, String path, String sessionToken, String datasetToken) {

        Map<String, String> requestHeaders = givenRequestHeaders(sessionToken, datasetToken);
        Map<String, List<String>> responseHeaders = givenResponseHeaders(checksum);

        httpResponse = HttpResponse.<InputStream>builder()
                .body(new ByteArrayInputStream(data.getBytes()))
                .headers(responseHeaders)
                .statusCode(200)
                .build();

        Mockito.when(client.getInputStream(path, requestHeaders)).thenReturn(httpResponse);
    }

    private void givenCallToGetInputStreamReturnsFailure(
            String error, String path, String sessionToken, String datasetToken) {

        Map<String, String> requestHeaders = givenRequestHeaders(sessionToken, datasetToken);

        badResponse = HttpResponse.builder()
                .body("{\"error\":\"" + error + "\"}")
                .statusCode(400)
                .build();

        Mockito.when(client.getInputStream(path, requestHeaders)).thenReturn(badResponse);
    }

    @NotNull
    private static Map<String, List<String>> givenResponseHeaders(String checksum) {
        Map<String, List<String>> responseHeaders = new HashMap<>();
        List<String> checksumValues = new ArrayList<>();
        checksumValues.add(checksum);
        responseHeaders.put("x-jpmc-checksum-sha256", checksumValues);
        return responseHeaders;
    }

    @NotNull
    private static Map<String, String> givenRequestHeaders(String sessionToken, String datasetToken) {
        Map<String, String> requestHeaders = new HashMap<>();
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
        testee = PartFetcher.builder()
                .client(client)
                .credentials(credentials)
                .request(dr)
                .build();
    }

    private void givenDownloadRequest(String catalog, String dataset) {
        dr = DownloadRequest.builder().catalog(catalog).dataset(dataset).build();
    }
}
