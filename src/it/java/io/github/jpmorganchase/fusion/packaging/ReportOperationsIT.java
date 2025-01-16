package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.Report;
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

        Report report = reportSrOne();

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

        Report report = reportSrOne().toBuilder().catalogIdentifier("foobar").build();

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


        Report report = reportSrOne().toBuilder().description("Updated Sample report description 1").build();


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
                        .withBodyFile("report/multiple-reports-response.json")));

        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/datasets/SR0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("report/dataset-report-SR0001-update-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Map<String, Report> reports = getSdk().listReports("common", "SR0001", true);
        Report originalReport = reports.get("SR0001");

        // When
        Report amendedReport = originalReport
                .toBuilder()
                .description("Updated Sample report description 1")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(amendedReport::update);
    }



    @Test
    public void testDeleteReport() {
        // Given
        wireMockRule.stubFor(WireMock.delete(WireMock.urlEqualTo("/catalogs/common/datasets/SR0001"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Report report = getSdk().builders().report()
                .identifier("SR0001")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(report::delete);
    }

    @Test
    public void testDeleteReportWithCatalogOverride() {
        // Given
        wireMockRule.stubFor(WireMock.delete(WireMock.urlEqualTo("/catalogs/foobar/datasets/SR0001"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Report report = getSdk().builders().report()
                .identifier("SR0001")
                .catalogIdentifier("foobar")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(report::delete);
    }

    @Test
    public void testListReports() {
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common/datasets"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("report/multiple-reports-response.json")));

        // When
        Map<String, Report> reports = getSdk().listReports();

        // Then Verify the response
        assertThat(reports.size(), is(equalTo(2)));
        assertThat(reports.containsKey("SR0001"), is(equalTo(true)));
        assertThat(reports.containsKey("SR0002"), is(equalTo(true)));
    }

    @Test
    public void testListReportsUsingIdContains() {
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common/datasets"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("report/multiple-reports-response.json")));

        // When
        Map<String, Report> reports = getSdk().listReports("common", "SR0001", true);

        // Then Verify the response
        assertThat(reports.size(), is(equalTo(1)));
        assertThat(reports.containsKey("SR0001"), is(equalTo(true)));
    }

    private Report reportSrOne() {
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
