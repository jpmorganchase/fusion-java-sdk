package io.github.jpmorganchase.fusion.model;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataDictionaryAttribute extends CatalogResource {

    String description;
    String title;
    Application applicationId;

    @Builder(toBuilder = true)
    public DataDictionaryAttribute(
            @Builder.ObtainVia(method = "getIdentifier") String identifier,
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getFusion") Fusion fusion,
            @Builder.ObtainVia(method = "getCatalogIdentifier") String catalogIdentifier,
            String description,
            String title,
            Application applicationId) {
        super(identifier, varArgs, fusion, catalogIdentifier);
        this.description = description;
        this.title = title;
        this.applicationId = applicationId;
    }

    @Override
    protected String getApiPath() {
        return String.format(
                "%1scatalogs/%2s/attributes/%3s",
                getFusion().getRootURL(), this.getCatalogIdentifier(), this.getIdentifier());
    }

    public static class DataDictionaryAttributeBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        public DataDictionaryAttributeBuilder varArg(String key, Object value) {
            this.varArgs = VarArgsHelper.varArg(key, value, this.varArgs);
            return this;
        }

        public DataDictionaryAttributeBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }
    }
}
