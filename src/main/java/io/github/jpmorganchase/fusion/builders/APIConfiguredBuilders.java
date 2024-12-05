package io.github.jpmorganchase.fusion.builders;

import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.api.context.APIContext;
import io.github.jpmorganchase.fusion.model.Attribute;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import io.github.jpmorganchase.fusion.model.Dataset;
import io.github.jpmorganchase.fusion.parsing.APIResponseParser;
import lombok.Builder;

@Builder
public class APIConfiguredBuilders implements Builders {

    Fusion fusion;

    @Override
    public Dataset.DatasetBuilder dataset() {
        return Dataset.builder()
                .fusion(fusion);
    }

    @Override
    public DataDictionaryAttribute.DataDictionaryAttributeBuilder dataDictionaryAttribute() {
        return DataDictionaryAttribute.builder()
                .fusion(fusion);
    }

    @Override
    public Attribute.AttributeBuilder attribute() {
        return Attribute.builder()
                .fusion(fusion);

    }
}
