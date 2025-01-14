package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static io.github.jpmorganchase.fusion.test.TestUtils.listOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DatasetOperationsIT extends BaseOperationsIT {

    @Test
    public void testCreateDataset() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SD0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-SD0001-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-create-response.json")));

        Dataset dataset = getSdk().builders().dataset()
                .identifier("SD0001")
                .description("Sample dataset description 1")
                .linkedEntity("SD0001/")
                .title("Sample Dataset 1 | North America")
                .frequency("Daily")
                .publisher("Publisher 1")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(dataset::create);
    }

    @Test
    public void testCreateDatasetWithVarArgs() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SD0002"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-SD0002-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-create-response.json")));

        Dataset dataset = getSdk().builders().dataset()
                .identifier("SD0002")
                .description("Sample dataset description 2")
                .linkedEntity("SD0002/")
                .title("Sample Dataset 2 | North America")
                .frequency("Daily")
                .publisher("Publisher 2")
                .varArg("category", listOf("Category 2"))
                .varArg("createdDate", "2022-02-06")
                .varArg("coverageStartDate", "2022-02-06")
                .varArg("coverageEndDate", "2023-03-09")
                .varArg("isThirdPartyData", Boolean.FALSE)
                .varArg("isInternalOnlyDataset", Boolean.FALSE)
                .varArg("language", "English")
                .varArg("maintainer", "Maintainer 2")
                .varArg("modifiedDate", "2023-03-09")
                .varArg("region", listOf("North America"))
                .varArg("source", listOf("Source System 2"))
                .varArg("subCategory", listOf("Subcategory 2"))
                .varArg("tag", listOf("Tag2"))
                .varArg("isRestricted", Boolean.FALSE)
                .varArg("isRawData", Boolean.FALSE)
                .varArg("hasSample", Boolean.FALSE)
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(dataset::create);
    }

    @Test
    public void testCreateDatasetOverrideDefaultCatalog() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/foobar/datasets/SD0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-SD0001-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-create-response.json")));

        Dataset dataset = getSdk().builders().dataset()
                .identifier("SD0001")
                .description("Sample dataset description 1")
                .linkedEntity("SD0001/")
                .title("Sample Dataset 1 | North America")
                .frequency("Daily")
                .catalogIdentifier("foobar")
                .publisher("Publisher 1")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(dataset::create);
    }


    @Test
    public void testUpdateDataset() {
        // Given
        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/datasets/SD0004"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-SD0004-update-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-update-response.json")));

        Dataset dataset = getSdk().builders().dataset()
                .identifier("SD0004")
                .description("New Sample dataset description 4")
                .linkedEntity("SD0004/")
                .title("Sample Dataset 4 | North America")
                .frequency("Daily")
                .publisher("Publisher 4")
                .build();


        // When & Then
        Assertions.assertDoesNotThrow(dataset::update);
    }

    @Test
    public void testUpdateDatasetRetrievedFromListDatasets() {
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common/datasets"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/multiple-dataset-response.json")));

        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/datasets/SD0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-SD0001-update-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-create-response.json")));

        Map<String, Dataset> datasets = getSdk().listDatasets("common", "SD0001", true);
        Dataset originalDataset = datasets.get("SD0001");

        // When
        Dataset amendedDataset = originalDataset
                .toBuilder()
                .description("Updated Sample dataset description 1")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(amendedDataset::update);
    }

    @Test
    public void testDeleteDataset() {
        // Given
        wireMockRule.stubFor(WireMock.delete(WireMock.urlEqualTo("/catalogs/common/datasets/SD0001"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-delete-response.json")));

        Dataset dataset = getSdk().builders().dataset()
                .identifier("SD0001")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(dataset::delete);
    }

    @Test
    public void testDeleteDatasetWithCatalogOverride() {
        // Given
        wireMockRule.stubFor(WireMock.delete(WireMock.urlEqualTo("/catalogs/foobar/datasets/SD0001"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-delete-response.json")));

        Dataset dataset = getSdk().builders().dataset()
                .identifier("SD0001")
                .catalogIdentifier("foobar")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(dataset::delete);
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
        Map<String, Dataset> datasets = getSdk().listDatasets();

        // Then Verify the response
        assertThat(datasets.containsKey("SD0001"), is(equalTo(true)));
        assertThat(datasets.containsKey("SD0002"), is(equalTo(true)));
        assertThat(datasets.containsKey("SD0003"), is(equalTo(true)));
        assertThat(datasets.containsKey("SR0001"), is(equalTo(true)));
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
        Map<String, Dataset> datasets = getSdk().listDatasets("common", "SD0001", true);

        // Then Verify the response
        assertThat(datasets.containsKey("SD0001"), is(equalTo(true)));
        assertThat(datasets.containsKey("SD0002"), is(equalTo(false)));
        assertThat(datasets.containsKey("SD0003"), is(equalTo(false)));
        assertThat(datasets.containsKey("SR0001"), is(equalTo(false)));

    }

}
