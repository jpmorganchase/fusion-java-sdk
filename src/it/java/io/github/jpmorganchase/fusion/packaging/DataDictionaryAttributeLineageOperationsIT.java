package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.Catalog;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttributeLineage;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DataDictionaryAttributeLineageOperationsIT extends BaseOperationsIT {

    @Test
    public void testSetDataDictionaryAttributeLineageWithBuilder() {

        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/base/attributes/BA0001/lineage"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-lineage/lineage-BA0001-DR0001-create-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute base = getSdk().builders().dataDictionaryAttribute()
                .identifier("BA0001")
                .catalogIdentifier("base")
                .build();

        DataDictionaryAttribute derived = getSdk().builders().dataDictionaryAttribute()
                .identifier("DR0001")
                .catalogIdentifier("derived")
                .build();

        //When
        base.setLinage(derived);

        //Then

    }

    @Test
    public void testUpdateDataDictionaryAttributeLineageWithBuilder() {
        // Given
        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/base/attributes/BA0001/lineage"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-lineage/lineage-BA0001-DR0002-create-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute base = getSdk().builders().dataDictionaryAttribute()
                .identifier("BA0001")
                .catalogIdentifier("base")
                .build();

        DataDictionaryAttribute derived = getSdk().builders().dataDictionaryAttribute()
                .identifier("DR0002")
                .catalogIdentifier("derived")
                .build();

        //When
        base.updateLineage(derived);

        //Then
    }

    @Test
    public void testDeleteDataDictionaryAttributeLineage(){
        // Given
        wireMockRule.stubFor(WireMock.delete(WireMock.urlEqualTo("/catalogs/base/attributes/BA0001/lineage"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute base = getSdk().builders().dataDictionaryAttribute()
                .identifier("BA0001")
                .catalogIdentifier("base")
                .build();

        //When
        base.deleteLineage();

        //Then
    }


    @Test
    public void testUpdateDataDictionaryAttributeLineageRetrievedFromGetFunction(){
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/base/attributes/BA0001/lineage"))
                .willReturn(WireMock.aResponse()
                        .withBody(TestUtils.loadJsonForIt("data-dictionary-lineage/lineage-BA0001-get-response.json"))
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        // When
        DataDictionaryAttributeLineage actual = getSdk().dataDictionaryAttributeLineage("base", "BA0001");

        // Then Verify the response
        assertThat(actual.getIdentifier(), is(equalTo("DR0001")));
        assertThat(actual.getDescription(), is(equalTo("Derived Attribute Description")));
        assertThat(actual.getTitle(), is(equalTo("Derived Attribute Title")));
        assertThat(actual.getLinkedEntity(), is(equalTo("lineage/")));
        assertThat(actual.getCatalogIdentifier(), is(equalTo("derived")));
        assertThat(actual.getApplicationId(), is(equalTo(Application.builder().sealId("12345").build())));
        assertThat(actual.getApiManager(), is(Matchers.notNullValue()));
        assertThat(actual.getRootUrl(), is(equalTo(getSdk().getRootURL())));
        assertThat(actual.getBaseCatalogIdentifier(), is(equalTo("base")));
        assertThat(actual.getBaseIdentifier(), is(equalTo("BA0001")));
        assertThat(actual.getCatalog(), is(equalTo(Catalog.builder()
                .identifier("derived")
                .description("Derived Catalog Description")
                .title("Derived Catalog Title")
                .linkedEntity("derived/")
                .build())));



    }


}
