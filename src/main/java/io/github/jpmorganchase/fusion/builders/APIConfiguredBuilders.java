package io.github.jpmorganchase.fusion.builders;

import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import io.github.jpmorganchase.fusion.model.Dataset;
import lombok.Builder;

@Builder
public class APIConfiguredBuilders implements Builders {

    APIManager apiManager;
    FusionConfiguration configuration;

    @Override
    public Dataset.DatasetBuilder dataset() {
        return Dataset.builder()
                .apiManager(apiManager)
                .rootUrl(configuration.getRootURL())
                .catalogIdentifier(configuration.getDefaultCatalog());
    }

    @Override
    public DataDictionaryAttribute.DataDictionaryAttributeBuilder dataDictionaryAttribute() {
        return DataDictionaryAttribute.builder()
                .apiManager(apiManager)
                .rootUrl(configuration.getRootURL())
                .catalogIdentifier(configuration.getDefaultCatalog());
    }
}
