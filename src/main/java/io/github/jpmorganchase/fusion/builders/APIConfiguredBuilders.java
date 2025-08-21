package io.github.jpmorganchase.fusion.builders;

import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.model.*;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class APIConfiguredBuilders implements Builders {

    Fusion fusion;

    @Override
    public Dataset.DatasetBuilder dataset() {
        return Dataset.builder().fusion(fusion);
    }

    @Override
    public DataFlow.DataFlowBuilder dataFlow() {
        return DataFlow.builder().fusion(fusion).type(DatasetType.FLOW.getLabel());
    }

    @Override
    public Attribute.AttributeBuilder attribute() {
        return Attribute.builder().fusion(fusion);
    }

    @Override
    public Attributes.AttributesBuilder attributes() {
        return Attributes.builder().fusion(fusion);
    }

    @Override
    public AttributeLineages.AttributeLineagesBuilder attributeLineages() {
        return AttributeLineages.builder().fusion(fusion);
    }
}
