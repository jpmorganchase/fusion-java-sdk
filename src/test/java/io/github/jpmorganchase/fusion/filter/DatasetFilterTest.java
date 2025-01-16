package io.github.jpmorganchase.fusion.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.model.Dataset;
import io.github.jpmorganchase.fusion.model.DatasetType;
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

    @Test
    public void testFilterByType_noFilter() {
        Map<String, Dataset> datasets = new HashMap<>();
        datasets.put(
                "1",
                Dataset.builder()
                        .identifier("ID001")
                        .title("Dataset 1")
                        .type("type1")
                        .build());
        datasets.put(
                "2",
                Dataset.builder()
                        .identifier("ID002")
                        .title("Dataset 2")
                        .type("type2")
                        .build());

        Map<String, Dataset> result = DatasetFilter.filterByType(datasets, null);

        assertThat("All datasets should be returned when no filter is applied.", result.size(), is(equalTo(2)));
    }

    @Test
    public void testFilterByType_withFilter() {
        Map<String, Dataset> datasets = new HashMap<>();
        datasets.put(
                "1",
                Dataset.builder()
                        .identifier("ID001")
                        .title("Dataset 1")
                        .type(DatasetType.REPORT.getLabel())
                        .build());
        datasets.put(
                "2",
                Dataset.builder()
                        .identifier("ID002")
                        .title("Dataset 2")
                        .type(DatasetType.FLOW.getLabel())
                        .build());

        Map<String, Dataset> result = DatasetFilter.filterByType(datasets, DatasetType.REPORT);

        assertThat("Only datasets matching the type should be returned.", result.size(), is(equalTo(1)));
        assertThat("Filtered dataset should be present.", result.containsKey("1"), is(equalTo(true)));
    }

    @Test
    public void testFilterByType_noMatch() {
        Map<String, Dataset> datasets = new HashMap<>();
        datasets.put(
                "1",
                Dataset.builder()
                        .identifier("ID001")
                        .title("Dataset 1")
                        .type(DatasetType.REPORT.getLabel())
                        .build());
        datasets.put(
                "2",
                Dataset.builder()
                        .identifier("ID002")
                        .title("Dataset 2")
                        .type(DatasetType.REPORT.getLabel())
                        .build());

        Map<String, Dataset> result = DatasetFilter.filterByType(datasets, DatasetType.FLOW);

        assertThat("No datasets should be returned if no matches found.", result.isEmpty(), is(equalTo(true)));
    }

    @Test
    public void testFilterByType_emptyInput() {
        Map<String, Dataset> datasets = new HashMap<>();

        Map<String, Dataset> result = DatasetFilter.filterByType(datasets, DatasetType.REPORT);

        assertThat("No datasets should be returned from an empty input.", result.isEmpty(), is(equalTo(true)));
    }

    @Test
    public void testFilterByType_nullFilter() {
        Map<String, Dataset> datasets = new HashMap<>();
        datasets.put(
                "1",
                Dataset.builder()
                        .identifier("ID001")
                        .title("Dataset 1")
                        .type("type1")
                        .build());
        datasets.put(
                "2",
                Dataset.builder()
                        .identifier("ID002")
                        .title("Dataset 2")
                        .type("type2")
                        .build());

        Map<String, Dataset> result = DatasetFilter.filterByType(datasets, null);

        assertThat("All datasets should be returned when the filter is null.", result.size(), is(equalTo(2)));
    }
}
