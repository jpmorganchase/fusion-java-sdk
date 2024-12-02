package io.github.jpmorganchase.fusion.builders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.model.Attribute;
import io.github.jpmorganchase.fusion.model.CatalogResource;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import io.github.jpmorganchase.fusion.model.Dataset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@SuppressWarnings("SameParameterValue")
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
        givenConfigurationReturnsRootUrl("http://foo.com/api/v1");
        givenConfigurationReturnsDefaultCatalog("bar");

        // When
        Dataset.DatasetBuilder datasetBuilder = apiConfiguredBuilders.dataset();
        assertNotNull(datasetBuilder);

        // Then
        Dataset d = datasetBuilder.build();
        thenApiManagerShouldBeNonNull(d);
        thenRootUrlShouldBeEqualTo(d, "http://foo.com/api/v1");
        thenCatalogIdentifierShouldBeEqualTo(d, "bar");
    }

    @Test
    public void testDataDictionaryAttributeBuilderReturned() {
        // Given
        givenConfigurationReturnsRootUrl("http://foo.com/api/v1");
        givenConfigurationReturnsDefaultCatalog("bar");

        // When
        DataDictionaryAttribute.DataDictionaryAttributeBuilder attributeBuilder =
                apiConfiguredBuilders.dataDictionaryAttribute();
        assertNotNull(attributeBuilder);

        // Then
        DataDictionaryAttribute a = attributeBuilder.build();
        thenApiManagerShouldBeNonNull(a);
        thenRootUrlShouldBeEqualTo(a, "http://foo.com/api/v1");
        thenCatalogIdentifierShouldBeEqualTo(a, "bar");
    }

    @Test
    public void testAttributeBuilderReturned() {
        // Given
        givenConfigurationReturnsRootUrl("http://foo.com/api/v1");
        givenConfigurationReturnsDefaultCatalog("bar");

        // When
        Attribute.AttributeBuilder attributeBuilder = apiConfiguredBuilders.attribute();
        assertNotNull(attributeBuilder);

        // Then
        Attribute a = attributeBuilder.build();
        thenApiManagerShouldBeNonNull(a);
        thenRootUrlShouldBeEqualTo(a, "http://foo.com/api/v1");
        thenCatalogIdentifierShouldBeEqualTo(a, "bar");
    }

    private static void thenApiManagerShouldBeNonNull(CatalogResource cr) {
        assertThat(cr.getApiManager(), is(notNullValue()));
    }

    private static void thenCatalogIdentifierShouldBeEqualTo(CatalogResource cr, String catalogIdentifier) {
        assertThat(cr.getCatalogIdentifier(), is(equalTo(catalogIdentifier)));
    }

    private static void thenRootUrlShouldBeEqualTo(CatalogResource cr, String rootUrl) {
        assertThat(cr.getRootUrl(), is(equalTo(rootUrl)));
    }

    private void givenConfigurationReturnsDefaultCatalog(String catalog) {
        Mockito.when(configuration.getDefaultCatalog()).thenReturn(catalog);
    }

    private void givenConfigurationReturnsRootUrl(String rootUrl) {
        Mockito.when(configuration.getRootURL()).thenReturn(rootUrl);
    }
}
