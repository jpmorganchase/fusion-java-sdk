package io.github.jpmorganchase.fusion.model;

import io.github.jpmorganchase.fusion.api.APIManager;
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

    @Builder(toBuilder = true)
    public DataDictionaryAttribute(
            @Builder.ObtainVia(method = "getIdentifier") String identifier,
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getApiManager") APIManager apiManager,
            @Builder.ObtainVia(method = "getRootUrl") String rootUrl,
            @Builder.ObtainVia(method = "getCatalogIdentifier") String catalogIdentifier,
            String description,
            String title) {
        super(identifier, varArgs, apiManager, rootUrl, catalogIdentifier);
        this.description = description;
        this.title = title;
    }

    @Override
    protected String getApiPath() {
        return String.format(
                "%1scatalogs/%2s/attributes/%3s", this.getRootUrl(), this.getCatalogIdentifier(), this.getIdentifier());
    }

    public String setLinage(DataDictionaryAttribute derived) {
        return buildAttributeLineage(derived.getIdentifier(), derived.getCatalogIdentifier())
                .create();
    }

    public String updateLineage(DataDictionaryAttribute derived) {
        return buildAttributeLineage(derived.getIdentifier(), derived.getCatalogIdentifier())
                .update();
    }

    public String deleteLineage() {
        return buildAttributeLineage().delete();
    }

    private DataDictionaryAttributeLineage buildAttributeLineage() {
        return buildAttributeLineage(null, null);
    }

    private DataDictionaryAttributeLineage buildAttributeLineage(
            String derivedIdentifier, String derivedCatalogIdentifier) {
        return DataDictionaryAttributeLineage.builder()
                .identifier(derivedIdentifier)
                .catalogIdentifier(derivedCatalogIdentifier)
                .baseIdentifier(this.getIdentifier())
                .baseCatalogIdentifier(this.getCatalogIdentifier())
                .apiManager(this.getApiManager())
                .rootUrl(this.getRootUrl())
                .build();
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
