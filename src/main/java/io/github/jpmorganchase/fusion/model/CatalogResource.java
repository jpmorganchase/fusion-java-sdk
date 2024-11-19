package io.github.jpmorganchase.fusion.model;

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
    private final Map<String, Object> varArgs;

    public CatalogResource(String identifier, Map<String, Object> varArgs) {
        this.varArgs = copyMap(varArgs);
        this.identifier = identifier;
    }

    public Map<String, Object> getVarArgs() {
        return copyMap(varArgs);
    }

    static Map<String, Object> copyMap(Map<String, Object> source) {
        Map<String, Object> target = null;
        if (source != null) {
            target = new HashMap<>(source);
        }
        return target;
    }

    static Map<String, Object> initializeMap() {
        return new HashMap<>();
    }
}
