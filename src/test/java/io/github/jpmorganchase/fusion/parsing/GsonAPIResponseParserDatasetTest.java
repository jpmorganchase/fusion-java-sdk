package io.github.jpmorganchase.fusion.parsing;

import static io.github.jpmorganchase.fusion.test.TestUtils.listOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.model.Dataset;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class GsonAPIResponseParserDatasetTest {

    private static final String singleDatasetJson = loadTestResource("single-dataset-response.json");
    private static final String multipleDatasetJson = loadTestResource("multiple-dataset-response.json");

    // TODO: Need to map out all the fields
    private static final Dataset testDataset = Dataset.builder()
            .identifier("SD0001")
            .description("Sample dataset description 1")
            .linkedEntity("SD0001/")
            .frequency("Daily")
            .title("Sample Dataset 1 | North America")
            .build();

    private static final Dataset testDatasetWithVarArgs = Dataset.builder()
            .identifier("SD0001")
            .description("Sample dataset description 1")
            .linkedEntity("SD0001/")
            .frequency("Daily")
            .title("Sample Dataset 1 | North America")
            .varArg("category", listOf("Category 1"))
            .varArg("createdDate", "2022-02-05")
            .varArg("coverageStartDate", "2022-02-05")
            .varArg("coverageEndDate", "2023-03-08")
            .varArg("isThirdPartyData", Boolean.FALSE)
            .varArg("isInternalOnlyDataset", Boolean.FALSE)
            .varArg("language", "English")
            .varArg("maintainer", "Maintainer 1")
            .varArg("modifiedDate", "2023-03-08")
            .varArg("publisher", "Publisher 1")
            .varArg("region", listOf("North America"))
            .varArg("source", listOf("Source System 1"))
            .varArg("subCategory", listOf("Subcategory 1"))
            .varArg("tag", listOf("Tag1"))
            .varArg("isRestricted", Boolean.FALSE)
            .varArg("isRawData", Boolean.FALSE)
            .varArg("hasSample", Boolean.FALSE)
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
    public void singleDatasetInResourcesParsesCorrectlyWithVarArgs() {
        Map<String, Dataset> datasetMap =
                responseParser.parseResourcesFromResponseWithVarArgs(singleDatasetJson, Dataset.class);
        assertThat(datasetMap.size(), is(1));

        Dataset testDatasetResponse = datasetMap.get("SD0001");
        assertThat(testDatasetResponse, is(equalTo(testDatasetWithVarArgs)));
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
