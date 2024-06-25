package io.github.jpmorganchase.fusion.api.operations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import com.google.gson.GsonBuilder;
import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.api.FusionApiManagerTest;
import io.github.jpmorganchase.fusion.api.context.MultipartTransferContext;
import io.github.jpmorganchase.fusion.api.context.UploadedPartContext;
import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.request.UploadRequest;
import io.github.jpmorganchase.fusion.api.response.UploadedPart;
import io.github.jpmorganchase.fusion.api.response.UploadedParts;
import io.github.jpmorganchase.fusion.digest.AlgoSpecificDigestProducer;
import io.github.jpmorganchase.fusion.digest.DigestDescriptor;
import io.github.jpmorganchase.fusion.digest.DigestProducer;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.model.Operation;
import io.github.jpmorganchase.fusion.oauth.provider.FusionTokenProvider;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("SameParameterValue")
@ExtendWith(MockitoExtension.class)
class FusionAPIUploadOperationsTest {

    private FusionAPIUploadOperations fusionAPIUploader;

    @Mock
    private Client client;

    @Mock
    private DigestProducer digestProducer;

    @Mock
    private FusionTokenProvider fusionTokenProvider;

    private String apiPath;

    private DigestDescriptor digestDescriptor;

    private byte[] uploadBody;

    private InputStream uploadStream;

    private String fileName;

    private Throwable thrown;

    private MultipartTransferContext multipartTransferContext;

    private UploadRequest uploadRequest;

    private List<UploadedPart> uploadedParts = new ArrayList<>();

    private FusionConfiguration fusionConfiguration;

    private int uploadPartSize = 16;

    private int singlePartUploadSizeLimit = 16;

    private Map<String, String> headers;

    private void givenSessionBearerToken(String token) {
        given(fusionTokenProvider.getSessionBearerToken()).willReturn(token);
    }

    @Test
    void successfulSinglePartFileUpload() {
        // given
        givenSdkAPIUploader();
        givenApiPath("http://localhost:8080/test");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "simple_dataset", "dataset-token");
        givenUploadFile("upload-test.csv");
        givenUploadBody("A,B,C\n1,2,3\n4,5,6\n7,8,9");
        givenClientHeaders("client-header-key", "client-header-val", "client-header-key1", "client-header-value1");

