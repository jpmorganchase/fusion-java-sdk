package io.github.jpmorganchase.fusion.serializing;

import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.Dataset;
import io.github.jpmorganchase.fusion.model.Flow;
import io.github.jpmorganchase.fusion.model.Report;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.jpmorganchase.fusion.test.TestUtils.listOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GsonAPIRequestSerializerDatasetTest {

    @Test
    public void testDatasetSerializesCorrectly() {
        // Given
        Dataset dataset = Dataset.builder()
                .identifier("SD0001")
                .description("Sample dataset description 1")
                .linkedEntity("SD0001/")
                .title("Sample Dataset 1 | North America")
                .frequency("Daily")
                .publisher("Publisher 1")
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
                .applicationId(Application.builder().sealId("12345").build())
                .report(Report.builder().tier("Tier 1").build())
                .fusion(Mockito.mock(Fusion.class))
                .catalogIdentifier("foobar")
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(dataset);

        // Then
        String expected = loadTestResource("dataset-request.json");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void testDatasetWithoutVarArgsSerializesCorrectly() {
        // Given
        Dataset dataset = Dataset.builder()
                .identifier("SD0001")
                .description("Sample dataset description 1")
                .linkedEntity("SD0001/")
                .title("Sample Dataset 1 | North America")
                .frequency("Daily")
                .publisher("Publisher 1")
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(dataset);

        // Then
        String expected = loadTestResource("dataset-no-varags-request.json");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void testDatasetOfTypeFlowInput() {
        // Given
        Dataset dataset = Dataset.builder()
                .identifier("SIF0001")
                .description("Sample input flow dataset description 1")
                .linkedEntity("SIF0001/")
                .title("Sample Input Flow Dataset 1 | North America")
                .frequency("Daily")
                .publisher("Publisher 1")
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
                .applicationId(Application.builder().sealId("12345").build())
                .flow(Flow.builder()
                        .flowDirection("Input")
                        .producerApplicationId(
                                Application.builder().sealId("123456").build())
                        .consumerApplicationId(
                                Application.builder().sealId("456789").build())
                        .build())
                .fusion(Mockito.mock(Fusion.class))
                .catalogIdentifier("foobar")
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(dataset);

        // Then
        String expected = loadTestResource("dataset-flow-input-request.json");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void testDatasetOfTypeFlowOutput() {
        // Given
        Dataset dataset = Dataset.builder()
                .identifier("SOF0001")
                .description("Sample output flow dataset description 1")
                .linkedEntity("SOF0001/")
                .title("Sample Output Flow Dataset 1 | North America")
                .frequency("Daily")
                .publisher("Publisher 1")
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
                .applicationId(Application.builder().sealId("12345").build())
                .flow(Flow.builder()
                        .flowDirection("Output")
                        .producerApplicationId(
                                Application.builder().sealId("123456").build())
                        .consumerApplicationId(
                                Application.builder().sealId("456789").build())
                        .build())
                .catalogIdentifier("foobar")
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(dataset);

        // Then
        String expected = loadTestResource("dataset-flow-output-request.json");
        assertThat(actual, is(equalTo(expected)));
    }

    private static String loadTestResource(String resourceName) {
        URL url = GsonAPIRequestSerializer.class.getResource(resourceName);
        try {
            Path path = Paths.get(url.toURI());
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data", e);
        }
    }
}
