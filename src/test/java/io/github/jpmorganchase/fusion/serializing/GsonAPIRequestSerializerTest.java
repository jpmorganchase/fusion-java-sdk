package io.github.jpmorganchase.fusion.serializing;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.sun.tools.javac.util.List;
import io.github.jpmorganchase.fusion.model.Dataset;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class GsonAPIRequestSerializerTest {

    @Test
    public void testDatasetSerializesCorrectly() {
        // Given
        Dataset dataset = Dataset.builder()
                .identifier("SD0001")
                .description("Sample dataset description 1")
                .linkedEntity("SD0001/")
                .title("Sample Dataset 1 | North America")
                .frequency("Daily")
                .varArg("category", List.of("Category 1"))
                .varArg("createdDate", "2022-02-05")
                .varArg("coverageStartDate", "2022-02-05")
                .varArg("coverageEndDate", "2023-03-08")
                .varArg("isThirdPartyData", Boolean.FALSE)
                .varArg("isInternalOnlyDataset", Boolean.FALSE)
                .varArg("language", "English")
                .varArg("maintainer", "Maintainer 1")
                .varArg("modifiedDate", "2023-03-08")
                .varArg("publisher", "Publisher 1")
                .varArg("region", List.of("North America"))
                .varArg("source", List.of("Source System 1"))
                .varArg("subCategory", List.of("Subcategory 1"))
                .varArg("tag", List.of("Tag1"))
                .varArg("isRestricted", Boolean.FALSE)
                .varArg("isRawData", Boolean.FALSE)
                .varArg("hasSample", Boolean.FALSE)
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serializeDatasetRequest(dataset);

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
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serializeDatasetRequest(dataset);

        // Then
        String expected = loadTestResource("dataset-no-varags-request.json");
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