        givenCallToProduceDigestReturnsDigestDescriptor("k0IH+I4DpJla6wabZBNCUEMSBZtS2seC/9ixCa3KnZE=");
        givenCallToClientToUploadIsSuccessful(
                "my-token",
                "dataset-token",
                "2023-03-01",
                "2023-03-02",
                "2023-03-03",
                "k0IH+I4DpJla6wabZBNCUEMSBZtS2seC/9ixCa3KnZE=",
                "23",
                new HashMap<>(this.headers));
        // when
        whenSDKAPIUploaderIsCalledToUploadFileFromPath(
                "common", "simple_dataset", "2023-03-01", "2023-03-02", "2023-03-03", new HashMap<>(this.headers));
    }

    @Test
    void successfulSinglePartFileUploadWithStream() {
        // given
        givenSdkAPIUploader();
        givenApiPath("http://localhost:8080/test");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "simple_dataset", "dataset-token");
        givenUploadBody("A,B,C\n1,2,3\n4,5,6\n7,8,9");
        givenClientHeaders("client-header-key", "client-header-val", "client-header-key1", "client-header-value1");

        givenCallToProduceDigestReturnsDigestDescriptor("k0IH+I4DpJla6wabZBNCUEMSBZtS2seC/9ixCa3KnZE=");
        givenCallToClientToUploadIsSuccessful(
                "my-token",
                "dataset-token",
                "2023-03-01",
                "2023-03-02",
                "2023-03-03",
                "k0IH+I4DpJla6wabZBNCUEMSBZtS2seC/9ixCa3KnZE=",
                "23",
                new HashMap<>(this.headers));
        // when
        whenSDKAPIUploaderIsCalledToUploadFileFromStream(
                "common", "simple_dataset", "2023-03-01", "2023-03-02", "2023-03-03", new HashMap<>(this.headers));
    }

    @Test
    void successfullyInitiateMultipartUpload() {
        // given
        givenSdkAPIUploader();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/test-dataset/datasetseries/2023-03-19/distributions/csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "test-dataset", "dataset-token");
        givenUploadFile("large-upload-test.csv");
        givenClientHeader("client-header-key", "client-header-val");

        givenCallToClientToInitiateTransferIsSuccessful(
                "some-operation-id-aa", "my-token", "dataset-token", copyOfHeaders());
        givenUploadRequest(copyOfHeaders());
        // when
        whenFusionApiManagerIsCalledToInitiateMultipartUpload();
        // then
        thenOperationIdShouldMatch("some-operation-id-aa");
        thenMultipartTransferContextShouldAllowToProgress();
    }

    @Test
    void successfullyUploadPartsWithSizeSetToSixteen() {
        // given
        givenSha256DigestProducer();
        givenUploadPartSize(16);
        givenSdkAPIUploader();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/test-dataset/datasetseries/2023-03-19/distributions/csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "test-dataset", "dataset-token");
        givenUploadFile("large-upload-test.csv");
        givenMultipartTransferContextStatusIsStarted("my-op-id");
        givenCallToClientToUploadPart(
                1,
                "SFiERkoisri4Xv+MPlq3mtarmxbkmHPSaeLAXeNDk6A=",
                "my-op-id",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                2,
                "Vlrqh/mN2FJfct6E4Ah5UhnwPQ2tIoyQW4vmUBVD+lw=",
                "my-op-id",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenUploadRequest("2023-03-19", copyOfHeaders());

        // when
        whenFusionApiManagerIsCalledToUploadParts();

        // then
        thenMultipartTransferContextStatusShouldBeTransferred();
        thenMultipartTransferContextStatusShouldSetTotalBytes(27000006L);
    }

    @Test
    void successfullyUploadPartsWithPartSizeSetToEight() {
        // given
        givenSha256DigestProducer();
        givenUploadPartSize(8);
        givenSdkAPIUploader();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/test-dataset/datasetseries/2023-03-19/distributions/csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "test-dataset", "dataset-token");
        givenUploadFile("large-upload-test.csv");
        givenClientHeader("client-header-key", "client-header-val");

        givenMultipartTransferContextStatusIsStarted("my-op-id");
        givenCallToClientToUploadPart(
                1,
                "QOFxhmCbpMEDsB6ZWpQGstjqGYrKbSx6FsStJgTK5HA=",
                "my-op-id",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                2,
                "BrRfmO2ryteC4a6+HeOMDwuxXOif0z3qRJ5BDWXKrXg=",
                "my-op-id",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                3,
                "TuKiXOkmJKwfK6luEz3XTKkevrsn2WC8YukQ/pEa6MA=",
                "my-op-id",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                4,
                "G6RtAEGJqAKL1PaJNRRCgT2AXceURap43HJ4oaWXYdo=",
                "my-op-id",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenUploadRequest("2023-03-19", copyOfHeaders());

        // when
        whenFusionApiManagerIsCalledToUploadParts();

        // then
        thenMultipartTransferContextStatusShouldBeTransferred();
    }

    @Test
    void successfullyCompleteMultipartUpload() {
        // given
        givenSha256DigestProducer();
        givenSdkAPIUploader();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/test-dataset/datasetseries/2023-03-19/distributions/csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "test-dataset", "dataset-token");
        givenUploadFile("large-upload-test.csv");
        givenClientHeaders("client-header-key", "client-header-val", "client-header-key1", "client-header-value1");
        givenUploadRequest("2023-05-18", copyOfHeaders());

        givenMultipartTransferContextStatusIsStarted("my-op-id");
        givenPartHasBeenUploaded(1, "provider-gen-part-id-1", "base64-checksum-1", "digest-as-bytes-1".getBytes());
        givenPartHasBeenUploaded(2, "provider-gen-part-id-2", "base64-checksum-2", "digest-as-bytes-2".getBytes());
        givenPartHasBeenUploaded(3, "provider-gen-part-id-3", "base64-checksum-3", "digest-as-bytes-3".getBytes());
        givenPartHasBeenUploaded(4, "provider-gen-part-id-4", "base64-checksum-4", "digest-as-bytes-4".getBytes());
        givenMultipartTransferContextStatusIsTransferred(8 * (1024 * 1024), 33554432, 4);
        givenCallToClientToCompleteMultipartUpload(
                "my-op-id",
                "my-token",
                "dataset-token",
                "2023-05-18",
                "2023-05-18",
                "2023-05-18",
                "kdNJFWpnN7XnQx02fsMYlZ5CxZzdCXvI4GEBIoqh/Wk=",
                copyOfHeaders());

        // when
        whenFusionApiManagerIsCalledToCompleteMultipartTransfer();

        // then
        thenMultipartTransferStatusShouldBeComplete();
    }

    @Test
    void successfullyMultipartUpload() {
        // given
        givenSha256DigestProducer();
        givenUploadPartSize(8);
        givenSdkAPIUploader();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/test-dataset/datasetseries/2023-03-19/distributions/csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "test-dataset", "dataset-token");
        givenUploadFile("large-upload-test.csv");
        givenClientHeader("client-header-key", "client-header-val");

        givenCallToClientToInitiateTransferIsSuccessful(
                "some-operation-id-aa", "my-token", "dataset-token", copyOfHeaders());
        givenCallToClientToUploadPart(
                1,
                "QOFxhmCbpMEDsB6ZWpQGstjqGYrKbSx6FsStJgTK5HA=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                2,
                "BrRfmO2ryteC4a6+HeOMDwuxXOif0z3qRJ5BDWXKrXg=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                3,
                "TuKiXOkmJKwfK6luEz3XTKkevrsn2WC8YukQ/pEa6MA=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                4,
                "G6RtAEGJqAKL1PaJNRRCgT2AXceURap43HJ4oaWXYdo=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToCompleteMultipartUpload(
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                "2023-03-01",
                "2023-03-02",
                "2023-03-03",
                "IASqXk2/aZRMT2siJkzc/3Mg6DSG2oil5IwkMeb4KgE=",
                copyOfHeaders());

        // When
        whenSDKAPIUploaderIsCalledToUploadFileFromPath(
                "common", "test-dataset", "2023-03-01", "2023-03-02", "2023-03-03", copyOfHeaders());
    }

    @Test
    void successfullyMultipartUploadWithStream() {
        // given
        givenSha256DigestProducer();
        givenUploadPartSize(8);
        givenSdkAPIUploader();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/test-dataset/datasetseries/2023-03-19/distributions/csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "test-dataset", "dataset-token");
        givenUploadStream("large-upload-test.csv");
        givenClientHeaders("client-header-key", "client-header-val", "client-header-key1", "client-header-value1");

        givenCallToClientToInitiateTransferIsSuccessful(
                "some-operation-id-aa", "my-token", "dataset-token", copyOfHeaders());
        givenCallToClientToUploadPart(
                1,
                "QOFxhmCbpMEDsB6ZWpQGstjqGYrKbSx6FsStJgTK5HA=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                2,
                "BrRfmO2ryteC4a6+HeOMDwuxXOif0z3qRJ5BDWXKrXg=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                3,
                "TuKiXOkmJKwfK6luEz3XTKkevrsn2WC8YukQ/pEa6MA=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                4,
                "G6RtAEGJqAKL1PaJNRRCgT2AXceURap43HJ4oaWXYdo=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToCompleteMultipartUpload(
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                "2023-03-01",
                "2023-03-02",
                "2023-03-03",
                "IASqXk2/aZRMT2siJkzc/3Mg6DSG2oil5IwkMeb4KgE=",
                copyOfHeaders());

        // When
        whenSDKAPIUploaderIsCalledToUploadFileFromStream(
                "common", "test-dataset", "2023-03-01", "2023-03-02", "2023-03-03", copyOfHeaders());
    }

    @Test
    void multipartUploadFailsToInitiateTransfer() {
        // given
        givenSha256DigestProducer();
        givenUploadPartSize(8);
        givenSdkAPIUploader();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/test-dataset/datasetseries/2023-03-19/distributions/csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "test-dataset", "dataset-token");
        givenUploadFile("large-upload-test.csv");
        givenClientHeaders("client-header-key", "client-header-val", "client-header-key1", "client-header-value1");

        givenCallToClientToInitiateTransferFails("my-token", "dataset-token", copyOfHeaders(), 500);

        // When
        whenSDKAPIUploaderIsCalledToUploadFileFromPathAndExceptionIsRaised(
                "common",
                "test-dataset",
                "2023-03-01",
                "2023-03-02",
                "2023-03-03",
                copyOfHeaders(),
                APICallException.class);

        // Then
        thenApiCallExceptionShouldHaveHttpStatus(500);
    }

    @Test
    void multipartUploadFailsToUploadPart() {
        // given
        givenSha256DigestProducer();
        givenUploadPartSize(8);
        givenSdkAPIUploader();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/test-dataset/datasetseries/2023-03-19/distributions/csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "test-dataset", "dataset-token");
        givenUploadFile("large-upload-test.csv");
        givenClientHeaders("client-header-key", "client-header-val", "client-header-key1", "client-header-value1");

        givenCallToClientToInitiateTransferIsSuccessful(
                "some-operation-id-aa", "my-token", "dataset-token", copyOfHeaders());
        givenCallToClientToUploadPart(
                1,
                "QOFxhmCbpMEDsB6ZWpQGstjqGYrKbSx6FsStJgTK5HA=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                2,
                "BrRfmO2ryteC4a6+HeOMDwuxXOif0z3qRJ5BDWXKrXg=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPartFails(
                3,
                "TuKiXOkmJKwfK6luEz3XTKkevrsn2WC8YukQ/pEa6MA=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders(),
                404);
        givenCallToClientToUploadPart(
                4,
                "G6RtAEGJqAKL1PaJNRRCgT2AXceURap43HJ4oaWXYdo=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToAbortMultipartUpload("some-operation-id-aa", "my-token", "dataset-token", copyOfHeaders());

        // When
        whenSDKAPIUploaderIsCalledToUploadFileFromPathAndExceptionIsRaised(
                "common",
                "test-dataset",
                "2023-03-01",
                "2023-03-02",
                "2023-03-03",
                new HashMap<>(this.headers),
                APICallException.class);

        // Then
        thenApiCallExceptionShouldHaveHttpStatus(404);
    }

    @Test
    void multipartUploadFailsToComplete() {
        // given
        givenSha256DigestProducer();
        givenUploadPartSize(8);
        givenSdkAPIUploader();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/test-dataset/datasetseries/2023-03-19/distributions/csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "test-dataset", "dataset-token");
        givenUploadFile("large-upload-test.csv");
        givenClientHeaders("client-header-key", "client-header-val", "client-header-key1", "client-header-value1");

        givenCallToClientToInitiateTransferIsSuccessful(
                "some-operation-id-aa", "my-token", "dataset-token", copyOfHeaders());
        givenCallToClientToUploadPart(
                1,
                "QOFxhmCbpMEDsB6ZWpQGstjqGYrKbSx6FsStJgTK5HA=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                2,
                "BrRfmO2ryteC4a6+HeOMDwuxXOif0z3qRJ5BDWXKrXg=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                3,
                "TuKiXOkmJKwfK6luEz3XTKkevrsn2WC8YukQ/pEa6MA=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                4,
                "G6RtAEGJqAKL1PaJNRRCgT2AXceURap43HJ4oaWXYdo=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToCompleteMultipartUploadFails(
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                "2023-05-18",
                "2023-05-18",
                "2023-05-18",
                "IASqXk2/aZRMT2siJkzc/3Mg6DSG2oil5IwkMeb4KgE=",
                copyOfHeaders(),
                504);
        givenCallToClientToAbortMultipartUpload("some-operation-id-aa", "my-token", "dataset-token", copyOfHeaders());

        // when
        whenSDKAPIUploaderIsCalledToUploadFileFromPathAndExceptionIsRaised(
                "common",
                "test-dataset",
                "2023-05-18",
                "2023-05-18",
                "2023-05-18",
                copyOfHeaders(),
                APICallException.class);

        // then
        thenApiCallExceptionShouldHaveHttpStatus(504);
    }

    @Test
    void multipartUploadFailsToAbort() {
        // given
        givenSha256DigestProducer();
        givenUploadPartSize(8);
        givenSdkAPIUploader();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/test-dataset/datasetseries/2023-03-19/distributions/csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "test-dataset", "dataset-token");
        givenUploadFile("large-upload-test.csv");
        givenClientHeader("client-header-key", "client-header-val");

        givenCallToClientToInitiateTransferIsSuccessful(
                "some-operation-id-aa", "my-token", "dataset-token", copyOfHeaders());
        givenCallToClientToUploadPart(
                1,
                "QOFxhmCbpMEDsB6ZWpQGstjqGYrKbSx6FsStJgTK5HA=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                2,
                "BrRfmO2ryteC4a6+HeOMDwuxXOif0z3qRJ5BDWXKrXg=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                3,
                "TuKiXOkmJKwfK6luEz3XTKkevrsn2WC8YukQ/pEa6MA=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToUploadPart(
                4,
                "G6RtAEGJqAKL1PaJNRRCgT2AXceURap43HJ4oaWXYdo=",
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                copyOfHeaders());
        givenCallToClientToCompleteMultipartUploadFails(
                "some-operation-id-aa",
                "my-token",
                "dataset-token",
                "2023-05-18",
                "2023-05-18",
                "2023-05-18",
                "IASqXk2/aZRMT2siJkzc/3Mg6DSG2oil5IwkMeb4KgE=",
                copyOfHeaders(),
                504);
        givenCallToClientToAbortMultipartUploadFails(
                "some-operation-id-aa", "my-token", "dataset-token", copyOfHeaders(), 500);

        // when
        whenSDKAPIUploaderIsCalledToUploadFileFromPathAndExceptionIsRaised(
                "common",
                "test-dataset",
                "2023-05-18",
                "2023-05-18",
                "2023-05-18",
                copyOfHeaders(),
                APICallException.class);

        // then
        thenApiCallExceptionShouldHaveHttpStatus(500);
    }

    @Test
    void successfullyAbortMultipartUpload() {
        // given
        givenSha256DigestProducer();
        givenSdkAPIUploader();
        givenApiPath(
                "http://localhost:8080/test/catalogs/common/datasets/test-dataset/datasetseries/2023-03-19/distributions/csv");
        givenSessionBearerToken("my-token");
        givenDatasetBearerToken("common", "test-dataset", "dataset-token");
        givenUploadFile("large-upload-test.csv");
        givenUploadRequest("2023-03-19", copyOfHeaders());
        givenMultipartTransferContextStatusIsStarted("my-op-id");
        givenPartHasBeenUploaded(1, "provider-gen-part-id-1", "base64-checksum-1", "digest-as-bytes-1".getBytes());
        givenCallToClientToAbortMultipartUpload("my-op-id", "my-token", "dataset-token", copyOfHeaders());

        // when
        whenFusionApiManagerIsCalledToAbortMultipartTransfer();

        // then
        thenMultipartTransferStatusShouldBeAborted();
    }

    private void givenUploadPartSize(int uploadPartSize) {
        this.uploadPartSize = uploadPartSize;
    }

    @SneakyThrows
    private void givenUploadRequest(String allDates, Map<String, String> headers) {
        uploadRequest = UploadRequest.builder()
                .fromStream(Files.newInputStream(new File(fileName).toPath()))
                .apiPath(apiPath)
                .catalog("common")
                .dataset("test-dataset")
                .fromDate(allDates)
                .toDate(allDates)
                .createdDate(allDates)
                .maxSinglePartFileSize(fusionAPIUploader.getSinglePartUploadSizeLimit())
                .headers(headers)
                .build();
    }

    @SneakyThrows
    private void givenUploadRequest(Map<String, String> headers) {
        uploadRequest = UploadRequest.builder()
                .fromStream(Files.newInputStream(new File(fileName).toPath()))
                .apiPath(apiPath)
                .catalog("common")
                .dataset("test-dataset")
                .fromDate("2023-03-01")
                .toDate("2023-03-02")
                .createdDate("2023-03-03")
                .maxSinglePartFileSize(fusionAPIUploader.getSinglePartUploadSizeLimit())
                .headers(headers)
                .build();
    }

    private void thenMultipartTransferStatusShouldBeComplete() {
        assertThat(
                multipartTransferContext.getStatus(),
                is(equalTo(MultipartTransferContext.MultipartTransferStatus.COMPLETED)));
    }

    private void thenMultipartTransferStatusShouldBeAborted() {
        assertThat(
                multipartTransferContext.getStatus(),
                is(equalTo(MultipartTransferContext.MultipartTransferStatus.ABORTED)));
    }

    private void givenCallToClientToCompleteMultipartUpload(
            String operationId,
            String token,
            String fusionToken,
            String fDate,
            String tDate,
            String cDate,
            String digest,
            Map<String, String> headers) {

        givenRequestHeader(headers, "Content-Type", "application/json");
        givenRequestHeader(headers, "Authorization", "Bearer " + token);
        givenRequestHeader(headers, "Fusion-Authorization", "Bearer " + fusionToken);
        givenRequestHeader(headers, "x-jpmc-distribution-from-date", fDate);
        givenRequestHeader(headers, "x-jpmc-distribution-to-date", tDate);
        givenRequestHeader(headers, "x-jpmc-distribution-created-date", cDate);
        givenRequestHeader(headers, "Digest", "SHA-256=" + digest);

        String body = new GsonBuilder()
                .create()
                .toJson(UploadedParts.builder().parts(uploadedParts).build());
        String path = String.format("/operations/upload?operationId=%s", operationId);

        when(client.post(eq(apiPath + path), eq(headers), eq(body)))
                .thenReturn(HttpResponse.<String>builder().statusCode(200).build());
    }

    private void givenCallToClientToCompleteMultipartUploadFails(
            String operationId,
            String token,
            String fusionToken,
            String fDate,
            String tDate,
            String cDate,
            String digest,
            Map<String, String> headers,
            int failureStatus) {

        givenRequestHeader(headers, "Content-Type", "application/json");
        givenRequestHeader(headers, "Authorization", "Bearer " + token);
        givenRequestHeader(headers, "Fusion-Authorization", "Bearer " + fusionToken);
        givenRequestHeader(headers, "x-jpmc-distribution-from-date", fDate);
        givenRequestHeader(headers, "x-jpmc-distribution-to-date", tDate);
        givenRequestHeader(headers, "x-jpmc-distribution-created-date", cDate);
        givenRequestHeader(headers, "Digest", "SHA-256=" + digest);

        String body = new GsonBuilder()
                .create()
                .toJson(UploadedParts.builder().parts(uploadedParts).build());
        String path = String.format("/operations/upload?operationId=%s", operationId);

        when(client.post(eq(apiPath + path), eq(headers), eq(body)))
                .thenReturn(
                        HttpResponse.<String>builder().statusCode(failureStatus).build());
    }

    private void givenCallToClientToAbortMultipartUpload(
            String operationId, String token, String fusionToken, Map<String, String> headers) {

        String path = String.format("/operations/upload?operationId=%s", operationId);

        givenRequestHeader(headers, "Authorization", "Bearer " + token);
        givenRequestHeader(headers, "Fusion-Authorization", "Bearer " + fusionToken);

        when(client.delete(eq(apiPath + path), eq(headers), isNull()))
                .thenReturn(HttpResponse.<String>builder().statusCode(200).build());
    }

    private void givenCallToClientToAbortMultipartUploadFails(
            String operationId, String token, String fusionToken, Map<String, String> headers, int failureStatus) {

        String path = String.format("/operations/upload?operationId=%s", operationId);

        givenRequestHeader(headers, "Authorization", "Bearer " + token);
        givenRequestHeader(headers, "Fusion-Authorization", "Bearer " + fusionToken);

        when(client.delete(eq(apiPath + path), eq(headers), isNull()))
                .thenReturn(
                        HttpResponse.<String>builder().statusCode(failureStatus).build());
    }

    private void whenFusionApiManagerIsCalledToCompleteMultipartTransfer() {
        multipartTransferContext =
                fusionAPIUploader.callAPIToCompleteMultiPartUpload(multipartTransferContext, uploadRequest);
    }

    private void whenFusionApiManagerIsCalledToAbortMultipartTransfer() {
        multipartTransferContext =
                fusionAPIUploader.callAPIToAbortMultiPartUpload(multipartTransferContext, uploadRequest);
    }

    private void givenMultipartTransferContextStatusIsTransferred(int chunkSize, int totalBytes, int partCount) {
        multipartTransferContext.transferred(chunkSize, totalBytes, partCount);
    }

    private void givenPartHasBeenUploaded(
            int partCnt, String partIdentifier, String partChecksum, byte[] partChecksumBytes) {

        UploadedPart up = UploadedPart.builder()
                .partNumber(String.valueOf(partCnt))
                .partIdentifier(partIdentifier)
                .partDigest(partChecksum)
                .build();

        uploadedParts.add(up);

        multipartTransferContext.partUploaded(UploadedPartContext.builder()
                .part(up)
                .digest(partChecksumBytes)
                .partNo(partCnt)
                .build());
    }

    private void thenApiCallExceptionShouldHaveHttpStatus(int expected) {
        assertThat(((APICallException) thrown).getResponseCode(), is(equalTo(expected)));
    }

    private void givenSha256DigestProducer() {
        this.digestProducer = AlgoSpecificDigestProducer.builder().sha256().build();
    }

    private void givenMultipartTransferContextStatusIsStarted(String opId) {
        Operation op = new Operation(opId);
        multipartTransferContext = MultipartTransferContext.started(op);
    }

    private void givenCallToClientToUploadPart(
            int partNo,
            String digest,
            String operationId,
            String authToken,
            String fusionToken,
            Map<String, String> headers) {

        String path = String.format("/operations/upload?operationId=%s&partNumber=%d", operationId, partNo);

        givenRequestHeader(headers, "accept", "*/*");
        givenRequestHeader(headers, "Authorization", "Bearer " + authToken);
        givenRequestHeader(headers, "Fusion-Authorization", "Bearer " + fusionToken);
        givenRequestHeader(headers, "Content-Type", "application/octet-stream");

        headers.put("Digest", "SHA-256=" + digest);

        UploadedPart uploadedPart = UploadedPart.builder()
                .partNumber(String.valueOf(partNo))
                .partIdentifier("provider-gen-part-id-" + partNo)
                .partDigest(digest)
                .build();

        String body = new GsonBuilder().create().toJson(uploadedPart);

        when(client.put(eq(apiPath + path), eq(headers), isNotNull()))
                .thenReturn(HttpResponse.<String>builder()
                        .body(body)
                        .statusCode(200)
                        .build());

        uploadedParts.add(uploadedPart);
    }

    private void givenCallToClientToUploadPartFails(
            int partNo,
            String digest,
            String operationId,
            String authToken,
            String fusionToken,
            Map<String, String> headers,
            int failureStatus) {

        String path = String.format("/operations/upload?operationId=%s&partNumber=%d", operationId, partNo);

        givenRequestHeader(headers, "accept", "*/*");
        givenRequestHeader(headers, "Authorization", "Bearer " + authToken);
        givenRequestHeader(headers, "Fusion-Authorization", "Bearer " + fusionToken);
        givenRequestHeader(headers, "Content-Type", "application/octet-stream");

        headers.put("Digest", "SHA-256=" + digest);

        when(client.put(eq(apiPath + path), eq(headers), isNotNull()))
                .thenReturn(
                        HttpResponse.<String>builder().statusCode(failureStatus).build());
    }

    private void thenMultipartTransferContextStatusShouldBeTransferred() {
        assertThat(
                multipartTransferContext.getStatus(),
                is(equalTo(MultipartTransferContext.MultipartTransferStatus.TRANSFERRED)));
    }

    private void thenMultipartTransferContextStatusShouldSetTotalBytes(long expected) {
        assertThat(multipartTransferContext.getTotalBytes(), is(equalTo(expected)));
    }

    @SneakyThrows
    private void whenFusionApiManagerIsCalledToUploadParts() {
        multipartTransferContext = fusionAPIUploader.callAPIToUploadParts(multipartTransferContext, uploadRequest);
    }

    private void thenMultipartTransferContextShouldAllowToProgress() {
        assertThat(multipartTransferContext.canProceedToTransfer(), is(equalTo(true)));
    }

    private void thenOperationIdShouldMatch(String expected) {
        assertThat(multipartTransferContext.getOperation().getOperationId(), is(equalTo(expected)));
    }

    private void whenFusionApiManagerIsCalledToInitiateMultipartUpload() {
        multipartTransferContext = fusionAPIUploader.callAPIToInitiateMultiPartUpload(uploadRequest);
    }

    private void givenCallToClientToInitiateTransferIsSuccessful(
            String operationId, String authToken, String fusionToken, Map<String, String> headers) {

        givenRequestHeader(headers, "accept", "*/*");
        givenRequestHeader(headers, "Authorization", "Bearer " + authToken);
        givenRequestHeader(headers, "Fusion-Authorization", "Bearer " + fusionToken);

        when(client.post(eq(apiPath + "/operationType/upload"), eq(headers), isNull()))
                .thenReturn(HttpResponse.<String>builder()
                        .body("{\"operationId\": \"" + operationId + "\"}")
                        .statusCode(200)
                        .build());
    }

    private void givenCallToClientToInitiateTransferFails(
            String authToken, String fusionToken, Map<String, String> headers, int failureStatus) {

        givenRequestHeader(headers, "accept", "*/*");
        givenRequestHeader(headers, "Authorization", "Bearer " + authToken);
        givenRequestHeader(headers, "Fusion-Authorization", "Bearer " + fusionToken);

        when(client.post(eq(apiPath + "/operationType/upload"), eq(headers), isNull()))
                .thenReturn(
                        HttpResponse.<String>builder().statusCode(failureStatus).build());
    }

    private void givenDatasetBearerToken(String catalog, String dataset, String token) {
        given(fusionTokenProvider.getDatasetBearerToken(catalog, dataset)).willReturn(token);
    }

    private void givenApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    private void givenUploadFile(String resource) {
        this.fileName = getPathFromResource(resource);
    }

    @SneakyThrows
    private void givenUploadStream(String resource) {
        this.fileName = getPathFromResource(resource);
        this.uploadStream = new FileInputStream(this.fileName);
    }

    private void whenSDKAPIUploaderIsCalledToUploadFileFromPath(
            String catalog,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate,
            Map<String, String> headers) {
        fusionAPIUploader.callAPIFileUpload(
                apiPath, fileName, catalog, dataset, fromDate, toDate, createdDate, headers);
    }

    private void whenSDKAPIUploaderIsCalledToUploadFileFromPathAndExceptionIsRaised(
            String catalog,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate,
            Map<String, String> headers,
            Class<? extends Throwable> throwable) {
        this.thrown = Assertions.assertThrows(
                throwable,
                () -> fusionAPIUploader.callAPIFileUpload(
                        apiPath, fileName, catalog, dataset, fromDate, toDate, createdDate, headers));
    }

    private void whenSDKAPIUploaderIsCalledToUploadFileFromStream(
            String catalog,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate,
            Map<String, String> headers) {
        fusionAPIUploader.callAPIFileUpload(
                apiPath, uploadStream, catalog, dataset, fromDate, toDate, createdDate, headers);
    }

    private void givenCallToClientToUploadIsSuccessful(
            String token,
            String fToken,
            String fDate,
            String tDate,
            String cDate,
            String digest,
            String length,
            Map<String, String> headers) {

        givenRequestHeader(headers, "accept", "*/*");
        givenRequestHeader(headers, "Authorization", "Bearer my-token");
        givenRequestHeader(headers, "Fusion-Authorization", "Bearer dataset-token");
        givenRequestHeader(headers, "Content-Type", "application/octet-stream");
        givenRequestHeader(headers, "x-jpmc-distribution-from-date", "2023-03-01");
        givenRequestHeader(headers, "x-jpmc-distribution-to-date", "2023-03-02");
        givenRequestHeader(headers, "x-jpmc-distribution-created-date", "2023-03-03");
        givenRequestHeader(headers, "Digest", "SHA-256=k0IH+I4DpJla6wabZBNCUEMSBZtS2seC/9ixCa3KnZE=");
        givenRequestHeader(headers, "Content-Length", "23");

        when(client.put(eq(apiPath), eq(headers), argThat(bodyEquals(uploadBody))))
                .thenReturn(HttpResponse.<String>builder().statusCode(200).build());
    }

    private void givenUploadBody(String uploadbody) {
        this.uploadBody = uploadbody.getBytes();
        this.uploadStream = new ByteArrayInputStream(this.uploadBody);
    }

    private Map<String, String> copyOfHeaders() {
        if (null == this.headers) {
            return new HashMap<>();
        }
        return new HashMap<>(this.headers);
    }

    private void givenClientHeaders(String key, String value, String key1, String value1) {
        this.headers = new HashMap<>();
        headers.put(key, value);
        headers.put(key1, value1);
    }

    private void givenClientHeader(String key, String value) {
        this.headers = new HashMap<>();
        headers.put(key, value);
    }

    private void givenCallToProduceDigestReturnsDigestDescriptor(String digest) {
        digestDescriptor = DigestDescriptor.builder()
                .checksum(digest)
                .size(uploadBody.length)
                .content(uploadBody)
                .build();

        when(this.digestProducer.execute(argThat(bodyEquals(uploadBody)))).thenReturn(digestDescriptor);
    }

    private void givenRequestHeader(Map<String, String> headers, String headerKey, String headerValue) {
        headers.put(headerKey, headerValue);
    }

    private void givenSdkAPIUploader() {

        fusionAPIUploader = FusionAPIUploadOperations.builder()
                .httpClient(client)
                .configuration(FusionConfiguration.builder()
                        .uploadPartSize(uploadPartSize)
                        .singlePartUploadSizeLimit(singlePartUploadSizeLimit)
                        .build())
                .fusionTokenProvider(fusionTokenProvider)
                .digestProducer(digestProducer)
                .build();
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
