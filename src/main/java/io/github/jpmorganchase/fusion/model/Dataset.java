package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * An object representing a dataset. Object properties hold dataset metadata attributes
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Dataset extends CatalogResource {

    String description;

    @SerializedName(value = "@id")
    String linkedEntity;

    String title;
    String frequency;

    @Builder
    public Dataset(
            String identifier,
            Map<String, String> varArgs,
            String description,
            String linkedEntity,
            String title,
            String frequency) {
        super(identifier, varArgs);
        this.description = description;
        this.linkedEntity = linkedEntity;
        this.title = title;
        this.frequency = frequency;
    }

    public static class DatasetBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, String> varArgs;

        public DatasetBuilder varArgs(Map<String, String> varArgs) {
            this.varArgs = copyMap(varArgs);
            return this;
        }
    }
}
