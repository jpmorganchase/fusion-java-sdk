package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import io.github.jpmorganchase.fusion.api.APIManager;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.Map;

/**
 * An object representing a Fusion catalog, which is a container and inventory of datasets.
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Catalog extends CatalogResource {

    String description;

    @SerializedName(value = "@id")
    String linkedEntity;

    String title;

    @Builder
    public Catalog(
            String identifier,
            Map<String, Object> varArgs,
            String rootUrl,
            String catalogIdentifier,
            APIManager apiManager,
            String description,
            String linkedEntity,
            String title) {
        super(identifier, varArgs, apiManager, rootUrl, catalogIdentifier);
        this.description = description;
        this.linkedEntity = linkedEntity;
        this.title = title;
    }

    @Override
    protected String getApiPath() {
        throw new UnsupportedOperationException("Operation not yet supported for Catalog");
    }

    public static class CatalogBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        public CatalogBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }
    }
}
