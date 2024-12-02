package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;

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
    public void testUpdateDataDictionaryAttributeLineageRetrievedFromListFunction(){
        // Given


        // When

        // Then Verify the response
    }


}
