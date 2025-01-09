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

    public static Set<String> getFieldNames(Set<String> exclusions, Class<?> resourceClass) {
        exclusions.addAll(Arrays.stream(resourceClass.getDeclaredFields())
                .map(Field::getName)
                .filter(n -> !"this$0".equals(n))
                .collect(Collectors.toSet()));
        return exclusions;
    }
}
