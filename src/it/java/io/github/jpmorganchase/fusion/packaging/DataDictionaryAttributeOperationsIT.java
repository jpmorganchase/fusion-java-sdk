package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;

@Disabled("CRUD Operations currently unsupported on DataDictionaryAttribute")
public class DataDictionaryAttributeOperationsIT extends BaseOperationsIT {


    @Test
    public void testCreateDataDictionaryAttribute() {

        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/attributes/AT0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-attribute/attribute-AT0001-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda = getSdk().builders().dataDictionaryAttribute()
                .identifier("AT0001")
                .title("Sample Attribute 1")
                .description("Sample dd attribute description 1")
                .applicationId(Application.builder().sealId("12345").build())
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(dda::create);
    }

    @Test
    public void testCreateDataDictionaryAttributeWithoutVarArgs() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/attributes/AT0002"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-attribute/attribute-AT0002-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda = getSdk().builders().dataDictionaryAttribute()
                .identifier("AT0002")
                .title("Sample Attribute 2")
                .description("Sample dd attribute description 2")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(dda::create);
    }

    @Test
    public void testCreateDataDictionaryAttributeWithOverrideDefaultCatalog() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/foobar/attributes/AT0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-attribute/attribute-AT0001-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda = getSdk().builders().dataDictionaryAttribute()
                .identifier("AT0001")
                .title("Sample Attribute 1")
                .description("Sample dd attribute description 1")
                .applicationId(Application.builder().sealId("12345").build())
                .catalogIdentifier("foobar")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(dda::create);
    }

    @Test
    public void testUpdateDataDictionaryAttribute() {
        // Given
        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/attributes/AT0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-attribute/attribute-AT0001-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda = getSdk().builders().dataDictionaryAttribute()
                .identifier("AT0001")
                .title("Sample Attribute 1")
                .description("Sample dd attribute description 1")
                .applicationId(Application.builder().sealId("12345").build())
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(dda::update);
    }

    @Test
    public void testUpdateDataDictionaryAttributeWithOverrideDefaultCatalog() {
        // Given
        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/foobar/attributes/AT0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-attribute/attribute-AT0001-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda = getSdk().builders().dataDictionaryAttribute()
                .identifier("AT0001")
                .title("Sample Attribute 1")
                .description("Sample dd attribute description 1")
                .applicationId(Application.builder().sealId("12345").build())
                .catalogIdentifier("foobar")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(dda::update);
    }

    @Test
    public void testUpdateDataDictionaryAttributeRetrievedFromListFunction(){
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common/attributes"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("data-dictionary-attribute/multiple-dd-attribute-response.json")));

        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/attributes/AT0002"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-attribute/attribute-AT0002-update-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Map<String, DataDictionaryAttribute> attributes = getSdk().listDataDictionaryAttributes("common");
        DataDictionaryAttribute original = attributes.get("AT0002");

        // When
        DataDictionaryAttribute amended = original
                .toBuilder()
                .description("Updated Sample dd attribute description 2")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(amended::update);
    }

    @Test
    public void testDeleteDataDictionaryAttribute(){
        // Given
        wireMockRule.stubFor(WireMock.delete(WireMock.urlEqualTo("/catalogs/common/attributes/AT0001"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda = getSdk().builders().dataDictionaryAttribute()
                .identifier("AT0001")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(dda::delete);
    }

    @Test
    public void testDeleteDataDictionaryAttributeWithCatalogOverride(){
        // Given
        wireMockRule.stubFor(WireMock.delete(WireMock.urlEqualTo("/catalogs/foobar/attributes/AT0001"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda = getSdk().builders().dataDictionaryAttribute()
                .identifier("AT0001")
                .catalogIdentifier("foobar")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(dda::delete);
    }


}
