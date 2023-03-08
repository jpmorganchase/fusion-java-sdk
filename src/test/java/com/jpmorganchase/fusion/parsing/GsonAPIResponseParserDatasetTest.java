package com.jpmorganchase.fusion.parsing;

import com.jpmorganchase.fusion.model.Dataset;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class GsonAPIResponseParserDatasetTest {

    private static final String singleDatasetJson = loadTestResource("single-dataset-response.json");
    private static final String multipleDatasetJson = loadTestResource("multiple-dataset-response.json");

    //TODO: Need to map out all the fields
    private static final Dataset testDataset = Dataset.builder()
            .identifier("SD0001")
            .description("Sample dataset description 1")
            .linkedEntity("SD0001/")
            .frequency("Daily")
            .title("Sample Dataset 1 | North America")
            .build();

    private static final Dataset testDataset2 = Dataset.builder()
            .identifier("SD0002")
            .description("Sample dataset description 2")
            .linkedEntity("SD0002/")
            .frequency("Daily")
            .title("Sample Dataset 2 | North America")
            .build();

    private static final Dataset testDataset3 = Dataset.builder()
            .identifier("SD0003")
            .description("Sample dataset description 3")
            .linkedEntity("SD0003/")
            .frequency("Daily")
            .title("Sample Dataset 3 | North America")
            .build();

    private static final APIResponseParser responseParser = new GsonAPIResponseParser();

    @Test
    public void singleDatasetInResourcesParsesCorrectly() {
        Map<String, Dataset> datasetMap = responseParser.parseDatasetResponse(singleDatasetJson);
        assertThat(datasetMap.size(), is(1));

        Dataset testDatasetResponse = datasetMap.get("SD0001");
        assertThat(testDatasetResponse, is(equalTo(testDataset)));
    }

    @Test
    public void multipleCatalogsInResourcesParseCorrectly() {
        Map<String, Dataset> datasetMap = responseParser.parseDatasetResponse(multipleDatasetJson);
        assertThat(datasetMap.size(), is(3));

        Dataset testDatasetResponse = datasetMap.get("SD0001");
        assertThat(testDatasetResponse, is(equalTo(testDataset)));

        Dataset testDatasetResponse2 = datasetMap.get("SD0002");
        assertThat(testDatasetResponse2, is(equalTo(testDataset2)));

        Dataset testDatasetResponse3 = datasetMap.get("SD0003");
        assertThat(testDatasetResponse3, is(equalTo(testDataset3)));
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
