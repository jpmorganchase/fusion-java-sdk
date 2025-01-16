package io.github.jpmorganchase.fusion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DatasetType {
    REPORT("Report"),
    FLOW("Flow"),
    DEFAULT("");

    private final String label;
}
