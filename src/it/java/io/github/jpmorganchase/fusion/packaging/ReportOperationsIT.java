package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.Dataset;
import io.github.jpmorganchase.fusion.model.Flow;
import io.github.jpmorganchase.fusion.model.ReportObj;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static io.github.jpmorganchase.fusion.test.TestUtils.listOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ReportOperationsIT extends BaseOperationsIT {

    @Test
    public void testCreateReport() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SR0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("report/dataset-report-SR0001-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        ReportObj report = reportSrOne();

        // When & Then
        Assertions.assertDoesNotThrow(report::create);
    }

    @Test
    public void testCreateReportOverrideDefaultCatalog() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/foobar/datasets/SR0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("report/dataset-report-SR0001-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        ReportObj report = reportSrOne().toBuilder().catalogIdentifier("foobar").build();

        // When & Then
        Assertions.assertDoesNotThrow(report::create);
    }

    @Test
    public void testUpdateReport() {
        // Given
        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/datasets/SR0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("report/dataset-report-SR0001-update-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));


        ReportObj report = reportSrOne().toBuilder().description("Updated Sample report description 1").build();


        // When & Then
        Assertions.assertDoesNotThrow(report::update);
    }

    @Test
    public void testUpdateReportRetrievedFromListReports() {
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common/datasets"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("dataset/multiple-dataset-response.json")));

        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/datasets/SR0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset-report-SR0001-update-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
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

        // When & Then
        Assertions.assertDoesNotThrow(amendedDataset::update);
    }



    @Test
    public void testDeleteReport() {
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
    public void testDeleteReportWithCatalogOverride() {
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
    public void testListReports() {
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
    public void testListReportsUsingIdContains() {
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

    private ReportObj reportSrOne() {
        return getSdk().builders().report()
                .identifier("SR0001")
                .description("Sample report description 1")
                .linkedEntity("SR0001/")
                .title("Sample Report 1 | North America")
                .frequency("Daily")
                .publisher("Publisher 1")
                .varArg("category", listOf("Category 1"))
                .varArg("createdDate", "2022-02-06")
                .varArg("coverageStartDate", "2022-02-06")
                .varArg("coverageEndDate", "2023-03-09")
                .varArg("isThirdPartyData", Boolean.FALSE)
                .varArg("isInternalOnlyDataset", Boolean.FALSE)
                .varArg("language", "English")
                .varArg("maintainer", "Maintainer 1")
                .varArg("modifiedDate", "2023-03-09")
                .varArg("region", listOf("North America"))
                .varArg("source", listOf("Source System 1"))
                .varArg("subCategory", listOf("Subcategory 1"))
                .varArg("tag", listOf("Tag1"))
                .varArg("isRestricted", Boolean.FALSE)
                .varArg("isRawData", Boolean.FALSE)
                .varArg("hasSample", Boolean.FALSE)
                .applicationId(Application.builder().sealId("12345").build())
                .tier("Tier 1")
                .build();
    }

}
