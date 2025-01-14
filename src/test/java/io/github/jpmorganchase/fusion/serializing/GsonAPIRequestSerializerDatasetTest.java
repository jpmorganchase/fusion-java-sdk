package io.github.jpmorganchase.fusion.serializing;

import static io.github.jpmorganchase.fusion.test.TestUtils.listOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.Dataset;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
    public void testDatasetWithNullValues() {
        // Given
        Dataset dataset = Dataset.builder()
                .identifier("SD0002")
                .description(null)
                .linkedEntity(null)
                .title("Sample Dataset with Nulls")
                .frequency("Monthly")
                .applicationId(null)
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(dataset);

        // Then
        String expected = loadTestResource("dataset-with-nulls-request.json");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void testDatasetWithEmptyConsumerApplicationIds() {
        // Given
        Dataset dataset = Dataset.builder()
                .identifier("SD0003")
                .description("Sample dataset with empty consumer IDs")
                .linkedEntity("SD0003/")
                .title("Sample Dataset 3 | North America")
                .frequency("Weekly")
                // .consumerApplicationId(new ArrayList<>())
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(dataset);

        // Then
        String expected = loadTestResource("dataset-empty-consumer-ids.json");
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void testDatasetWithSpecialCharacters() {
        // Given
        Dataset dataset = Dataset.builder()
                .identifier("SD0004")
                .description("Special characters: \"quotes\", \n newlines, and \u2022 bullets")
                .linkedEntity("SD0004/")
                .title("Dataset with Special Characters")
                .frequency("Yearly")
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(dataset);

        // Then
        String expected = loadTestResource("dataset-special-characters.json");
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
