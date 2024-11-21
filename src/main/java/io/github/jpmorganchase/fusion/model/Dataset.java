package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import io.github.jpmorganchase.fusion.api.APIManager;
import java.util.Map;
import java.util.Objects;
import lombok.*;

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
            Map<String, Object> varArgs,
            APIManager apiManager,
            String rootUrl,
            String catalogIdentifier,
            String description,
            String linkedEntity,
            String title,
            String frequency) {
        super(identifier, varArgs, apiManager, rootUrl, catalogIdentifier);
        this.description = description;
        this.linkedEntity = linkedEntity;
        this.title = title;
        this.frequency = frequency;
    }

    @Override
    protected String getApiPath() {
        return String.format(
                "%1scatalogs/%2s/datasets/%3s", this.getRootUrl(), this.getCatalogIdentifier(), this.getIdentifier());
    }

    public static class DatasetBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        public DatasetBuilder varArg(String key, Object value) {
            if (Objects.isNull(varArgs)) {
                this.varArgs = initializeMap();
            }
            this.varArgs.put(key, value);
            return this;
        }

        public DatasetBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = copyMap(varArgs);
            return this;
        }
    }
}
