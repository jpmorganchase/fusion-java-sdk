package io.github.jpmorganchase.fusion.model;

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
    String idType;

    public static class ApplicationBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private String idType;

        public Application.ApplicationBuilder sealId(String id) {
            this.id = id;
            this.idType = "SEAL";
            return this;
        }

        public Application.ApplicationBuilder utId(String id) {
            this.id = id;
            this.idType = "UT";
            return this;
        }

        public Application.ApplicationBuilder isId(String id) {
            this.id = id;
            this.idType = "is";
            return this;
        }
    }
}
