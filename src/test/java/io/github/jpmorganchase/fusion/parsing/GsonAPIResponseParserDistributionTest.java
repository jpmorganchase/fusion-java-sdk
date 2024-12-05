package io.github.jpmorganchase.fusion.parsing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.model.Distribution;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class GsonAPIResponseParserDistributionTest {

    private static final String singleDistributionJson = loadTestResource("single-distribution-response.json");
    private static final String multipleDistributionJson = loadTestResource("multiple-distribution-response.json");

    private static final Distribution testDistribution = Distribution.builder()
            .identifier("csv")
            .description("Snapshot data will be in a tabular, comma separated format.")
            .linkedEntity("csv/")
            .fileExtension(".csv")
            .mediaType("text/csv; header=present; charset=utf-8")
            .title("CSV")
            .build();

    private static final Distribution testDistribution2 = Distribution.builder()
            .identifier("parquet")
            .description("Snapshot data will be in a parquet format.")
            .linkedEntity("parquet/")
            .fileExtension(".parquet")
            .mediaType("application/parquet; header=present")
            .title("Parquet")
            .build();

    private static final APIResponseParser responseParser =
            GsonAPIResponseParser.builder().gson(DefaultGsonConfig.gson()).build();

    @Test
    public void singleDistributionInResourcesParsesCorrectly() {
        Map<String, Distribution> distributionMap = responseParser.parseDistributionResponse(singleDistributionJson);
        assertThat(distributionMap.size(), is(1));

        Distribution testDistributionResponse = distributionMap.get("csv");
        assertThat(testDistributionResponse, is(equalTo(testDistribution)));
    }

    @Test
    public void multipleCatalogsInResourcesParseCorrectly() {
        Map<String, Distribution> distributionMap = responseParser.parseDistributionResponse(multipleDistributionJson);
        assertThat(distributionMap.size(), is(2));

        Distribution testDistributionResponse = distributionMap.get("csv");
        assertThat(testDistributionResponse, is(equalTo(testDistribution)));

        Distribution testDistributionResponse2 = distributionMap.get("parquet");
        assertThat(testDistributionResponse2, is(equalTo(testDistribution2)));
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
