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
    public Report.ReportBuilder report() {
        return Report.builder().fusion(fusion).type(DatasetType.REPORT.getLabel());
    }

    @Override
    public DataFlow.DataFlowBuilder dataFlow() {
        return DataFlow.builder().fusion(fusion).type(DatasetType.FLOW.getLabel());
    }

    @Override
    public DataDictionaryAttribute.DataDictionaryAttributeBuilder dataDictionaryAttribute() {
        return DataDictionaryAttribute.builder().fusion(fusion);
    }

    @Override
    public DataDictionaryAttributes.DataDictionaryAttributesBuilder dataDictionaryAttributes() {
        return DataDictionaryAttributes.builder().fusion(fusion);
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
