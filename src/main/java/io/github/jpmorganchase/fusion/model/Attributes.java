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
public class Attributes extends CatalogResource {

    List<Attribute> attributes;
    String datasetIdentifier;

    @Builder(toBuilder = true)
    public Attributes(
            @Builder.ObtainVia(method = "getIdentifier") String identifier,
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getFusion") Fusion fusion,
            @Builder.ObtainVia(method = "getCatalogIdentifier") String catalogIdentifier,
            List<Attribute> attributes,
            String datasetIdentifier) {
        super(identifier, varArgs, fusion, catalogIdentifier);
        this.attributes = attributes;
        this.datasetIdentifier = datasetIdentifier;
    }

    @Override
    protected String getApiPath() {
        return String.format(
                "%1scatalogs/%2s/datasets/%3s/attributes",
                getFusion().getRootURL(), this.getCatalogIdentifier(), this.getDatasetIdentifier());
    }

    @Override
    public String create() {
        return this.update();
    }

    @Override
    public String delete() {
        throw new UnsupportedOperationException(
                "Operation not yet supported for " + this.getClass().getName());
    }

    @Override
    public Set<String> getRegisteredAttributes() {
        Set<String> exclusions = super.getRegisteredAttributes();
        exclusions.addAll(VarArgsHelper.getFieldNames(Attributes.class));
        return exclusions;
    }

    public static class AttributesBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        private List<Attribute> attributes;

        public Attributes.AttributesBuilder varArg(String key, Object value) {
            this.varArgs = VarArgsHelper.varArg(key, value, this.varArgs);
            return this;
        }

        public Attributes.AttributesBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }

        public Attributes.AttributesBuilder attribute(Attribute attribute) {
            initialiseAttributesArray();

            this.attributes.add(attribute);
            return this;
        }

        public Attributes.AttributesBuilder attributes(List<Attribute> attributes) {
            initialiseAttributesArray();

            this.attributes.addAll(attributes);
            return this;
        }

        private void initialiseAttributesArray() {
            if (null == this.attributes) {
                this.attributes = new ArrayList<>();
            }
        }
    }
}
