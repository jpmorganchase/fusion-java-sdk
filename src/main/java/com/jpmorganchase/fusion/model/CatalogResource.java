package com.jpmorganchase.fusion.model;

import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * superclass of all entities contained in a catalog
 */
@Getter
@ToString
@EqualsAndHashCode
public abstract class CatalogResource {

    private final String identifier;
    private Map<String, String> varArgs;

    public CatalogResource(String identifier, Map<String, String> varArgs) {
        this.varArgs = copyMap(varArgs);
        this.identifier = identifier;
    }

    public Map<String, String> getVarArgs() {
        return copyMap(varArgs);
    }

    static Map<String, String> copyMap(Map<String, String> source) {
        Map<String, String> target = null;
        if (source != null) {
            target = new HashMap<>(source);
        }
        return target;
    }
}
