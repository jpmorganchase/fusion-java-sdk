package io.github.jpmorganchase.fusion.filter;

import io.github.jpmorganchase.fusion.model.Dataset;
import io.github.jpmorganchase.fusion.model.DatasetType;
import java.util.Map;
import java.util.function.Predicate;
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
    public static <T extends Dataset> Map<String, T> filterDatasets(
            Map<String, T> datasets, String contains, boolean idContains) {
        if (contains == null || contains.isEmpty()) {
            return datasets; // No filtering needed
        }

        // Use a stream to filter the datasets based on the `contains` and `idContains` logic
        return datasets.entrySet().stream()
                .filter(entry -> matchesFilter(entry.getValue(), contains, idContains))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Filters a map of datasets (or its subclasses) based on a specific type.
     *
     * @param <T>      The type of Dataset or its subclass.
     * @param datasets The original map of datasets to filter.
     * @param type     The type to filter datasets by.
     * @return A filtered map of datasets containing only entries that match the specified type.
     */
    public static <T extends Dataset> Map<String, T> filterByType(Map<String, T> datasets, DatasetType type) {
        if (type == null) {
            return datasets; // No filtering needed
        }

        // Use a stream to filter the datasets based on type
        return datasets.entrySet().stream()
                .filter(typePredicate(type))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static <T extends Dataset> Predicate<Map.Entry<String, T>> typePredicate(DatasetType type) {
        return entry -> type.getLabel().equals(entry.getValue().getType());
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
