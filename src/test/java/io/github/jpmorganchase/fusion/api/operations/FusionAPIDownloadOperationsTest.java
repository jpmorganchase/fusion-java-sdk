package io.github.jpmorganchase.fusion.api.operations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.exception.FileDownloadException;
import io.github.jpmorganchase.fusion.api.request.DownloadRequest;
import io.github.jpmorganchase.fusion.api.request.PartFetcher;
import io.github.jpmorganchase.fusion.api.request.PartRequest;
import io.github.jpmorganchase.fusion.api.response.ContentRange;
import io.github.jpmorganchase.fusion.api.response.GetPartResponse;
import io.github.jpmorganchase.fusion.api.response.Head;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@SuppressWarnings({"SameParameterValue", "resource"})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FusionAPIDownloadOperationsTest {

    private FusionAPIDownloadOperations apiDownloader;

    @Mock
    PartFetcher partFetcher;

    private DownloadRequest downloadRequest;

    private byte[] downloadBody;

    private InputStream responseStream;

    private Throwable throwable;

    private Head head;

    @AfterEach
    void cleanUp() {
        finallyDeleteFile();
    }

    @Test
    void successfulSinglePartDownloadToFile() throws Exception {
        // given
        givenFusionApiManager();
        givenDownloadBody("A,B,C\n1,2,3");
        givenDownloadRequestForFile(
                "common",
                "API_TEST",
                "downloads/common-API_TEST-20230319.csv",
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv",
                Collections.singletonMap("fusion-e2e", "rootId/Id"));
        givenCallToPartFetcherToGetHeadReturns("version-123", 1, "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", 5);
        givenCallToPartFetcherForSinglePartReturns("A,B,C\n1,2,3");

        // when
        whenFusionApiManagerIsCalledToDownloadFileToPath();

        // finally
        thenTheFileShouldMatchExpected();
    }

    @Test
    void singlePartDownloadToFileFailToGetDistribution() throws Exception {
        // given
        givenFusionApiManager();

        givenDownloadRequestForFile(
                "common",
                "API_TEST",
                "downloads/common-API_TEST-20230319.csv",
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv",
                Collections.singletonMap("fusion-e2e", "rootId/Id"));
        givenCallToPartFetcherToGetHeadReturns("version-123", 1, "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=", 5);
        givenCallToPartFetcherForSinglePartFails(500);

        // when
        whenFusionApiManagerIsCalledToDownloadFileToPathAndExceptionIsExcepted(APICallException.class);

        // finally
        thenAPICallExceptionShouldMatch(500);
    }

    @Test
    void singlePartDownloadToFileFailsToWrite() throws Exception {
        // given
        givenFusionApiManager();
        givenDownloadBody("A,B,C\n1,2,3");
        givenDownloadRequestForFile(
                "common",
                "API_TEST",
                "downloads///common/-API_TEST-20230319.csv",
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv",
                Collections.singletonMap("fusion-e2e", "rootId/Id"));

        givenCallToPartFetcherToGetHeadReturns("version-123", 1, "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=", 5);
        givenCallToPartFetcherForSinglePartReturns("A,B,C\n1,2,3");

        // when
        whenFusionApiManagerIsCalledToDownloadFileToPathAndExceptionIsExcepted(FileDownloadException.class);

        // finally
        thenExceptionMessageShouldMatch("Problem encountered attempting to write downloaded distribution to file");
    }

    @Test
    void successfulMultiPartDownloadToFile() throws Exception {
        // given
        givenFusionApiManager();
        givenDownloadRequestForFile(
                "common",
                "API_TEST",
                "downloads/common-API_TEST-20230319.csv",
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv",
                Collections.singletonMap("fusion-e2e", "rootId/Id"));
        givenCallToPartFetcherToGetHeadReturns("a1", 5, "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=-5", 23);

        givenCallToPartFetcherSuccess(
                "A,B,C", 1, "a1", "KPD9WTOuUoQrDwpugLaHblJS+OdUnXaML3YWXla28Rg=", 5, 23, 0, 4, 23);
        givenCallToPartFetcherSuccess(
                "\r1,2,", 2, "a1", "KyQR+rbMkYVdfMHW+tHYfTOmpszv9gHWVn1Ec9yj7lA=", 5, 23, 5, 9, 23);
        givenCallToPartFetcherSuccess(
                "3\r4,5", 3, "a1", "qMnQo29rnj1iA37dWzSBFCKSctoJe8AX5mgmexxvh4A=", 5, 23, 10, 14, 23);
        givenCallToPartFetcherSuccess(
                ",6\r7,", 4, "a1", "RjKiTp8KSSXM64sjp5uHtPXF/uwjh8VNVaCvgDAwrkA=", 5, 23, 15, 19, 23);
        givenCallToPartFetcherSuccess(
                "8,9", 5, "a1", "GI3Dn4384xRI1aZfvWIpkSDzDQbYwKaK4yCy3oBZm/U=", 5, 23, 20, 23, 23);
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

        givenDownloadRequestForFile(
                "commob",
                "API_TST",
                "downloads/common-API_TEST-20230319.csv",
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv",
                Collections.singletonMap("fusion-e2e", "rootId/Id"));
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

        givenDirectoryExists("downloads");
        givenDownloadRequestForFile(
                "common",
                "API_TEST",
                "downloads/common-API_TEST-20230319.csv",
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv",
                Collections.singletonMap("fusion-e2e", "rootId/Id"));
        givenCallToPartFetcherToGetHeadReturns("a1", 5, "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=-5", 23);

        givenCallToPartFetcherSuccess(
                "A,B,C", 1, "a1", "KPD9WTOuUoQrDwpugLaHblJS+OdUnXaML3YWXla28Rg=", 5, 23, 0, 4, 23);
        givenCallToPartFetcherSuccess(
                "\r1,2,", 2, "a1", "KyQR+rbMkYVdfMHW+tHYfTOmpszv9gHWVn1Ec9yj7lA=", 5, 23, 5, 9, 23);
        givenCallToClientToGetPartFails(3, 504);
        givenCallToPartFetcherSuccess(
                ",6\r7,", 4, "a1", "RjKiTp8KSSXM64sjp5uHtPXF/uwjh8VNVaCvgDAwrkA=", 5, 23, 15, 19, 23);
        givenCallToPartFetcherSuccess(
                "8,9", 5, "a1", "GI3Dn4384xRI1aZfvWIpkSDzDQbYwKaK4yCy3oBZm/U=", 5, 23, 20, 23, 23);
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
        givenDownloadRequestForFile(
                "common",
                "API_TEST",
                "downloads/common-API_TEST-20230319.csv",
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv",
                Collections.singletonMap("fusion-e2e", "rootId/Id"));
        givenCallToPartFetcherToGetHeadReturns("a1", 5, "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=-5", 23);

        givenCallToPartFetcherSuccess("A,B,C", 1, "a1", "c1", 5, 23, 0, 4, 23);
        givenCallToPartFetcherSuccess("\r1,2,", 2, "a1", "c2", 5, 23, 5, 9, 23);
        givenCallToPartFetcherSuccess("3\r4,5", 3, "a1", "c3", 5, 23, -1, 14, 23);
        givenCallToPartFetcherSuccess(",6\r7,", 4, "a1", "c4", 5, 23, 15, 19, 23);
        givenCallToPartFetcherSuccess("8,9", 5, "a1", "c5", 5, 23, 20, 23, 23);
        givenDownloadBody("A,B,C\r1,2,3\r4,5,6\r7,8,9");

        // When
        whenFusionApiManagerIsCalledToDownloadFileToPathAndExceptionIsExcepted(FileDownloadException.class);

        // then
        thenExceptionMessageShouldMatch("Problem encountered attempting to write downloaded distribution to file");
    }

    @Test
    void successfulSinglePartDownloadToStream() throws Exception {
        // given
        givenFusionApiManager();

        givenDownloadBody("A,B,C\n1,2,3");
        givenDownloadRequestForStream(
                "common",
                "API_TEST",
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv",
                Collections.singletonMap("fusion-e2e", "rootId/Id"));
        givenCallToPartFetcherToGetHeadReturns("version-123", 1, "U9Wqny3Wh4uvBG7jlJ249WgvG/A+Ue2C+iQ7P8r22G8=", 5);

        givenCallToPartFetcherForSinglePartReturns("A,B,C\n1,2,3");

        // when
        whenApiIsCalledToDownloadFileAsStream();

        // then
        thenTheDownloadBodyShouldMatchExpected();
    }

    @Test
    void singlePartDownloadToStreamFailsToGetDistribution() throws Exception {
        // given
        givenFusionApiManager();

        givenDownloadRequestForStream(
                "common",
                "API_TEST",
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv",
                Collections.singletonMap("fusion-e2e", "rootId/Id"));
        givenDownloadBody("A,B,C\n1,2,3");
        givenCallToPartFetcherToGetHeadReturns("version-123", 1, "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=", 5);
        givenCallToPartFetcherForSinglePartFails(504);

        // when
        whenApiIsCalledToDownloadFileAsStreamFailsWith(APICallException.class);

        // then
        thenAPICallExceptionShouldMatch(504);
    }

    @Test
    void successfulMultiPartDownloadToStream() throws Exception {
        // given
        givenFusionApiManager();
        givenDownloadBody("A,B,C\r1,2,3\r4,5,6\r7,8,9");
        givenDownloadRequestForStream(
                "common",
                "API_TEST",
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv",
                Collections.singletonMap("fusion-e2e", "rootId/Id"));

        givenCallToPartFetcherToGetHeadReturns("a1", 5, "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=-5", 23);
        givenCallToPartFetcherSuccess(
                "A,B,C", 1, "a1", "KPD9WTOuUoQrDwpugLaHblJS+OdUnXaML3YWXla28Rg=", 5, 23, 0, 4, 23);
        givenCallToPartFetcherSuccess(
                "\r1,2,", 2, "a1", "KyQR+rbMkYVdfMHW+tHYfTOmpszv9gHWVn1Ec9yj7lA=", 5, 23, 5, 9, 23);
        givenCallToPartFetcherSuccess(
                "3\r4,5", 3, "a1", "qMnQo29rnj1iA37dWzSBFCKSctoJe8AX5mgmexxvh4A=", 5, 23, 10, 14, 23);
        givenCallToPartFetcherSuccess(
                ",6\r7,", 4, "a1", "RjKiTp8KSSXM64sjp5uHtPXF/uwjh8VNVaCvgDAwrkA=", 5, 23, 15, 19, 23);
        givenCallToPartFetcherSuccess(
                "8,9", 5, "a1", "GI3Dn4384xRI1aZfvWIpkSDzDQbYwKaK4yCy3oBZm/U=", 5, 23, 20, 23, 23);

        // When
        whenApiIsCalledToDownloadFileAsStream();

        // then
        thenTheDownloadBodyShouldMatchExpected();
    }

    @Test
    void successfulCallToPerformMultiPartDownload() throws Exception {
        // given
        givenFusionApiManager();
        givenDirectoryExists("downloads");
        givenDownloadBody("A,B,C\r1,2,3\r4,5,6\r7,8,9");
        givenDownloadRequestForFile(
                "common",
                "API_TEST",
                "downloads/common-API_TEST-20230319.csv",
                "http://localhost:8080/test/catalogs/common/datasets/API_TEST/datasetseries/20230319/distributions/csv",
                Collections.singletonMap("fusion-e2e", "rootId/Id"));
        givenHead("a1", "c", 23L, 5);

        givenCallToPartFetcherSuccess(
                "A,B,C", 1, "a1", "KPD9WTOuUoQrDwpugLaHblJS+OdUnXaML3YWXla28Rg=", 5, 23, 0, 4, 23);
        givenCallToPartFetcherSuccess(
                "\r1,2,", 2, "a1", "KyQR+rbMkYVdfMHW+tHYfTOmpszv9gHWVn1Ec9yj7lA=", 5, 23, 5, 9, 23);
        givenCallToPartFetcherSuccess(
                "3\r4,5", 3, "a1", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", 5, 23, 10, 14, 23);
        givenCallToPartFetcherSuccess(
                ",6\r7,", 4, "a1", "RjKiTp8KSSXM64sjp5uHtPXF/uwjh8VNVaCvgDAwrkA=", 5, 23, 15, 19, 23);
        givenCallToPartFetcherSuccess(
                "8,9", 5, "a1", "GI3Dn4384xRI1aZfvWIpkSDzDQbYwKaK4yCy3oBZm/U=", 5, 23, 20, 23, 23);

        // When
        whenFusionApiDownloaderIsCalledToPerformMultiPartDownload();

        // then
        thenTheFileShouldMatchExpected();
    }

    private void givenDownloadRequestForStream(
            String catalog, String dataset, String apiPath, Map<String, String> headers) {
        downloadRequest = DownloadRequest.builder()
                .catalog(catalog)
                .dataset(dataset)
                .apiPath(apiPath)
                .isDownloadToStream(true)
                .headers(headers)
                .build();
    }

    private void thenExceptionMessageShouldMatch(String expected) {
        assertThat((throwable).getMessage(), is(equalTo(expected)));
    }

    private void thenAPICallExceptionShouldMatch(int expected) {
        assertThat(((APICallException) throwable).getResponseCode(), is(equalTo(expected)));
    }

    private void givenDirectoryExists(String directory) throws IOException {
        Path dir = FileSystems.getDefault().getPath(directory);
        if (!Files.isDirectory(dir)) {
            Files.createDirectory(dir);
        }
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

    private void givenCallToPartFetcherSuccess(
            String content,
            int partNumber,
            String version,
            String checksum,
            int partCount,
            long contentLength,
            long rangeStart,
            long rangeEnd,
            long rangeTotal) {

        givenCallToPartFetcher(
                new ByteArrayInputStream(content.getBytes()),
                partNumber,
                version,
                checksum,
                partCount,
                contentLength,
                rangeStart,
                rangeEnd,
                rangeTotal);
    }

    private void givenCallToPartFetcher(
            InputStream inputStream,
            int partNumber,
            String version,
            String checksum,
            int partCount,
            long contentLength,
            long rangeStart,
            long rangeEnd,
            long rangeTotal) {
        when(partFetcher.fetch(PartRequest.builder()
                        .partNo(partNumber)
                        .downloadRequest(downloadRequest)
                        .build()))
                .thenReturn(GetPartResponse.builder()
                        .content(inputStream)
                        .head(Head.builder()
                                .checksum(checksum)
                                .version(version)
                                .partCount(partCount)
                                .contentLength(contentLength)
                                .contentRange(ContentRange.builder()
                                        .start(rangeStart)
                                        .end(rangeEnd)
                                        .total(rangeTotal)
                                        .build())
                                .build())
                        .build());
    }

    private void givenCallToClientToGetPartFails(int partNumber, int failureStatus) {
        given(partFetcher.fetch(PartRequest.builder()
                        .partNo(partNumber)
                        .downloadRequest(downloadRequest)
                        .build()))
                .willThrow(new APICallException(failureStatus, "broken response"));
    }

    private void givenDownloadRequestForFile(
            String catalog, String dataset, String fileName, String apiPath, Map<String, String> headers) {
        this.downloadRequest = DownloadRequest.builder()
                .apiPath(apiPath)
                .catalog(catalog)
                .dataset(dataset)
                .filePath(fileName)
                .isDownloadToStream(false)
                .headers(headers)
                .build();
    }

    private void givenCallToPartFetcherToGetHeadReturns(
            String version, int partCount, String checksum, long contentLength) {

        Head.HeadBuilder hb =
                Head.builder().version(version).contentLength(contentLength).checksum(checksum);

        if (partCount > 1) {
            hb.partCount(partCount);
            hb.isMultipart(true);
        }

        head = hb.build();
        when(partFetcher.fetch(PartRequest.builder()
                        .partNo(0)
                        .downloadRequest(downloadRequest)
                        .build()))
                .thenReturn(GetPartResponse.builder().head(head).build());
    }

    private void givenCallToClientToGetHeadFails(int failureStatusCode) {
        when(partFetcher.fetch(PartRequest.builder()
                        .partNo(0)
                        .downloadRequest(downloadRequest)
                        .build()))
                .thenThrow(new APICallException(failureStatusCode, "broken head"));
    }

    private void finallyDeleteFile() {
        if (Objects.nonNull(downloadRequest) && Objects.nonNull(downloadRequest.getFilePath())) {
            File file = new File(downloadRequest.getFilePath());
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void thenTheFileShouldMatchExpected() throws Exception {

        File file = new File(downloadRequest.getFilePath());

        byte[] outputBytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(outputBytes);
        }

        assertThat(outputBytes, is(equalTo(downloadBody)));
    }

    private void whenFusionApiManagerIsCalledToDownloadFileToPath() {
        apiDownloader.callAPIFileDownload(
                downloadRequest.getApiPath(),
                downloadRequest.getFilePath(),
                downloadRequest.getCatalog(),
                downloadRequest.getDataset(),
                downloadRequest.getHeaders());
    }

    private void whenFusionApiManagerIsCalledToDownloadFileToPathAndExceptionIsExcepted(
            Class<? extends Throwable> exception) {
        throwable = Assertions.assertThrows(
                exception,
                () -> apiDownloader.callAPIFileDownload(
                        downloadRequest.getApiPath(),
                        downloadRequest.getFilePath(),
                        downloadRequest.getCatalog(),
                        downloadRequest.getDataset(),
                        downloadRequest.getHeaders()));
    }

    private void thenTheDownloadBodyShouldMatchExpected() throws Exception {
        byte[] outputBytes = new byte[(int) downloadBody.length];
        StringBuilder sb = new StringBuilder();

        while ((responseStream.read(outputBytes)) != -1) {}

        assertThat(outputBytes, is(equalTo(downloadBody)));
    }

    private void whenApiIsCalledToDownloadFileAsStream() {
        responseStream = apiDownloader.callAPIFileDownload(
                downloadRequest.getApiPath(),
                downloadRequest.getCatalog(),
                downloadRequest.getDataset(),
                downloadRequest.getHeaders());
    }

    private void whenApiIsCalledToDownloadFileAsStreamFailsWith(Class<? extends Throwable> expected) {
        throwable = Assertions.assertThrows(
                expected,
                () -> apiDownloader.callAPIFileDownload(
                        downloadRequest.getApiPath(),
                        downloadRequest.getCatalog(),
                        downloadRequest.getDataset(),
                        downloadRequest.getHeaders()));
    }

    private void givenCallToPartFetcherForSinglePartReturns(String content) {

        when(partFetcher.fetch(PartRequest.builder()
                        .partNo(1)
                        .downloadRequest(downloadRequest)
                        .head(head)
                        .build()))
                .thenReturn(GetPartResponse.builder()
                        .content(new ByteArrayInputStream(content.getBytes()))
                        .head(head)
                        .build());
    }

    private void givenCallToPartFetcherForSinglePartFails(int status) {
        given(partFetcher.fetch(PartRequest.builder()
                        .partNo(1)
                        .downloadRequest(downloadRequest)
                        .head(head)
                        .build()))
                .willThrow(new APICallException(status, "broken response"));
    }

    private void givenDownloadBody(String body) {
        this.downloadBody = body.getBytes();
    }

    private void givenFusionApiManager() {

        apiDownloader = FusionAPIDownloadOperations.builder()
                .partFetcher(partFetcher)
                .configuration(FusionConfiguration.builder().build())
                .build();
    }

    private static class BrokenInputStream extends InputStream {
        byte[] fooBar;

        public BrokenInputStream(byte[] bytes) {
            fooBar = bytes;
        }

        @Override
        public int read() throws IOException {
            throw new IOException("Unable to read from InputStream");
        }
    }
}
