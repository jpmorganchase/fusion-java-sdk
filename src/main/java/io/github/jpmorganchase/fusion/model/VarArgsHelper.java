package io.github.jpmorganchase.fusion.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VarArgsHelper {

    public static Map<String, Object> varArg(String key, Object value, Map<String, Object> varArgs) {
        if (Objects.isNull(varArgs)) {
            varArgs = initializeMap();
        }
        varArgs.put(key, value);
        return varArgs;
    }

    public static Map<String, Object> copyMap(Map<String, Object> source) {
        Map<String, Object> target = null;
        if (source != null) {
            target = new HashMap<>(source);
        }
        return target;
    }

    public static Map<String, Object> initializeMap() {
        return new HashMap<>();
    }
}
