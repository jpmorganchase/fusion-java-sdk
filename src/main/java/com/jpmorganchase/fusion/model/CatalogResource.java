package com.jpmorganchase.fusion.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * superclass of all entities contained in a catalog
 */
@Getter
@AllArgsConstructor // TODO: Deep copy the map?
@ToString
@EqualsAndHashCode
public abstract class CatalogResource {

    private final String identifier;
    private Map<String, String> varArgs;
}
