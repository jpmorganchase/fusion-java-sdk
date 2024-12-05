package io.github.jpmorganchase.fusion.parsing;

import static io.github.jpmorganchase.fusion.test.TestUtils.listOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.api.context.APIContext;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.Dataset;
import io.github.jpmorganchase.fusion.model.Report;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GsonAPIResponseParserDatasetReportTest {

    private static final String singleDatasetJson = loadTestResource("single-dataset-report-response.json");
    private static final String multipleDatasetJson = loadTestResource("multiple-dataset-report-response.json");

    private final Dataset testDataset = Dataset.builder()
            .identifier("SR0001")
            .description("Sample report description 1")
            .linkedEntity("SR0001/")
            .frequency("Daily")
            .fusion(fusion)
            .title("Sample Report 1 | North America")
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
            .applicationId(Application.builder().sealId("12345").build())
            .report(Report.builder().tier("Tier 1").build())
            .build();

    private final Dataset testDataset2 = Dataset.builder()
            .identifier("SR0002")
            .description("Sample report description 2")
            .linkedEntity("SR0002/")
            .frequency("Daily")
            .title("Sample Report 2 | North America")
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
            .varArg("publisher", "Publisher 2")
            .varArg("region", listOf("North America"))
            .varArg("source", listOf("Source System 2"))
            .varArg("subCategory", listOf("Subcategory 2"))
            .varArg("tag", listOf("Tag2"))
            .varArg("isRestricted", Boolean.FALSE)
            .varArg("isRawData", Boolean.FALSE)
            .varArg("hasSample", Boolean.FALSE)
            .applicationId(Application.builder().sealId("12345").build())
            .report(Report.builder().tier("Tier 2").build())
            .build();

    private final Dataset testDataset3 = Dataset.builder()
            .identifier("SR0003")
            .description("Sample report description 3")
            .linkedEntity("SR0003/")
            .frequency("Daily")
            .title("Sample Report 3 | North America")
            .fusion(fusion)
            .varArg("category", listOf("Category 3"))
            .varArg("createdDate", "2022-02-07")
            .varArg("coverageStartDate", "2022-02-07")
            .varArg("coverageEndDate", "2023-03-10")
            .varArg("isThirdPartyData", Boolean.FALSE)
            .varArg("isInternalOnlyDataset", Boolean.FALSE)
            .varArg("language", "English")
            .varArg("maintainer", "Maintainer 3")
            .varArg("modifiedDate", "2023-03-10")
            .varArg("publisher", "Publisher 3")
            .varArg("region", listOf("North America"))
            .varArg("source", listOf("Source System 3"))
            .varArg("subCategory", listOf("Subcategory 3"))
            .varArg("tag", listOf("Tag3"))
            .varArg("isRestricted", Boolean.FALSE)
            .varArg("isRawData", Boolean.FALSE)
            .varArg("hasSample", Boolean.FALSE)
            .applicationId(Application.builder().sealId("12345").build())
            .report(Report.builder().tier("Tier 3").build())
            .build();

    private static final Fusion fusion = Mockito.mock(Fusion.class);

    private static final APIResponseParser responseParser = new GsonAPIResponseParser();

    @Test
    public void singleDatasetInResourcesParsesCorrectly() {
        Map<String, Dataset> datasetMap = responseParser.parseDatasetResponse(fusion, singleDatasetJson);
        assertThat(datasetMap.size(), is(1));

        Dataset testDatasetResponse = datasetMap.get("SR0001");
        assertThat(testDatasetResponse, is(equalTo(testDataset)));
    }

    @Test
    public void multipleCatalogsInResourcesParseCorrectly() {
        Map<String, Dataset> datasetMap = responseParser.parseDatasetResponse(fusion, multipleDatasetJson);
        assertThat(datasetMap.size(), is(3));

        Dataset testDatasetResponse = datasetMap.get("SR0001");
        assertThat(testDatasetResponse, is(equalTo(testDataset)));

        Dataset testDatasetResponse2 = datasetMap.get("SR0002");
        assertThat(testDatasetResponse2, is(equalTo(testDataset2)));

        Dataset testDatasetResponse3 = datasetMap.get("SR0003");
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
