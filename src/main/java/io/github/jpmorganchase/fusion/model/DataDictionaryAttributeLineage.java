package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import io.github.jpmorganchase.fusion.api.APIManager;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataDictionaryAttributeLineage extends CatalogResource {

    String baseIdentifier;

    String baseCatalogIdentifier;

    String description;

    @SerializedName(value = "@id")
    String linkedEntity;

    String title;

    Application applicationId;
    Catalog catalog;

    @Builder(toBuilder = true)
    public DataDictionaryAttributeLineage(
            @Builder.ObtainVia(method = "getIdentifier") String identifier,
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getApiManager") APIManager apiManager,
            @Builder.ObtainVia(method = "getRootUrl") String rootUrl,
            @Builder.ObtainVia(method = "getCatalogIdentifier") String catalogIdentifier,
            String baseIdentifier,
            String baseCatalogIdentifier,
            String description,
            String linkedEntity,
            String title,
            Application applicationId,
            Catalog catalog) {
        super(identifier, varArgs, apiManager, rootUrl, catalogIdentifier);
        this.baseIdentifier = baseIdentifier;
        this.baseCatalogIdentifier = baseCatalogIdentifier;
        this.description = description;
        this.linkedEntity = linkedEntity;
        this.title = title;
        this.applicationId = applicationId;
        this.catalog = catalog;
    }

    @Override
    protected String getApiPath() {
        return String.format(
                "%1scatalogs/%2s/attributes/%3s/lineage",
                this.getRootUrl(), this.getBaseCatalogIdentifier(), this.getBaseIdentifier());
    }

    public static class AttributeLineageBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        public DataDictionaryAttributeLineage.AttributeLineageBuilder varArg(String key, Object value) {
            this.varArgs = VarArgsHelper.varArg(key, value, this.varArgs);
            return this;
        }

        public DataDictionaryAttributeLineage.AttributeLineageBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }
    }
}
