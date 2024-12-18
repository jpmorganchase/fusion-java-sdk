package io.github.jpmorganchase.fusion.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode
@ToString
@Builder
public class AttributeLineage {

    AttributeReference source;
    Set<AttributeReference> target;

    public static class AttributeLineageBuilder {

        Set<AttributeReference> target;

        public AttributeLineage.AttributeLineageBuilder target(AttributeReference attributeReference) {
            initialiseAttributeLineages();
            this.target.add(attributeReference);
            return this;
        }

        public AttributeLineage.AttributeLineageBuilder target(Set<AttributeReference> attributeReferences) {
            initialiseAttributeLineages();
            this.target.addAll(attributeReferences);
            return this;
        }

        private void initialiseAttributeLineages() {
            if (null == this.target) {
                this.target = new HashSet<>();
            }
        }
    }
}
