package io.github.jpmorganchase.fusion.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.exception.FileDownloadException;
import io.github.jpmorganchase.fusion.api.request.DownloadRequest;
import io.github.jpmorganchase.fusion.api.response.GetPartResponse;
import io.github.jpmorganchase.fusion.api.response.Head;
import io.github.jpmorganchase.fusion.digest.DigestProducer;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.oauth.provider.DatasetTokenProvider;
import io.github.jpmorganchase.fusion.oauth.provider.SessionTokenProvider;
import java.io.*;
import java.util.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("ALL")
@ExtendWith(MockitoExtension.class)
class FusionAPIDownloaderTest {

    private FusionAPIDownloader apiDownloader;

    @Mock
    private Client client;

    @Mock
    private SessionTokenProvider sessionTokenProvider;

    @Mock
    private DatasetTokenProvider datasetTokenProvider;

    @Mock
    private DigestProducer digestProducer;

    private String apiPath;

    private String catalog;

    private String dataset;

    private String folder;

    private String fileName;

    private DownloadRequest downloadRequest;

    private Map<String, String> requestHeaders = new HashMap<>();

    private byte[] downloadBody;

    private Map<String, List<String>> responseHeaders = new HashMap<>();

    private InputStream responseStream;

    private Throwable throwable;

    private String responseBody;

    private String actualResponse;

    private Head head;

    private GetPartResponse getPartResponse;

    @AfterEach
    void cleanUp() {
        finallyDeleteFile();
    }

