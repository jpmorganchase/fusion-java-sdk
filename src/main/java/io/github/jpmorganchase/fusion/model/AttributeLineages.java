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
public class AttributeLineages extends CatalogResource {

    Set<AttributeLineage> attributeLineages;

    @Builder(toBuilder = true)
    public AttributeLineages(
            @Builder.ObtainVia(method = "getIdentifier") String identifier,
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getFusion") Fusion fusion,
            @Builder.ObtainVia(method = "getCatalogIdentifier") String catalogIdentifier,
            Set<AttributeLineage> attributeLineages) {
        super(identifier, varArgs, fusion, catalogIdentifier);
        this.attributeLineages = attributeLineages;
    }

    @Override
    protected String getApiPath() {
        return String.format(
                "%1scatalogs/%2s/attributes/lineage", getFusion().getRootURL(), this.getCatalogIdentifier());
    }

    public static class AttributeLineagesBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        private Set<AttributeLineage> attributeLineages;

        public AttributeLineages.AttributeLineagesBuilder varArg(String key, Object value) {
            this.varArgs = VarArgsHelper.varArg(key, value, this.varArgs);
            return this;
        }

        public AttributeLineages.AttributeLineagesBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }

        public AttributeLineages.AttributeLineagesBuilder attributeLineage(AttributeLineage attributeLineage) {
            initialiseAttributeLineages();

            this.attributeLineages.add(attributeLineage);
            return this;
        }

        public AttributeLineages.AttributeLineagesBuilder attributeLineages(Set<AttributeLineage> attributeLineages) {
            initialiseAttributeLineages();

            this.attributeLineages.addAll(attributeLineages);
            return this;
        }

        private void initialiseAttributeLineages() {
            if (null == this.attributeLineages) {
                this.attributeLineages = new HashSet<>();
            }
        }
    }
}
