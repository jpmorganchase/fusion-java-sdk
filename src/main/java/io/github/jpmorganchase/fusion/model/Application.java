package io.github.jpmorganchase.fusion.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
@EqualsAndHashCode()
@ToString()
public class Application {

    String id;
    String type;

    public Map<String, String> toMap() {
        Map<String, String> attributeMap = new HashMap<>();
        attributeMap.put("id", id);
        attributeMap.put("type", type);
        return attributeMap;
    }

    public static class ApplicationBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private String type;

        public Application.ApplicationBuilder sealId(String id) {
            this.id = id;
            this.type = "Application (SEAL)";
            return this;
        }

        public Application.ApplicationBuilder userToolId(String id) {
            this.id = id;
            this.type = "User Tool";
            return this;
        }

        public Application.ApplicationBuilder intelligentSolutionsId(String id) {
            this.id = id;
            this.type = "Intelligent Solutions";
            return this;
        }
    }
}
