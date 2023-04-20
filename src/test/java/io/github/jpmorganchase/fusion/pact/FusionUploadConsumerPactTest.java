package io.github.jpmorganchase.fusion.pact;

import static io.github.jpmorganchase.fusion.pact.util.RequestResponseHelper.*;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.github.jpmorganchase.fusion.Fusion;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PactConsumerTestExt.class)
@Slf4j
public class FusionUploadConsumerPactTest {

    private static final String FUSION_API_VERSION = "/v1/";

    Fusion fusion;

    @Pact(provider = "FusionUpload", consumer = "FusionSdk")
    public RequestResponsePact uploadFile(PactDslWithProvider builder) {

        Map<String, String> uploadHeaders = givenUploadHeaders(
                "2022-01-15", "2022-01-16", "2022-01-17", "5", "KPD9WTOuUoQrDwpugLaHblJS+OdUnXaML3YWXla28Rg=");
        return uploadExpectation(
                builder,
                "a distribution that is available for download",
                "a request is made to download the distribution",
                "/v1/catalogs/common/datasets/API_TEST/authorize/token",
                "/v1/catalogs/common/datasets/API_TEST/datasetseries/20220117/distributions/csv",
                uploadHeaders,
                "A,B,C");
    }

    @Pact(provider = "FusionUpload", consumer = "FusionSdk")
    public RequestResponsePact uploadStream(PactDslWithProvider builder) {

        Map<String, String> uploadHeaders = givenUploadHeaders(
                "2022-01-16", "2022-01-16", "2022-01-17", "5", "KPD9WTOuUoQrDwpugLaHblJS+OdUnXaML3YWXla28Rg=");
        return uploadExpectation(
                builder,
                "a distribution that is available for download",
                "a request is made to download the distribution",
                "/v1/catalogs/common/datasets/API_TEST/authorize/token",
                "/v1/catalogs/common/datasets/API_TEST/datasetseries/20220117/distributions/csv",
                uploadHeaders,
                "A,B,C");
    }

    @AfterEach
    @SneakyThrows
    public void cleanupDirectory() {
        Files.deleteIfExists(Paths.get("downloads/common_API_TEST_20220117.csv"));
    }

    @Test
    @SneakyThrows
    @PactTestFor(pactMethod = "uploadFile")
    void testUploadFile(MockServer mockServer) {

        log.atInfo().log("Executing upload file test");

        givenInstanceOfFusionSdk(mockServer);
        givenFileReadForUpload("/common_API_TEST_20220117.csv", "A,B,C");

        Assertions.assertDoesNotThrow(() -> fusion.upload(
                "common",
                "API_TEST",
                "20220117",
                "csv",
                "downloads/common_API_TEST_20220117.csv",
                LocalDate.of(2022, 1, 15),
                LocalDate.of(2022, 1, 16),
                LocalDate.of(2022, 1, 17)));
    }

    @Test
    @PactTestFor(pactMethod = "uploadStream")
    void testUploadStream(MockServer mockServer) {

        log.atInfo().log("Executing upload stream test");

        givenInstanceOfFusionSdk(mockServer);

        Assertions.assertDoesNotThrow(() -> fusion.upload(
                "common",
                "API_TEST",
                "20220117",
                "csv",
                new ByteArrayInputStream("A,B,C".getBytes()),
                LocalDate.of(2022, 1, 16),
                LocalDate.of(2022, 1, 16),
                LocalDate.of(2022, 1, 17)));
    }

    private static void givenFileReadForUpload(String fileName, String body) throws IOException {
        Path downloadsPath = Files.createDirectories(Paths.get("downloads"));
        FileOutputStream fos = new FileOutputStream(downloadsPath.toString() + "/" + fileName);
        fos.write(body.getBytes());
        fos.close();
    }

    private void givenInstanceOfFusionSdk(MockServer mockServer) {
        fusion = Fusion.builder()
                .rootURL(mockServer.getUrl() + FUSION_API_VERSION)
                .bearerToken("my-bearer-token")
                .datasetTokenProvider((catalog, dataset) -> "my-fusion-bearer")
                .build();
    }

    private static Map<String, String> givenUploadHeaders(
            String fromDate, String toDate, String cDate, String length, String digest) {
        Map<String, String> uploadHeaders = new HashMap<>();
        uploadHeaders.put("x-jpmc-distribution-from-date", fromDate);
        uploadHeaders.put("x-jpmc-distribution-to-date", toDate);
        uploadHeaders.put("x-jpmc-distribution-created-date", cDate);
        uploadHeaders.put("Content-Length", length);
        uploadHeaders.put("Digest", "SHA-256=" + digest);
        return uploadHeaders;
    }
}
