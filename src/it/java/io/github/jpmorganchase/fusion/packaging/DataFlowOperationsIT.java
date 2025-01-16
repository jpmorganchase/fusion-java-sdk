package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.DataFlow;
import io.github.jpmorganchase.fusion.model.Dataset;
import io.github.jpmorganchase.fusion.model.Flow;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static io.github.jpmorganchase.fusion.test.TestUtils.listOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DataFlowOperationsIT extends BaseOperationsIT {

    @Test
    public void testCreateInputDataFlow() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SIF0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("flow/dataset-flow-SIF0001-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));


        DataFlow flow = flow("SIF0001", "Input");

        // When & Then
        Assertions.assertDoesNotThrow(flow::create);
    }

    @Test
    public void testCreateOutputDataFlow() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SOF0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("flow/dataset-flow-SOF0001-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataFlow flow = flow("SOF0001", "Output");

        // When & Then
        Assertions.assertDoesNotThrow(flow::create);
    }

    @Test
    public void testCreateDataFlowWithAddedFeedDetails() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SIF0002"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("flow/dataset-flow-SIF0002-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));


        DataFlow flow = flow("SIF0002", "Input")
                .toBuilder()
                .flow(Flow.builder()
                        .flowDirection("Input")
                        .producerApplicationId(Application.builder().sealId("123456").build())
                        .consumerApplicationId(Application.builder().sealId("456789").build())
                        .consumerApplicationId(Application.builder().sealId("901234").build())
                        .flowType("raw")
                        .startTime("10:00")
                        .endTime("13:00")
                        .timeZone("UTC+1")
                        .build())
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(flow::create);
    }

    @Test
    public void testCreateFlowOverrideDefaultCatalog() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/foobar/datasets/SIF0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("flow/dataset-flow-SIF0001-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataFlow flow = flow("SIF0001", "Input")
                .toBuilder()
                .catalogIdentifier("foobar")
                .build();
        // When & Then
        Assertions.assertDoesNotThrow(flow::create);
    }

    @Test
    public void testUpdateDataFlow() {
        // Given
        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/datasets/SIF0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("flow/dataset-flow-SIF0001-update-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataFlow flow = flow("SIF0001", "Input")
                .toBuilder()
                .description("Updated Sample input flow description SIF0001")
                .build();


        // When & Then
        Assertions.assertDoesNotThrow(flow::update);
    }

    @Test
    public void testUpdateDataFlowRetrievedFromListDataFlows() {
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common/datasets"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("flow/multiple-flow-response.json")));

        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/datasets/SIF0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("flow/dataset-flow-SIF0001-update-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Map<String, DataFlow> flows = getSdk().listDataFlows("common", "SIF0001", true);
        DataFlow original = flows.get("SIF0001");

        // When
        DataFlow amended = original
                .toBuilder()
                .description("Updated Sample input flow description SIF0001")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(amended::update);
    }


    @Test
    public void testDeleteDataset() {
        // Given
        wireMockRule.stubFor(WireMock.delete(WireMock.urlEqualTo("/catalogs/common/datasets/SIF0001"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataFlow flow = getSdk().builders().dataFlow()
                .identifier("SIF0001")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(flow::delete);
    }

    @Test
    public void testDeleteDatasetWithCatalogOverride() {
        // Given
        wireMockRule.stubFor(WireMock.delete(WireMock.urlEqualTo("/catalogs/foobar/datasets/SIF0001"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataFlow flow = getSdk().builders().dataFlow()
                .identifier("SIF0001")
                .catalogIdentifier("foobar")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(flow::delete);
    }

    @Test
    public void testListDataFlows() {
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common/datasets"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("flow/multiple-flow-response.json")));

        // When
        Map<String, DataFlow> flows = getSdk().listDataFlows();

        // Then Verify the response
        assertThat(flows.containsKey("SIF0001"), is(equalTo(true)));
        assertThat(flows.containsKey("SOF0001"), is(equalTo(true)));
    }

    @Test
    public void testListDatasetsUsingIdContains() {
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common/datasets"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("flow/multiple-flow-response.json")));

        // When
        Map<String, DataFlow> datasets = getSdk().listDataFlows("common", "SIF0001", true);

        // Then Verify the response
        assertThat(datasets.size(), is(equalTo(1)));
        assertThat(datasets.containsKey("SIF0001"), is(equalTo(true)));
    }

    private DataFlow flow(String identifier, String direction){
        return getSdk().builders().dataFlow()
                .identifier(identifier)
                .description("Sample flow description " + identifier)
                .linkedEntity(identifier + "/")
                .title("Sample Flow | North America " + identifier)
                .frequency("Daily")
                .publisher("Publisher " + identifier)
                .varArg("category", listOf("Category " + identifier))
                .varArg("createdDate", "2022-02-06")
                .varArg("coverageStartDate", "2022-02-06")
                .varArg("coverageEndDate", "2023-03-09")
                .varArg("isThirdPartyData", Boolean.FALSE)
                .varArg("isInternalOnlyDataset", Boolean.FALSE)
                .varArg("language", "English")
                .varArg("maintainer", "Maintainer " + identifier)
                .varArg("modifiedDate", "2023-03-09")
                .varArg("region", listOf("North America"))
                .varArg("source", listOf("Source System " + identifier))
                .varArg("subCategory", listOf("Subcategory " + identifier))
                .varArg("tag", listOf("Tag" + identifier))
                .varArg("isRestricted", Boolean.FALSE)
                .varArg("isRawData", Boolean.FALSE)
                .varArg("hasSample", Boolean.FALSE)
                .applicationId(Application.builder().sealId("12345").build())
                .flow(
                        Flow.builder()
                                .flowDirection(direction)
                                .producerApplicationId(Application.builder().sealId("123456").build())
                                .consumerApplicationId(Application.builder().sealId("456789").build())
                                .consumerApplicationId(Application.builder().sealId("901234").build())
                                .build()
                )
                .build();
    }

}
