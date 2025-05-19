package io.github.jpmorganchase.fusion.model;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

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

    public static Set<String> getFieldNames(Class<? extends Resource> resourceClass) {
        if (resourceClass == null) {
            // Explicitly return an empty set to handle null input
            return Collections.emptySet();
        }

        // Stream the fields, map names, and filter out synthetic fields like "this$0"
        return Arrays.stream(resourceClass.getDeclaredFields())
                .filter(f -> !f.isSynthetic())
                .map(Field::getName)
                .filter(name -> !name.startsWith("__$") && !name.equals("this$0"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
