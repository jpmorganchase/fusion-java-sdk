package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import io.github.jpmorganchase.fusion.api.APIManager;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.Map;

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

    @Builder(toBuilder = true)
    public Dataset(
            @Builder.ObtainVia(method = "getIdentifier") String identifier,
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getApiManager") APIManager apiManager,
            @Builder.ObtainVia(method = "getRootUrl") String rootUrl,
            @Builder.ObtainVia(method = "getCatalogIdentifier") String catalogIdentifier,
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
            this.varArgs = VarArgsHelper.varArg(key, value, this.varArgs);
            return this;
        }

        public DatasetBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }
    }
}
