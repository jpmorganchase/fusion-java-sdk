package io.github.jpmorganchase.fusion.model;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DataDictionaryAttributes extends CatalogResource {

    List<DataDictionaryAttribute> dataDictionaryAttributes;

    @Builder(toBuilder = true)
    public DataDictionaryAttributes(
            @Builder.ObtainVia(method = "getIdentifier") String identifier,
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getFusion") Fusion fusion,
            @Builder.ObtainVia(method = "getCatalogIdentifier") String catalogIdentifier,
            List<DataDictionaryAttribute> dataDictionaryAttributes) {
        super(identifier, varArgs, fusion, catalogIdentifier);
        this.dataDictionaryAttributes = dataDictionaryAttributes;
    }

    @Override
    protected String getApiPath() {
        return String.format("%1scatalogs/%2s/attributes", getFusion().getRootURL(), this.getCatalogIdentifier());
    }

    @Override
    public String update() {
        throw new UnsupportedOperationException("Operation not yet supported for DataDictionaryAttribute");
    }

    @Override
    public String delete() {
        throw new UnsupportedOperationException("Operation not yet supported for DataDictionaryAttribute");
    }

    @Override
    public Set<String> getRegisteredAttributes() {
        Set<String> exclusions = super.getRegisteredAttributes();
        exclusions.addAll(VarArgsHelper.getFieldNames(DataDictionaryAttributes.class));
        return exclusions;
    }

    public static class DataDictionaryAttributesBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        private List<DataDictionaryAttribute> dataDictionaryAttributes;

        public DataDictionaryAttributes.DataDictionaryAttributesBuilder varArg(String key, Object value) {
            this.varArgs = VarArgsHelper.varArg(key, value, this.varArgs);
            return this;
        }

        public DataDictionaryAttributes.DataDictionaryAttributesBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }

        public DataDictionaryAttributes.DataDictionaryAttributesBuilder dataDictionaryAttribute(
                DataDictionaryAttribute dataDictionaryAttribute) {
            initialiseDDAttributesArray();

            this.dataDictionaryAttributes.add(dataDictionaryAttribute);
            return this;
        }

        public DataDictionaryAttributes.DataDictionaryAttributesBuilder dataDictionaryAttributes(
                List<DataDictionaryAttribute> dataDictionaryAttributes) {
            initialiseDDAttributesArray();

            this.dataDictionaryAttributes.addAll(dataDictionaryAttributes);
            return this;
        }

        private void initialiseDDAttributesArray() {
            if (null == this.dataDictionaryAttributes) {
                this.dataDictionaryAttributes = new ArrayList<>();
            }
        }
    }
}
