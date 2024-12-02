package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.Expose;
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
public class Attribute extends CatalogResource {

    @SerializedName("isDatasetKey")
    boolean key;

    String dataType;
    long index;
    String description;
    String title;

    @Expose(serialize = false, deserialize = false)
    String dataset;

    @Builder(toBuilder = true)
    public Attribute(
            @Builder.ObtainVia(method = "getIdentifier") String identifier,
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getApiManager") APIManager apiManager,
            @Builder.ObtainVia(method = "getRootUrl") String rootUrl,
            @Builder.ObtainVia(method = "getCatalogIdentifier") String catalogIdentifier,
            boolean key,
            String dataType,
            long index,
            String description,
            String title,
            String dataset) {
        super(identifier, varArgs, apiManager, rootUrl, catalogIdentifier);
        this.key = key;
        this.dataType = dataType;
        this.index = index;
        this.description = description;
        this.title = title;
        this.dataset = dataset;
    }

    @Override
    protected String getApiPath() {
        return String.format(
                "%1scatalogs/%2s/datasets/%3s/attributes/%4s",
                this.getRootUrl(), this.getCatalogIdentifier(), this.getDataset(), this.getIdentifier());
    }

    public static class AttributeBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        public Attribute.AttributeBuilder varArg(String key, Object value) {
            this.varArgs = VarArgsHelper.varArg(key, value, this.varArgs);
            return this;
        }

        public Attribute.AttributeBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }
    }
}