    @Test
    void successfulSinglePartDownloadToFile() throws Exception {
        // given
        givenFusionApiManager();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenFilePath("downloads/common-API_TEST-20230319.csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "API_TEST", "dataset-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");
        givenDownloadBody("A,B,C\n1,2,3");
        givenResponseHeader("Content-Type", "text/csv");
        givenResponseHeader("Content-Disposition", "attachment; filename=test-testFile.csv");
        givenCallToClientToGetHeadReturns("version-123", null, "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=", "5");
        givenCallToClientToGetInputStreamIsSuccessfully();

        // when
        whenFusionApiManagerIsCalledToDownloadFileToPath();

        // finally
        thenTheFileShouldMatchExpected();
    }

    @Test
    void singlePartDownloadToFileFailToGetDistribution() throws Exception {
        // given
        givenFusionApiManager();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenFilePath("downloads/common-API_TEST-20230319.csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "API_TEST", "dataset-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");

        givenCallToClientToGetHeadReturns("version-123", null, "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=", "5");
        givenCallToClientToGetInputStreamFails(500);

        // when
        whenFusionApiManagerIsCalledToDownloadFileToPathAndExceptionIsExcepted(APICallException.class);

        // finally
        thenAPICallExceptionShouldMatch(500);
    }

    @Test
    void singlePartDownloadToFileFailsToWrite() throws Exception {
        // given
        givenFusionApiManager();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenFilePath("downloads///common/-API_TEST-20230319.csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "API_TEST", "dataset-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");
        givenDownloadBody("A,B,C\n1,2,3");
        givenResponseHeader("Content-Type", "text/csv");
        givenResponseHeader("Content-Disposition", "attachment; filename=test-testFile.csv");
        givenCallToClientToGetHeadReturns("version-123", null, "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=", "5");
        givenCallToClientToGetInputStreamIsSuccessfully();

        // when
        whenFusionApiManagerIsCalledToDownloadFileToPathAndExceptionIsExcepted(FileDownloadException.class);

        // finally
        thenExceptionMessageShouldMatch("Problem encountered attempting to write downloaded distribution to file");
    }

    @Test
    void successfulMultiPartDownloadToFile() throws Exception {
        // given
        givenFusionApiManager();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenFilePath("downloads/common-API_TEST-20230319.csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "API_TEST", "dataset-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");

        givenCallToClientToGetHeadReturns("a1", "5", "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=-5", "23");

        givenCallToClientToGetPartReturns("A,B,C", "1", "a1", "c1", "4", "23", "bytes 0-4/23");
        givenCallToClientToGetPartReturns("\r1,2,", "2", "a1", "c2", "4", "23", "bytes 5-9/23");
        givenCallToClientToGetPartReturns("3\r4,5", "3", "a1", "c3", "4", "23", "bytes 10-14/23");
        givenCallToClientToGetPartReturns(",6\r7,", "4", "a1", "c4", "4", "23", "bytes 15-19/23");
        givenCallToClientToGetPartReturns("8,9", "5", "a1", "c5", "4", "23", "bytes 20-23/23");
        givenDownloadBody("A,B,C\r1,2,3\r4,5,6\r7,8,9");

        // When
        whenFusionApiManagerIsCalledToDownloadFileToPath();

        // then
        thenTheFileShouldMatchExpected();
    }

    @Test
    void multiPartDownloadToFileFailsToGetHead() throws Exception {
        // given
        givenFusionApiManager();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenFilePath("downloads/common-API_TEST-20230319.csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "API_TEST", "dataset-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");

        givenCallToClientToGetHeadFails(500);

        // When
        whenFusionApiManagerIsCalledToDownloadFileToPathAndExceptionIsExcepted(APICallException.class);

        // then
        thenAPICallExceptionShouldMatch(500);
    }

    @Test
    void multiPartDownloadToFileFailsToGetPart() throws Exception {
        // given
        givenFusionApiManager();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenFilePath("downloads/common-API_TEST-20230319.csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "API_TEST", "dataset-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");

        givenCallToClientToGetHeadReturns("a1", "5", "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=-5", "23");

        givenCallToClientToGetPartReturns("A,B,C", "1", "a1", "c1", "4", "23", "bytes 0-4/23");
        givenCallToClientToGetPartReturns("\r1,2,", "2", "a1", "c2", "4", "23", "bytes 5-9/23");
        givenCallToClientToGetPartFails("3", 504);
        givenCallToClientToGetPartReturns(",6\r7,", "4", "a1", "c4", "4", "23", "bytes 15-19/23");
        givenCallToClientToGetPartReturns("8,9", "5", "a1", "c5", "4", "23", "bytes 20-23/23");
        givenDownloadBody("A,B,C\r1,2,3\r4,5,6\r7,8,9");

        // When
        whenFusionApiManagerIsCalledToDownloadFileToPathAndExceptionIsExcepted(APICallException.class);

        // then
        thenAPICallExceptionShouldMatch(504);
    }

    @Test
    void multiPartDownloadToFileFailsToWriteToFile() throws Exception {
        // given
        givenFusionApiManager();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenFilePath("downloads/common-API_TEST-20230319.csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "API_TEST", "dataset-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");

        givenCallToClientToGetHeadReturns("a1", "5", "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=-5", "23");

        givenCallToClientToGetPartReturns("A,B,C", "1", "a1", "c1", "4", "23", "bytes 0-4/23");
        givenCallToClientToGetPartReturns("\r1,2,", "2", "a1", "c2", "4", "23", "bytes 5-9/23");
        givenCallToClientToGetPartReturns("3\r4,5", "3", "a1", "c3", "4", "23", "bytes -1-14/23");
        givenCallToClientToGetPartReturns(",6\r7,", "4", "a1", "c4", "4", "23", "bytes 15-19/23");
        givenCallToClientToGetPartReturns("8,9", "5", "a1", "c5", "4", "23", "bytes 20-23/23");
        givenDownloadBody("A,B,C\r1,2,3\r4,5,6\r7,8,9");

        // When
        whenFusionApiManagerIsCalledToDownloadFileToPathAndExceptionIsExcepted(FileDownloadException.class);

        // then
        thenExceptionMessageShouldMatch("Problem encountered attempting to write downloaded distribution to file");
    }

    private void thenExceptionMessageShouldMatch(String expected) {
        assertThat((throwable).getMessage(), is(equalTo(expected)));
    }

    private void thenAPICallExceptionShouldMatch(int expected) {
        assertThat(((APICallException) throwable).getResponseCode(), is(equalTo(expected)));
    }

    @Test
    void successfulSinglePartDownloadToStream() throws Exception {
        // given
        givenFusionApiManager();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "API_TEST", "dataset-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");
        givenDownloadBody("A,B,C\n1,2,3");
        givenResponseHeader("Content-Type", "text/csv");
        givenResponseHeader("Content-Disposition", "attachment; filename=test-testFile.csv");
        givenCallToClientToGetHeadReturns("version-123", null, "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=", "5");
        givenCallToClientToGetInputStreamIsSuccessfully();

        // when
        whenApiIsCalledToDownloadFileAsStream();

        // then
        thenTheDownloadBodyShouldMatchExpected();
    }

    @Test
    void singlePartDownloadToStreamFailsToGetDistribution() throws Exception {
        // given
        givenFusionApiManager();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "API_TEST", "dataset-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");
        givenDownloadBody("A,B,C\n1,2,3");
        givenResponseHeader("Content-Type", "text/csv");
        givenResponseHeader("Content-Disposition", "attachment; filename=test-testFile.csv");
        givenCallToClientToGetHeadReturns("version-123", null, "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=", "5");
        givenCallToClientToGetInputStreamFails(504);

        // when
        whenFusionApiManagerIsCalledToDownloadFileToPathAndExceptionIsExcepted(APICallException.class);

        // then
        thenAPICallExceptionShouldMatch(504);
    }

    @Test
    void successfulMultiPartDownloadToStream() throws Exception {
        // given
        givenFusionApiManager();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenFolder("downloads");
        givenFilePath("common-API_TEST-20230319.csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "API_TEST", "dataset-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");

        givenCallToClientToGetHeadReturns("a1", "5", "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=-5", "23");
        givenCallToClientToGetPartReturns(
                "A,B,C", "1", "a1", "KPD9WTOuUoQrDwpugLaHblJS+OdUnXaML3YWXla28Rg=", "4", "23", "bytes 0-4/23");
        givenCallToClientToGetPartReturns(
                "\r1,2,", "2", "a1", "KyQR+rbMkYVdfMHW+tHYfTOmpszv9gHWVn1Ec9yj7lA=", "4", "23", "bytes 5-9/23");
        givenCallToClientToGetPartReturns(
                "3\r4,5", "3", "a1", "qMnQo29rnj1iA37dWzSBFCKSctoJe8AX5mgmexxvh4A=", "4", "23", "bytes 10-14/23");
        givenCallToClientToGetPartReturns(
                ",6\r7,", "4", "a1", "RjKiTp8KSSXM64sjp5uHtPXF/uwjh8VNVaCvgDAwrkA=", "4", "23", "bytes 15-19/23");
        givenCallToClientToGetPartReturns(
                "8,9", "5", "a1", "GI3Dn4384xRI1aZfvWIpkSDzDQbYwKaK4yCy3oBZm/U=", "4", "23", "bytes 20-23/23");
        givenDownloadBody("A,B,C\r1,2,3\r4,5,6\r7,8,9");

        // When
        whenApiIsCalledToDownloadFileAsStream();

        // then
        thenTheDownloadBodyShouldMatchExpected();
    }

    @Test
    void multiPartDownloadToStreamFailsToGetPart() throws Exception {
        // given
        givenFusionApiManager();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenFolder("downloads");
        givenFilePath("common-API_TEST-20230319.csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "API_TEST", "dataset-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");

        givenCallToClientToGetHeadReturns("a1", "5", "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=-5", "23");
        givenCallToClientToGetPartReturns("A,B,C", "1", "a1", "c1", "4", "23", "bytes 0-4/23");
        givenCallToClientToGetPartFails("2", 400);
        givenCallToClientToGetPartReturns("3\r4,5", "3", "a1", "c3", "4", "23", "bytes 10-14/23");
        givenCallToClientToGetPartReturns(",6\r7,", "4", "a1", "c4", "4", "23", "bytes 15-19/23");
        givenCallToClientToGetPartReturns("8,9", "5", "a1", "c5", "4", "23", "bytes 20-23/23");
        givenDownloadBody("A,B,C\r1,2,3\r4,5,6\r7,8,9");

        // When
        whenApiIsCalledToDownloadFileAsStreamFailsWith(APICallException.class);

        // then
        thenAPICallExceptionShouldMatch(400);
    }

    @Test
    void successfulCallToGetHead() {
        // given
        givenFusionApiManager();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "API_TEST", "dataset-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");

        givenCallToClientToGetHeadReturns("version-123", "3", "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=-3", "15");

        // when
        whenFusionApiDownloaderIsCalledForHead();

        // then
        thenChecksumOnHeadShouldEqual("SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=");
        thenPartCountOnHeadShouldEqual(3);
        thenVersionOnHeadShouldEqual("version-123");
    }

    @Test
    void successfulCallToGetPartHead() {

        // given
        givenFusionApiManager();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenFolder("downloads");
        givenFilePath("common-API_TEST-20230319.csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "API_TEST", "dataset-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");
        givenDownloadRequest(false);

        givenCallToClientToGetPartReturns("A,B,C", "2", "a1", "c1", "3", "17", "bytes 10-15/17");

        // when
        whenFusionApiDownloaderIsCalledToGetPart(2);

        // then
        thenContentShouldEqual("A,B,C".getBytes());
        thenStartPosShouldEqual(10L);
    }

    @Test
    void successfulCallToPerformMultiPartDownload() throws Exception {
        // given
        givenFusionApiManager();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv");
        givenCatalog("common");
        givenDataset("API_TEST");
        givenFilePath("downloads/common-API_TEST-20230319.csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "API_TEST", "dataset-token");
        givenRequestHeader("Authorization", "Bearer my-token");
        givenRequestHeader("Fusion-Authorization", "Bearer dataset-token");
        givenDownloadRequest(false);
        givenHead("a1", "c", 23L, 5);

        givenCallToClientToGetPartReturns("A,B,C", "1", "a1", "c1", "4", "23", "bytes 0-4/23");
        givenCallToClientToGetPartReturns("\r1,2,", "2", "a1", "c2", "4", "23", "bytes 5-9/23");
        givenCallToClientToGetPartReturns("3\r4,5", "3", "a1", "c3", "4", "23", "bytes 10-14/23");
        givenCallToClientToGetPartReturns(",6\r7,", "4", "a1", "c4", "4", "23", "bytes 15-19/23");
        givenCallToClientToGetPartReturns("8,9", "5", "a1", "c5", "4", "23", "bytes 20-23/23");
        givenDownloadBody("A,B,C\r1,2,3\r4,5,6\r7,8,9");

        // When
        whenFusionApiDownloaderIsCalledToPerformMultiPartDownload();

        // then
        thenTheFileShouldMatchExpected();
    }

    private void whenFusionApiDownloaderIsCalledToPerformMultiPartDownload() {
        apiDownloader.performMultiPartDownloadToFile(downloadRequest, head);
    }

    private void givenHead(String version, String checksum, Long length, int partCount) {
        head = Head.builder()
                .version(version)
                .checksum(checksum)
                .contentLength(length)
                .partCount(partCount)
                .build();
    }

    private void givenCallToClientToGetPartReturns(
            String content,
            String partNumber,
            String version,
            String checksum,
            String partCount,
            String contentLength,
            String contentRange) {

        Map<String, List<String>> responseHeaders = new HashMap<>();
        givenResponseHeader(responseHeaders, "x-jpmc-version-id", version);
        givenResponseHeader(responseHeaders, "x-jpmc-checksum-sha256", checksum);
        givenResponseHeader(responseHeaders, "x-jpmc-mp-parts-count", partCount);
        givenResponseHeader(responseHeaders, "Content-Length", contentLength);
        givenResponseHeader(responseHeaders, "Content-Range", contentRange);

        when(client.getInputStream(
                        eq(apiPath + "/operationType/download?downloadPartNumber=" + partNumber), eq(requestHeaders)))
                .thenReturn(HttpResponse.<InputStream>builder()
                        .statusCode(200)
                        .headers(responseHeaders)
                        .body(new ByteArrayInputStream(content.getBytes()))
                        .build());
    }

    private void givenCallToClientToGetPartReturnsBadStream(
            String content,
            String partNumber,
            String version,
            String checksum,
            String partCount,
            String contentLength,
            String contentRange) {

        Map<String, List<String>> responseHeaders = new HashMap<>();
        givenResponseHeader(responseHeaders, "x-jpmc-version-id", version);
        givenResponseHeader(responseHeaders, "x-jpmc-checksum-sha256", checksum);
        givenResponseHeader(responseHeaders, "x-jpmc-mp-parts-count", partCount);
        givenResponseHeader(responseHeaders, "Content-Length", contentLength);
        givenResponseHeader(responseHeaders, "Content-Range", contentRange);

        when(client.getInputStream(
                        eq(apiPath + "/operationType/download?downloadPartNumber=" + partNumber), eq(requestHeaders)))
                .thenReturn(HttpResponse.<InputStream>builder()
                        .statusCode(200)
                        .headers(responseHeaders)
                        .body(new BrokenInputStream())
                        .build());
    }

    private void givenCallToClientToGetPartFails(String partNumber, int failureStatus) {

        when(client.getInputStream(
                        eq(apiPath + "/operationType/download?downloadPartNumber=" + partNumber), eq(requestHeaders)))
                .thenReturn(HttpResponse.<InputStream>builder()
                        .statusCode(failureStatus)
                        .build());
    }

    private void thenStartPosShouldEqual(Long expected) {
        assertThat(getPartResponse.getHead().getContentRange().getStart(), is(equalTo(expected)));
    }

    @SneakyThrows
    private void thenContentShouldEqual(byte[] expected) {

        byte[] buffer = new byte[8192];
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        int read;
        while ((read = getPartResponse.getContent().read(buffer)) != -1) {
            actual.write(buffer, 0, read);
        }

        assertThat(actual.toByteArray(), is(equalTo(expected)));
    }

    private void whenFusionApiDownloaderIsCalledToGetPart(int partNumber) {
        getPartResponse = apiDownloader.callToAPIToGetPart(downloadRequest, partNumber);
    }

    private void givenDownloadRequest(boolean isDownloadToStream) {
        this.downloadRequest = DownloadRequest.builder()
                .apiPath(apiPath)
                .catalog(catalog)
                .dataset(dataset)
                .filePath(fileName)
                .isDownloadToStream(isDownloadToStream)
                .build();
    }

    private void givenFilePath(String fileName) {
        this.fileName = fileName;
    }

    private void givenFolder(String folder) {
        this.folder = folder;
    }

    private void thenVersionOnHeadShouldEqual(String expected) {
        assertThat(head.getVersion(), is(equalTo(expected)));
    }

    private void thenPartCountOnHeadShouldEqual(int expected) {
        assertThat(head.getPartCount(), is(equalTo(expected)));
    }

    private void thenChecksumOnHeadShouldEqual(String expected) {
        assertThat(head.getChecksum(), is(equalTo(expected)));
    }

    private void whenFusionApiDownloaderIsCalledForHead() {

        DownloadRequest dr = DownloadRequest.builder()
                .catalog(catalog)
                .dataset(dataset)
                .apiPath(apiPath)
                .build();

        head = this.apiDownloader.callAPIToGetHead(dr);
    }

    private void givenCallToClientToGetHeadReturns(
            String version, String partCount, String checksum, String contentLength) {

        Map<String, List<String>> responseHeaders = new HashMap<>();
        responseHeaders.put("x-jpmc-version-id", Collections.singletonList(version));
        responseHeaders.put("x-jpmc-checksum-sha256", Collections.singletonList(checksum));
        responseHeaders.put("Content-Length", Collections.singletonList(contentLength));
        if (Objects.nonNull(partCount))
            responseHeaders.put("x-jpmc-mp-parts-count", Collections.singletonList(partCount));

        when(client.getInputStream(eq(apiPath + "/operationType/download"), eq(requestHeaders)))
                .thenReturn(HttpResponse.<InputStream>builder()
                        .statusCode(200)
                        .headers(responseHeaders)
                        .build());
    }

    private void givenCallToClientToGetHeadFails(int failureStatusCode) {
        when(client.getInputStream(eq(apiPath + "/operationType/download"), eq(requestHeaders)))
                .thenReturn(HttpResponse.<InputStream>builder()
                        .statusCode(failureStatusCode)
                        .build());
    }

    private void givenDataset(String dataset) {
        this.dataset = dataset;
    }

    private void givenCatalog(String catalog) {
        this.catalog = catalog;
    }

    private void givenDatasetBearerToken(String catalog, String dataset, String token) {
        given(datasetTokenProvider.getDatasetBearerToken(catalog, dataset)).willReturn(token);
    }

    private void finallyDeleteFile() {
        if (Objects.nonNull(fileName)) {
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void thenTheFileShouldMatchExpected() throws Exception {

        File file = new File(fileName);

        byte[] outputBytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(outputBytes);
        }

        assertThat(outputBytes, is(equalTo(downloadBody)));
    }

    private void whenFusionApiManagerIsCalledToDownloadFileToPath() {
        apiDownloader.callAPIFileDownload(apiPath, fileName, catalog, dataset);
    }

    private void whenFusionApiManagerIsCalledToDownloadFileToPathAndExceptionIsExcepted(
            Class<? extends Throwable> exception) {
        throwable = Assertions.assertThrows(
                exception, () -> apiDownloader.callAPIFileDownload(apiPath, fileName, catalog, dataset));
    }

    private void thenTheDownloadBodyShouldMatchExpected() throws Exception {
        byte[] outputBytes = new byte[(int) downloadBody.length];
        StringBuilder sb = new StringBuilder();
        int bytesRead = 0;

        while ((bytesRead = responseStream.read(outputBytes)) != -1) {}

        assertThat(outputBytes, is(equalTo(downloadBody)));
    }

    private void whenApiIsCalledToDownloadFileAsStream() {
        responseStream = apiDownloader.callAPIFileDownload(apiPath, catalog, dataset);
    }

    private void whenApiIsCalledToDownloadFileAsStreamFailsWith(Class<? extends Throwable> expected) {
        throwable =
                Assertions.assertThrows(expected, () -> apiDownloader.callAPIFileDownload(apiPath, catalog, dataset));
    }

    private void givenCallToClientToGetInputStreamIsSuccessfully() {
        HttpResponse<InputStream> expectedHttpResponse = HttpResponse.<InputStream>builder()
                .statusCode(200)
                .body(new ByteArrayInputStream(downloadBody))
                .headers(responseHeaders)
                .build();

        when(client.getInputStream(apiPath, requestHeaders)).thenReturn(expectedHttpResponse);
    }

    private void givenCallToClientToGetInputStreamFails(int status) {
        HttpResponse<InputStream> expectedHttpResponse =
                HttpResponse.<InputStream>builder().statusCode(status).build();

        when(client.getInputStream(apiPath, requestHeaders)).thenReturn(expectedHttpResponse);
    }

    private void givenSessionBearerToken(String token) {
        given(sessionTokenProvider.getSessionBearerToken()).willReturn(token);
    }

    private void givenApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    private void givenResponseHeader(Map<String, List<String>> headers, String headerKey, String headerValue) {
        headers.put(headerKey, Lists.newArrayList(headerValue));
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

        apiDownloader = FusionAPIDownloader.builder()
                .httpClient(client)
                .sessionTokenProvider(sessionTokenProvider)
                .datasetTokenProvider(datasetTokenProvider)
                .digestProducer(digestProducer)
                .downloadThreadPoolSize(Runtime.getRuntime().availableProcessors())
                .build();
    }

    private class BrokenInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            throw new IOException("Unable to read from InputStream");
        }
    }
}
