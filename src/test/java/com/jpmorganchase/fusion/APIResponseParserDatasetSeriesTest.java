package com.jpmorganchase.fusion;

import com.google.gson.JsonParseException;
import com.jpmorganchase.fusion.model.DatasetSeries;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class APIResponseParserDatasetSeriesTest {

    private static final String datasetSeriesJson = loadTestResource("multiple-datasetseries-response.json");
    private static final String datasetSeriesWithDuplicatesJson = loadTestResource("duplicate-datasetseries-response.json");
    private static final String datasetSeriesWithInvalidDatesJson = loadTestResource("invalid-dates-datasetseries-response.json");

    private static final DatasetSeries member1 = DatasetSeries.builder()
            .identifier("20220318")
            .linkedEntity("20220318/")
            .createdDate(LocalDate.of(2022, 3, 18))
            .fromDate(LocalDate.of(2022, 3, 18))
            .toDate(LocalDate.of(2022, 3, 18))
            .build();

    private static final DatasetSeries member2 = DatasetSeries.builder()
            .identifier("20220319")
            .linkedEntity("20220319/")
            .createdDate(LocalDate.of(2022, 3, 19))
            .fromDate(LocalDate.of(2022, 3, 19))
            .toDate(LocalDate.of(2022, 3, 19))
            .build();

    private static final DatasetSeries member3 = DatasetSeries.builder()
            .identifier("20220320")
            .linkedEntity("20220320/")
            .createdDate(LocalDate.of(2022, 3, 20))
            .fromDate(LocalDate.of(2022, 3, 20))
            .toDate(LocalDate.of(2022, 3, 20))
            .build();


    private static final APIResponseParser responseParser = new APIResponseParser();


    @Test
    public void multipleSeriesMembersInResourcesParseCorrectly() {
        Map<String, DatasetSeries> datasetSeriesMap = responseParser.parseDatasetSeriesResponse(datasetSeriesJson);
        assertThat(datasetSeriesMap.size(), is(3));

        DatasetSeries testDatasetSeriesResponse = datasetSeriesMap.get("20220318");
        assertThat(testDatasetSeriesResponse, is(equalTo(member1)));

        DatasetSeries testDatasetSeriesResponse2 = datasetSeriesMap.get("20220319");
        assertThat(testDatasetSeriesResponse2, is(equalTo(member2)));

        DatasetSeries testDatasetSeriesResponse3 = datasetSeriesMap.get("20220320");
        assertThat(testDatasetSeriesResponse3, is(equalTo(member3)));
    }

    @Test
    public void duplicateSeriesMembersAreSkipped() {
        Map<String, DatasetSeries> datasetSeriesMap = responseParser.parseDatasetSeriesResponse(datasetSeriesWithDuplicatesJson);
        assertThat(datasetSeriesMap.size(), is(1));

        DatasetSeries testDatasetSeriesResponse = datasetSeriesMap.get("20220318");
        assertThat(testDatasetSeriesResponse, is(equalTo(member1)));
    }

    //TODO: As per comments in the class itself, this failure logic should be revisited
    @Test
    public void invalidDateFormatResultsInParserException() {
        JsonParseException thrown = assertThrows(
                JsonParseException.class,
                () -> responseParser.parseDatasetSeriesResponse(datasetSeriesWithInvalidDatesJson),
                "Expected a parsing exception for invalid dates, but it didn't throw"
        );
        assertThat(thrown.getMessage(), is(equalTo("Failed to deserialize date field with value INVALID DATE FORMAT")));
    }

    //TODO: This is duplicated - fix
    private static String loadTestResource(String resourceName) {
        URL url = APIResponseParserDatasetSeriesTest.class.getResource(resourceName);
        try {
            Path path = Paths.get(url.toURI());
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data", e);
        }
    }
}