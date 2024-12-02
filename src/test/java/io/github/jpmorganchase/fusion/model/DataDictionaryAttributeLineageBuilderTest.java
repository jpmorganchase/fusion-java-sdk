package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.api.APIManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DataDictionaryAttributeLineageBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesMinimumFields() {

        APIManager apiManager = Mockito.mock(APIManager.class);

        DataDictionaryAttributeLineage l = DataDictionaryAttributeLineage.builder()
                .baseIdentifier("BASE_IDENTIFIER")
                .baseCatalogIdentifier("base")
                .apiManager(apiManager)
                .rootUrl("http://foobar/api/v1/")
                .identifier("DERIVED_IDENTIFIER")
                .catalogIdentifier("derived")
                .build();

        assertThat(l.getBaseIdentifier(), is(equalTo("BASE_IDENTIFIER")));
        assertThat(l.getBaseCatalogIdentifier(), is(equalTo("base")));
        assertThat(l.getRootUrl(), is(equalTo("http://foobar/api/v1/")));
        assertThat(l.getIdentifier(), is(equalTo("DERIVED_IDENTIFIER")));
        assertThat(l.getCatalogIdentifier(), is(equalTo("derived")));
        assertThat(
                l.getApiPath(), is(equalTo("http://foobar/api/v1/catalogs/base/attributes/BASE_IDENTIFIER/lineage")));
    }

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {

        APIManager apiManager = Mockito.mock(APIManager.class);
        Application appId = Application.builder().sealId("12345").build();
        Catalog catalog = Catalog.builder()
                .linkedEntity("derived/")
                .identifier("derived")
                .description("derived catalog description")
                .title("Derived Catalog")
                .isInternal(true)
                .build();

        DataDictionaryAttributeLineage l = DataDictionaryAttributeLineage.builder()
                .identifier("DERIVED_IDENTIFIER")
                .description("Description of derived attribute")
                .title("Title of Derived Attribute")
                .linkedEntity("lineage/")
                .catalogIdentifier("derived")
                .applicationId(appId)
                .catalog(catalog)
                .apiManager(apiManager)
                .rootUrl("http://foobar/api/v1/")
                .baseIdentifier("BASE_IDENTIFIER")
                .baseCatalogIdentifier("base")
                .build();

        assertThat(l.getIdentifier(), is(equalTo("DERIVED_IDENTIFIER")));
        assertThat(l.getDescription(), is(equalTo("Description of derived attribute")));
        assertThat(l.getTitle(), is(equalTo("Title of Derived Attribute")));
        assertThat(l.getCatalogIdentifier(), is(equalTo("derived")));
        assertThat(l.getLinkedEntity(), is(equalTo("lineage/")));
        assertThat(l.getApplicationId(), is(equalTo(appId)));
        assertThat(l.getCatalog(), is(equalTo(catalog)));
        assertThat(l.getRootUrl(), is(equalTo("http://foobar/api/v1/")));
        assertThat(l.getBaseIdentifier(), is(equalTo("BASE_IDENTIFIER")));
        assertThat(l.getBaseCatalogIdentifier(), is(equalTo("base")));
        assertThat(
                l.getApiPath(), is(equalTo("http://foobar/api/v1/catalogs/base/attributes/BASE_IDENTIFIER/lineage")));
    }
}
