package io.github.jpmorganchase.fusion.parsing;

import static io.github.jpmorganchase.fusion.test.TestUtils.listOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.DataFlow;
import io.github.jpmorganchase.fusion.model.Dataset;
import io.github.jpmorganchase.fusion.model.Flow;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GsonAPIResponseParserDatasetFlowTest {

    private static final String singleDatasetInputFlowJson =
            loadTestResource("single-dataset-input-flow-response.json");

    private static final String singleDatasetOutputFlowJson =
            loadTestResource("single-dataset-output-flow-response.json");
    private static final String multipleDatasetJson = loadTestResource("multiple-dataset-flow-response.json");

    private final DataFlow expectedFlow = DataFlow.builder()
            .identifier("SIF0001")
            .description("Sample input flow description 1")
            .linkedEntity("SIF0001/")
            .frequency("Daily")
            .publisher("Publisher 1")
            .fusion(fusion)
            .title("Sample Input Flow 1 | North America")
            .varArg("category", listOf("Category 1"))
            .varArg("createdDate", "2022-02-05")
            .varArg("coverageStartDate", "2022-02-05")
            .varArg("coverageEndDate", "2023-03-08")
            .varArg("isThirdPartyData", Boolean.FALSE)
            .varArg("isInternalOnlyDataset", Boolean.FALSE)
            .varArg("language", "English")
            .varArg("maintainer", "Maintainer 1")
            .varArg("modifiedDate", "2023-03-08")
            .varArg("region", listOf("North America"))
            .varArg("source", listOf("Source System 1"))
            .varArg("subCategory", listOf("Subcategory 1"))
            .varArg("tag", listOf("Tag1"))
            .varArg("isRestricted", Boolean.FALSE)
            .varArg("isRawData", Boolean.FALSE)
            .varArg("hasSample", Boolean.FALSE)
            .catalogIdentifier("foobar")
            .applicationId(Application.builder().sealId("12345").build())
            .flow(Flow.builder()
                    .flowDirection("Input")
                    .producerApplicationId(
                            Application.builder().sealId("123456").build())
                    .consumerApplicationId(
                            Application.builder().sealId("456789").build())
                    .build())
            .build();

    private final DataFlow expectedFlowOne = DataFlow.builder()
            .identifier("SOF0001")
            .description("Sample output flow description 1")
            .linkedEntity("SOF0001/")
            .frequency("Daily")
            .publisher("Publisher 1")
            .fusion(fusion)
            .title("Sample Output Flow 1 | North America")
            .varArg("category", listOf("Category 1"))
            .varArg("createdDate", "2022-02-05")
            .varArg("coverageStartDate", "2022-02-05")
            .varArg("coverageEndDate", "2023-03-08")
            .varArg("isThirdPartyData", Boolean.FALSE)
            .varArg("isInternalOnlyDataset", Boolean.FALSE)
            .varArg("language", "English")
            .varArg("maintainer", "Maintainer 1")
            .varArg("modifiedDate", "2023-03-08")
            .varArg("region", listOf("North America"))
            .varArg("source", listOf("Source System 1"))
            .varArg("subCategory", listOf("Subcategory 1"))
            .varArg("tag", listOf("Tag1"))
            .varArg("isRestricted", Boolean.FALSE)
            .varArg("isRawData", Boolean.FALSE)
            .varArg("hasSample", Boolean.FALSE)
            .catalogIdentifier("foobar")
            .applicationId(Application.builder().sealId("12345").build())
            .flow(Flow.builder()
                    .flowDirection("Output")
                    .producerApplicationId(
                            Application.builder().sealId("123456").build())
                    .consumerApplicationId(
                            Application.builder().sealId("456789").build())
                    .build())
            .build();

    private final DataFlow expectedFlowTwo = DataFlow.builder()
            .identifier("SIF0002")
            .description("Sample input flow description 2")
            .linkedEntity("SIF0002/")
            .frequency("Daily")
            .title("Sample Input Flow 2 | North America")
            .publisher("Publisher 2")
            .fusion(fusion)
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
            .catalogIdentifier("foobar")
            .applicationId(Application.builder().sealId("12345").build())
            .flow(Flow.builder()
                    .flowDirection("Input")
                    .producerApplicationId(
                            Application.builder().sealId("123456").build())
                    .consumerApplicationId(
                            Application.builder().sealId("456789").build())
                    .build())
            .build();

    private static final Fusion fusion = Mockito.mock(Fusion.class);

    private static final APIResponseParser responseParser = GsonAPIResponseParser.builder()
            .gson(DefaultGsonConfig.gson())
            .fusion(fusion)
            .build();

    @Test
    public void singleDatasetInputFlowInResourcesParsesCorrectly() {
        Map<String, DataFlow> dataFlowMap = responseParser.parseDataFlowResponse(singleDatasetInputFlowJson, "foobar");
        assertThat(dataFlowMap.size(), is(1));

        DataFlow actualDataFlow = dataFlowMap.get("SIF0001");
        assertThat(actualDataFlow, is(equalTo(expectedFlow)));
    }

    @Test
    public void singleDatasetOutputFlowInResourcesParsesCorrectly() {
        Map<String, DataFlow> dataFlowMap = responseParser.parseDataFlowResponse(singleDatasetOutputFlowJson, "foobar");
        assertThat(dataFlowMap.size(), is(1));

        Dataset testDatasetResponse = dataFlowMap.get("SOF0001");
        assertThat(testDatasetResponse, is(equalTo(expectedFlowOne)));
    }

    @Test
    public void multipleCatalogsInResourcesParseCorrectly() {
        Map<String, DataFlow> dataFlowMap = responseParser.parseDataFlowResponse(multipleDatasetJson, "foobar");
        assertThat(dataFlowMap.size(), is(3));

        Dataset testDatasetResponse = dataFlowMap.get("SIF0001");
        assertThat(testDatasetResponse, is(equalTo(expectedFlow)));

        Dataset testDatasetResponse2 = dataFlowMap.get("SOF0001");
        assertThat(testDatasetResponse2, is(equalTo(expectedFlowOne)));

        Dataset testDatasetResponse3 = dataFlowMap.get("SIF0002");
        assertThat(testDatasetResponse3, is(equalTo(expectedFlowTwo)));
    }

    @Test
    public void singleFlowWithAdditionalFlowDetailsParsesCorrectly() {
        DataFlow expectedFlow = DataFlow.builder()
                .identifier("SIF0001")
                .description("Sample input flow description 1")
                .linkedEntity("SIF0001/")
                .frequency("Daily")
                .publisher("Publisher 1")
                .fusion(fusion)
                .title("Sample Input Flow 1 | North America")
                .varArg("category", listOf("Category 1"))
                .varArg("createdDate", "2022-02-05")
                .varArg("coverageStartDate", "2022-02-05")
                .varArg("coverageEndDate", "2023-03-08")
                .varArg("isThirdPartyData", Boolean.FALSE)
                .varArg("isInternalOnlyDataset", Boolean.FALSE)
                .varArg("language", "English")
                .varArg("maintainer", "Maintainer 1")
                .varArg("modifiedDate", "2023-03-08")
                .varArg("region", listOf("North America"))
                .varArg("source", listOf("Source System 1"))
                .varArg("subCategory", listOf("Subcategory 1"))
                .varArg("tag", listOf("Tag1"))
                .varArg("isRestricted", Boolean.FALSE)
                .varArg("isRawData", Boolean.FALSE)
                .varArg("hasSample", Boolean.FALSE)
                .catalogIdentifier("foobar")
                .applicationId(Application.builder().sealId("12345").build())
                .flow(Flow.builder()
                        .flowDirection("Input")
                        .flowType("raw")
                        .startTime("10:00")
                        .endTime("13:00")
                        .timeZone("UTC+1")
                        .producerApplicationId(
                                Application.builder().sealId("123456").build())
                        .consumerApplicationId(
                                Application.builder().sealId("456789").build())
                        .build())
                .build();

        String jsonBody = loadTestResource("single-dataset-input-flow-with-feed-details-response.json");
        Map<String, DataFlow> dataFlowMap = responseParser.parseDataFlowResponse(jsonBody, "foobar");
        assertThat(dataFlowMap.size(), is(1));

        DataFlow actualDataFlow = dataFlowMap.get("SIF0001");
        assertThat(actualDataFlow, is(equalTo(expectedFlow)));
    }

    private static String loadTestResource(String resourceName) {
        URL url = GsonAPIResponseParser.class.getResource(resourceName);
        try {
            Path path = Paths.get(url.toURI());
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data", e);
        }
    }
}
