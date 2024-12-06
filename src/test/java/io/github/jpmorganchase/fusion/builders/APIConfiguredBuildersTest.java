package io.github.jpmorganchase.fusion.builders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.github.jpmorganchase.fusion.Fusion;
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

    @BeforeEach
    public void setUp() {

        Fusion fusion = Mockito.mock(Fusion.class);
        apiConfiguredBuilders = new APIConfiguredBuilders(fusion);
    }

    @Test
    public void testDatasetBuilderReturned() {
        // Given
        Dataset.DatasetBuilder datasetBuilder = apiConfiguredBuilders.dataset();

        // When
        Dataset d = datasetBuilder.build();

        // Then
        thenFusionShouldBeNonNull(d);
    }

    @Test
    public void testDataDictionaryAttributeBuilderReturned() {
        // Given
        DataDictionaryAttribute.DataDictionaryAttributeBuilder attributeBuilder =
                apiConfiguredBuilders.dataDictionaryAttribute();

        // When
        DataDictionaryAttribute a = attributeBuilder.build();

        // Then
        thenFusionShouldBeNonNull(a);
    }

    @Test
    public void testAttributeBuilderReturned() {
        // Given
        Attribute.AttributeBuilder attributeBuilder = apiConfiguredBuilders.attribute();

        // When
        Attribute a = attributeBuilder.build();

        // Then
        thenFusionShouldBeNonNull(a);
    }

    private static void thenFusionShouldBeNonNull(CatalogResource cr) {
        assertThat(cr.getFusion(), is(notNullValue()));
    }
}
