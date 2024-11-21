package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.model.Dataset;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static io.github.jpmorganchase.fusion.test.TestUtils.listOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

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
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-SD0001-create-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-create-response.json")));

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
        //TODO :: Contract for response of dataset.create() needs to be decided
    }

    @Test
    public void testCreateDatasetWithVarArgs() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SD0002"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-SD0002-create-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-create-response.json")));

        Dataset dataset = sdk.builders().dataset()
                .identifier("SD0002")
                .description("Sample dataset description 2")
                .linkedEntity("SD0002/")
                .title("Sample Dataset 2 | North America")
                .frequency("Daily")
                .varArg("category", listOf("Category 2"))
                .varArg("createdDate", "2022-02-06")
                .varArg("coverageStartDate", "2022-02-06")
                .varArg("coverageEndDate", "2023-03-09")
                .varArg("isThirdPartyData", Boolean.FALSE)
                .varArg("isInternalOnlyDataset", Boolean.FALSE)
                .varArg("language", "English")
                .varArg("maintainer", "Maintainer 2")
                .varArg("modifiedDate", "2023-03-09")
                .varArg("publisher", "Publisher 2")
                .varArg("region", listOf("North America"))
                .varArg("source", listOf("Source System 2"))
                .varArg("subCategory", listOf("Subcategory 2"))
                .varArg("tag", listOf("Tag2"))
                .varArg("isRestricted", Boolean.FALSE)
                .varArg("isRawData", Boolean.FALSE)
                .varArg("hasSample", Boolean.FALSE)
                .build();

        // When
        dataset.create();


        // Then Verify the response
        //TODO :: Contract for response of dataset.create() needs to be decided
    }

    @Test
    public void testCreateDatasetOverrideDefaultCatalog() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/foobar/datasets/SD0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-SD0001-create-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-create-response.json")));

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
        //TODO :: Contract for response of dataset.create() needs to be decided
    }

    @Test
    public void testListDatasets() {
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common/datasets"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/multiple-dataset-response.json")));

        // When
        Map<String, Dataset> datasets = sdk.listDatasets();

        // Then Verify the response
        MatcherAssert.assertThat(datasets.containsKey("SD0001"), is(equalTo(true)));
        MatcherAssert.assertThat(datasets.containsKey("SD0002"), is(equalTo(true)));
        MatcherAssert.assertThat(datasets.containsKey("SD0003"), is(equalTo(true)));

    }

    @Test
    public void testListDatasetsUsingIdContains() {
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common/datasets"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/multiple-dataset-response.json")));

        // When
        Map<String, Dataset> datasets = sdk.listDatasets("common", "SD0001", true);

        // Then Verify the response
        MatcherAssert.assertThat(datasets.containsKey("SD0001"), is(equalTo(true)));
        MatcherAssert.assertThat(datasets.containsKey("SD0002"), is(equalTo(false)));
        MatcherAssert.assertThat(datasets.containsKey("SD0003"), is(equalTo(false)));

    }

}
