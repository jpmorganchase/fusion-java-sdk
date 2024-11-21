package io.github.jpmorganchase.fusion.filter;

import io.github.jpmorganchase.fusion.model.Dataset;
import java.util.Map;
import java.util.stream.Collectors;

public class DatasetFilter {

    /**
     * Filters a map of datasets based on a search keyword and whether to filter only the identifier.
     *
     * @param datasets   The original map of datasets to filter.
     * @param contains   The search keyword.
     * @param idContains True if the filter should only apply to the identifier.
     * @return A filtered map of datasets.
     */
    public static Map<String, Dataset> filterDatasets(
            Map<String, Dataset> datasets, String contains, boolean idContains) {
        if (contains == null || contains.isEmpty()) {
            return datasets; // No filtering needed
        }

        // Use a stream to filter the datasets based on the `contains` and `idContains` logic
        return datasets.entrySet().stream()
                .filter(entry -> matchesFilter(entry.getValue(), contains, idContains))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Checks whether a dataset matches the filter condition.
     *
     * @param dataset    The dataset to check.
     * @param contains   The search keyword.
     * @param idContains True if the filter should only apply to the identifier.
     * @return True if the dataset matches the filter, false otherwise.
     */
    private static boolean matchesFilter(Dataset dataset, String contains, boolean idContains) {
        if (idContains) {
            // Only check the identifier
            return dataset.getIdentifier() != null && dataset.getIdentifier().contains(contains);
        } else {
            // Check across multiple fields (example: identifier, title, description)
            return (dataset.getIdentifier() != null && dataset.getIdentifier().contains(contains))
                    || (dataset.getTitle() != null && dataset.getTitle().contains(contains))
                    || (dataset.getDescription() != null
                            && dataset.getDescription().contains(contains));
        }
    }
}
