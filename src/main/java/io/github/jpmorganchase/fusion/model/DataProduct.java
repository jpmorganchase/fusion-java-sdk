package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * An object representing a data product. Object properties hold metadata attributes and descriptions.
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataProduct extends CatalogResource {

    String description;

    @SerializedName(value = "@id")
    String linkedEntity;

    String title;
    String status;

    @Builder
    public DataProduct(
            String identifier,
            Map<String, String> varArgs,
            String description,
            String linkedEntity,
            String title,
            String status) {
        super(identifier, varArgs);
        this.description = description;
        this.linkedEntity = linkedEntity;
        this.title = title;
        this.status = status;
    }

    public static class DataProductBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, String> varArgs;

        public DataProductBuilder varArgs(Map<String, String> varArgs) {
            this.varArgs = copyMap(varArgs);
            return this;
        }
    }
}
