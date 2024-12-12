package io.github.jpmorganchase.fusion.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.model.Dataset;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DatasetFilterTest {

    @Test
    public void testFilterDatasets_noFilter() {
        Map<String, Dataset> datasets = new HashMap<>();
        datasets.put(
                "1",
                Dataset.builder()
                        .identifier("ID001")
                        .title("Dataset 1")
                        .description("Description 1")
                        .build());
        datasets.put(
                "2",
                Dataset.builder()
                        .identifier("ID002")
                        .title("Dataset 2")
                        .description("Description 2")
                        .build());

        Map<String, Dataset> result = DatasetFilter.filterDatasets(datasets, "", false);

        assertThat("All datasets should be returned when no filter is applied.", result.size(), is(equalTo(2)));
    }

    @Test
    public void testFilterDatasets_withFilter() {
        Map<String, Dataset> datasets = new HashMap<>();
        datasets.put(
                "1",
                Dataset.builder()
                        .identifier("ID001")
                        .title("Dataset 1")
                        .description("Description 1")
                        .build());
        datasets.put(
                "2",
                Dataset.builder()
                        .identifier("ID002")
                        .title("Dataset 2")
                        .description("Description 2")
                        .build());

        Map<String, Dataset> result = DatasetFilter.filterDatasets(datasets, "Dataset 1", false);

        assertThat("Only matching dataset should be returned.", result.size(), is(equalTo(1)));
        assertThat("Filtered dataset should be present.", result.containsKey("1"), is(equalTo(true)));
    }

    @Test
    public void testFilterDatasets_withIdContains() {
        Map<String, Dataset> datasets = new HashMap<>();
        datasets.put(
                "1",
                Dataset.builder()
                        .identifier("ID001")
                        .title("Dataset 1")
                        .description("Description 1")
                        .build());
        datasets.put(
                "2",
                Dataset.builder()
                        .identifier("ID002")
                        .title("Dataset 2")
                        .description("Description 2")
                        .build());

        Map<String, Dataset> result = DatasetFilter.filterDatasets(datasets, "ID001", true);

        assertThat("Only matching dataset should be returned.", result.size(), is(equalTo(1)));
        assertThat("Filtered dataset should be present.", result.containsKey("1"), is(equalTo(true)));
    }

    @Test
    public void testFilterDatasets_noMatch() {
        Map<String, Dataset> datasets = new HashMap<>();
        datasets.put(
                "1",
                Dataset.builder()
                        .identifier("ID001")
                        .title("Dataset 1")
                        .description("Description 1")
                        .build());
        datasets.put(
                "2",
                Dataset.builder()
                        .identifier("ID002")
                        .title("Dataset 2")
                        .description("Description 2")
                        .build());

        Map<String, Dataset> result = DatasetFilter.filterDatasets(datasets, "Non-existent", false);

        assertThat("No datasets should be returned if no matches found.", result.isEmpty(), is(equalTo(true)));
    }

    @Test
    public void testFilterDatasets_emptyInput() {
        Map<String, Dataset> datasets = new HashMap<>();

        Map<String, Dataset> result = DatasetFilter.filterDatasets(datasets, "Dataset", false);

        assertThat("No datasets should be returned from an empty input.", result.isEmpty(), is(equalTo(true)));
    }
}
