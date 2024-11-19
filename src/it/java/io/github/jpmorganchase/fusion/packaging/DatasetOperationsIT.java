package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.model.Dataset;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static io.github.jpmorganchase.fusion.test.TestUtils.listOf;

@ExtendWith(WireMockExtension.class)
public class DatasetOperationsIT {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @RegisterExtension
    public static WireMockExtension wireMockRule = WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig().dynamicPort()).build();

    private Fusion sdk;

    @BeforeEach
    public void setUp() {
        int port = wireMockRule.getRuntimeInfo().getHttpPort();
        logger.debug("Wiremock is configured to port {}", port);

        sdk = Fusion.builder()
                .bearerToken("my-token")
                .configuration(FusionConfiguration.builder()
                        .rootURL("http://localhost:" + port + "/")
                .build()).build();
    }

    @Test
    public void testCreateDataset() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SD0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset_SD0001_create_request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("dataset/dataset_create_response.json")));

        Dataset dataset = sdk.builders().dataset()
                .identifier("SD0001")
                .description("Sample dataset description 1")
                .linkedEntity("SD0001/")
                .title("Sample Dataset 1 | North America")
                .frequency("Daily")
                .build();

        // When
        dataset.create();

        // Then Verify the response
    }

    @Test
    public void testCreateDatasetWithVarArgs() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SD0002"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset_SD0002_create_request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("dataset/dataset_create_response.json")));

        Dataset dataset = sdk.builders().dataset()
                .identifier("SD0002")
                .description("Sample dataset description 1")
                .linkedEntity("SD0002/")
                .title("Sample Dataset 1 | North America")
                .frequency("Daily")
                .varArg("category", listOf("Category 1"))
                .varArg("createdDate", "2022-02-05")
                .varArg("coverageStartDate", "2022-02-05")
                .varArg("coverageEndDate", "2023-03-08")
                .varArg("isThirdPartyData", Boolean.FALSE)
                .varArg("isInternalOnlyDataset", Boolean.FALSE)
                .varArg("language", "English")
                .varArg("maintainer", "Maintainer 1")
                .varArg("modifiedDate", "2023-03-08")
                .varArg("publisher", "Publisher 1")
                .varArg("region", listOf("North America"))
                .varArg("source", listOf("Source System 1"))
                .varArg("subCategory", listOf("Subcategory 1"))
                .varArg("tag", listOf("Tag1"))
                .varArg("isRestricted", Boolean.FALSE)
                .varArg("isRawData", Boolean.FALSE)
                .varArg("hasSample", Boolean.FALSE)
                .build();

        // When
        dataset.create();


        // Then Verify the response
    }

    @Test
    public void testCreateDatasetOverrideDefaultCatalog() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/foobar/datasets/SD0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset_SD0001_create_request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("dataset/dataset_create_response.json")));

        Dataset dataset = sdk.builders().dataset()
                .identifier("SD0001")
                .description("Sample dataset description 1")
                .linkedEntity("SD0001/")
                .title("Sample Dataset 1 | North America")
                .frequency("Daily")
                .catalogIdentifier("foobar")
                .build();

        // When
        dataset.create();

        // Then Verify the response
    }

}
