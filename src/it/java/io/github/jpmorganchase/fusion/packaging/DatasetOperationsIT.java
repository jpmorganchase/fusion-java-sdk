package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.Dataset;
import io.github.jpmorganchase.fusion.model.Report;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static io.github.jpmorganchase.fusion.test.TestUtils.listOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DatasetOperationsIT extends BaseOperationsIT {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void testCreateDataset() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SD0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-SD0001-create-request.json")))
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

        Dataset dataset = getSdk().builders().dataset()
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

        Dataset dataset = getSdk().builders().dataset()
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
    public void testCreateDatasetOfTypeReport() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SR0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-report-SR0001-create-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-create-response.json")));

        Dataset dataset = getSdk().builders().dataset()
                .identifier("SR0001")
                .description("Sample report description 1")
                .linkedEntity("SR0001/")
                .title("Sample Report 1 | North America")
                .frequency("Daily")
                .varArg("category", listOf("Category 1"))
                .varArg("createdDate", "2022-02-06")
                .varArg("coverageStartDate", "2022-02-06")
                .varArg("coverageEndDate", "2023-03-09")
                .varArg("isThirdPartyData", Boolean.FALSE)
                .varArg("isInternalOnlyDataset", Boolean.FALSE)
                .varArg("language", "English")
                .varArg("maintainer", "Maintainer 1")
                .varArg("modifiedDate", "2023-03-09")
                .varArg("publisher", "Publisher 1")
                .varArg("region", listOf("North America"))
                .varArg("source", listOf("Source System 1"))
                .varArg("subCategory", listOf("Subcategory 1"))
                .varArg("tag", listOf("Tag1"))
                .varArg("isRestricted", Boolean.FALSE)
                .varArg("isRawData", Boolean.FALSE)
                .varArg("hasSample", Boolean.FALSE)
                .applicationId(Application.builder().sealId("12345").build())
                .report(Report.builder().tier("Tier 1").build())
                .build();

        // When
        dataset.create();


        // Then Verify the response
        //TODO :: Contract for response of dataset.create() needs to be decided
    }

    @Test
    public void testCreateDatasetOfTypeFlowInput() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SIF0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-flow-SIF0001-create-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-create-response.json")));

        Dataset dataset = getSdk().builders().dataset()
                .identifier("SIF0001")
                .description("Sample input flow description 1")
                .linkedEntity("SIF0001/")
                .title("Sample Input Flow 1 | North America")
                .frequency("Daily")
                .varArg("category", listOf("Category 1"))
                .varArg("createdDate", "2022-02-06")
                .varArg("coverageStartDate", "2022-02-06")
                .varArg("coverageEndDate", "2023-03-09")
                .varArg("isThirdPartyData", Boolean.FALSE)
                .varArg("isInternalOnlyDataset", Boolean.FALSE)
                .varArg("language", "English")
                .varArg("maintainer", "Maintainer 1")
                .varArg("modifiedDate", "2023-03-09")
                .varArg("publisher", "Publisher 1")
                .varArg("region", listOf("North America"))
                .varArg("source", listOf("Source System 1"))
                .varArg("subCategory", listOf("Subcategory 1"))
                .varArg("tag", listOf("Tag1"))
                .varArg("isRestricted", Boolean.FALSE)
                .varArg("isRawData", Boolean.FALSE)
                .varArg("hasSample", Boolean.FALSE)
                .applicationId(Application.builder().sealId("12345").build())
                .inputFlow(
                        Application.builder().sealId("123456").build(),
                        new Application[] {Application.builder().sealId("456789").build()}
                )
                .build();

        // When
        dataset.create();


        // Then Verify the response
        //TODO :: Contract for response of dataset.create() needs to be decided
    }

    @Test
    public void testCreateDatasetOfTypeFlowOutput() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SOF0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-flow-SOF0001-create-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-create-response.json")));

        Dataset dataset = getSdk().builders().dataset()
                .identifier("SOF0001")
                .description("Sample output flow description 1")
                .linkedEntity("SOF0001/")
                .title("Sample Output Flow 1 | North America")
                .frequency("Daily")
                .varArg("category", listOf("Category 1"))
                .varArg("createdDate", "2022-02-06")
                .varArg("coverageStartDate", "2022-02-06")
                .varArg("coverageEndDate", "2023-03-09")
                .varArg("isThirdPartyData", Boolean.FALSE)
                .varArg("isInternalOnlyDataset", Boolean.FALSE)
                .varArg("language", "English")
                .varArg("maintainer", "Maintainer 1")
                .varArg("modifiedDate", "2023-03-09")
                .varArg("publisher", "Publisher 1")
                .varArg("region", listOf("North America"))
                .varArg("source", listOf("Source System 1"))
                .varArg("subCategory", listOf("Subcategory 1"))
                .varArg("tag", listOf("Tag1"))
                .varArg("isRestricted", Boolean.FALSE)
                .varArg("isRawData", Boolean.FALSE)
                .varArg("hasSample", Boolean.FALSE)
                .applicationId(Application.builder().sealId("12345").build())
                .outputFlow(
                        Application.builder().sealId("123456").build(),
                        new Application[] {Application.builder().sealId("456789").build()}
                )
                .build();

        // When
        dataset.create();


        // Then Verify the response
        //TODO :: Contract for response of dataset.create() needs to be decided
    }

    @Test
    public void testUpdateDataset() {
        // Given
        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/datasets/SD0004"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-SD0004-update-request.json")))
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
                .build();

        // When
        dataset.update();

        // Then Verify the response
        //TODO :: Contract for response of dataset.update() needs to be decided
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

        amendedDataset.update();

        // Then Verify the response
        //TODO :: Contract for response of dataset.update() needs to be decided
    }

    @Test
    public void testUpdateDatasetOfTypeReportRetrievedFromListDatasets() {
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common/datasets"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/multiple-dataset-response.json")));

        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/datasets/SR0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-report-SR0001-update-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-create-response.json")));

        Map<String, Dataset> datasets = getSdk().listDatasets("common", "SR0001", true);
        Dataset originalDataset = datasets.get("SR0001");

        // When
        Dataset amendedDataset = originalDataset
                .toBuilder()
                .description("Updated Sample report description 1")
                .build();

        amendedDataset.update();

        // Then Verify the response
        //TODO :: Contract for response of dataset.update() needs to be decided
    }

    @Test
    public void testUpdateDatasetOfTypeFlowRetrievedFromListDatasets() {
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common/datasets"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/multiple-dataset-response.json")));

        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/datasets/SIF0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-flow-SIF0001-update-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/dataset-create-response.json")));

        Map<String, Dataset> datasets = getSdk().listDatasets("common", "SIF0001", true);
        Dataset originalDataset = datasets.get("SIF0001");

        // When
        Dataset amendedDataset = originalDataset
                .toBuilder()
                .description("Updated Sample input flow description 1")
                .build();

        amendedDataset.update();

        // Then Verify the response
        //TODO :: Contract for response of dataset.update() needs to be decided
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

        // When
        dataset.delete();

        // Then Verify the response
        //TODO :: Contract for response of dataset.delete() needs to be decided
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

        // When
        dataset.delete();

        // Then Verify the response
        //TODO :: Contract for response of dataset.delete() needs to be decided
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
