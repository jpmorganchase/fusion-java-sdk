package io.github.jpmorganchase.fusion.builders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import io.github.jpmorganchase.fusion.model.Dataset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class APIConfiguredBuildersTest {
    private APIConfiguredBuilders apiConfiguredBuilders;
    private FusionConfiguration configuration;

    @BeforeEach
    public void setUp() {

        APIManager mockApiManager = Mockito.mock(APIManager.class);
        configuration = Mockito.mock(FusionConfiguration.class);
        apiConfiguredBuilders = APIConfiguredBuilders.builder()
                .apiManager(mockApiManager)
                .configuration(configuration)
                .build();
    }

    @Test
    public void testDatasetBuilderReturned() {
        // Given
        Mockito.when(configuration.getRootURL()).thenReturn("http://foobar.com/api/v1");
        Mockito.when(configuration.getDefaultCatalog()).thenReturn("foobar");

        Dataset.DatasetBuilder datasetBuilder = apiConfiguredBuilders.dataset();
        assertNotNull(datasetBuilder);

        Dataset d = datasetBuilder.build();
        assertThat(d.getRootUrl(), is(equalTo("http://foobar.com/api/v1")));
        assertThat(d.getCatalogIdentifier(), is(equalTo("foobar")));
    }

    @Test
    public void testDataDictionaryAttributeBuilderReturned() {
        // Given
        Mockito.when(configuration.getRootURL()).thenReturn("http://foobar.com/api/v1");
        Mockito.when(configuration.getDefaultCatalog()).thenReturn("foobar");

        DataDictionaryAttribute.DataDictionaryAttributeBuilder attributeBuilder =
                apiConfiguredBuilders.dataDictionaryAttribute();
        assertNotNull(attributeBuilder);

        DataDictionaryAttribute a = attributeBuilder.build();
        assertThat(a.getRootUrl(), is(equalTo("http://foobar.com/api/v1")));
        assertThat(a.getCatalogIdentifier(), is(equalTo("foobar")));
    }
}
