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
public class Attribute extends CatalogResource {

    @SerializedName("isDatasetKey")
    boolean key;

    String dataType;
    long index;
    String description;
    String title;

    @Builder
    public Attribute(
            String identifier,
            Map<String, Object> varArgs,
            APIManager apiManager,
            String rootUrl,
            String catalogIdentifier,
            boolean key,
            String dataType,
            long index,
            String description,
            String title) {
        super(identifier, varArgs, apiManager, rootUrl, catalogIdentifier);
        this.key = key;
        this.dataType = dataType;
        this.index = index;
        this.description = description;
        this.title = title;
    }

    @Override
    protected String getApiPath() {
        throw new UnsupportedOperationException("Operation is not yet supported for attributes");
    }

    public static class AttributeBuilder {
        private Map<String, Object> varArgs;

        public AttributeBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = copyMap(varArgs);
            return this;
        }
    }
}
