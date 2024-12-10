package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.jpmorganchase.fusion.model.Attribute;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;

@ExtendWith(WireMockExtension.class)
public class AttributeOperationsIT extends BaseOperationsIT {

    @Test
    public void testCreateAttribute() {

        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SD0001/attributes/name"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("attribute/attribute-name-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Attribute a = getSdk().builders().attribute()
                .datasetIdentifier("SD0001")
                .identifier("name")
                .title("Name")
                .description("The name")
                .index(0)
                .key(true)
                .isCriticalDataElement(true)
                .varArg("source", "Source System 1")
                .varArg("term", "bizterm1")
                .varArg("dataType", "String")
                .varArg("sourceFieldId", "src_name")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(a::create);

    }

    @Test
    public void testCreateAttributeWithoutVarArgs() {

        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SD0001/attributes/alternate"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("attribute/attribute-alternate-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Attribute a = getSdk().builders().attribute()
                .datasetIdentifier("SD0001")
                .identifier("alternate")
                .title("Alternate")
                .description("The alternate")
                .index(0)
                .key(true)
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(a::create);

    }

    @Test
    public void testCreateAttributeWithCatalogOverride() {

        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/foobar/datasets/SD0001/attributes/name"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("attribute/attribute-name-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Attribute a = getSdk().builders().attribute()
                .datasetIdentifier("SD0001")
                .identifier("name")
                .title("Name")
                .description("The name")
                .index(0)
                .key(true)
                .varArg("source", "Source System 1")
                .varArg("term", "bizterm1")
                .varArg("dataType", "String")
                .varArg("sourceFieldId", "src_name")
                .catalogIdentifier("foobar")
                .isCriticalDataElement(true)
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(a::create);
    }

    @Test
    public void testUpdateAttribute() {

        // Given
        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/datasets/SD0001/attributes/name"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("attribute/attribute-name-update-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Attribute a = getSdk().builders().attribute()
                .datasetIdentifier("SD0001")
                .identifier("name")
                .title("Name")
                .description("The name updated")
                .index(0)
                .key(true)
                .varArg("source", "Source System 1")
                .varArg("term", "bizterm1")
                .varArg("dataType", "String")
                .varArg("sourceFieldId", "src_name")
                .varArg("id", 1)
                .isCriticalDataElement(true)
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(a::update);
    }

    @Test
    public void testUpdateAttributeWithCatalogOverride() {

        // Given
        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/foobar/datasets/SD0001/attributes/name"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("attribute/attribute-name-update-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Attribute a = getSdk().builders().attribute()
                .datasetIdentifier("SD0001")
                .identifier("name")
                .title("Name")
                .description("The name updated")
                .index(0)
                .key(true)
                .varArg("source", "Source System 1")
                .varArg("term", "bizterm1")
                .varArg("dataType", "String")
                .varArg("sourceFieldId", "src_name")
                .varArg("id", 1)
                .catalogIdentifier("foobar")
                .isCriticalDataElement(true)
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(a::update);

    }

    @Test
    public void testUpdateAttributeRetrieveFromListFunction() {

        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common/datasets/SD0001/attributes"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("attribute/multiple-attribute-response.json")));

        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/datasets/SD0001/attributes/name"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("attribute/attribute-name-update-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Map<String, Attribute> attributes = getSdk().listAttributes("SD0001");
        Attribute original = attributes.get("name");

        Attribute a = original.toBuilder()
                .description("The name updated")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(a::update);
    }

    @Test
    public void testDeleteAttribute() {

        // Given
        wireMockRule.stubFor(WireMock.delete(WireMock.urlEqualTo("/catalogs/common/datasets/SD0001/attributes/name"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Attribute a = getSdk().builders().attribute()
                .datasetIdentifier("SD0001")
                .identifier("name")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(a::delete);
    }

    @Test
    public void testDeleteAttributeWithCatalogOverride() {

        // Given
        wireMockRule.stubFor(WireMock.delete(WireMock.urlEqualTo("/catalogs/foobar/datasets/SD0001/attributes/name"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Attribute a = getSdk().builders().attribute()
                .catalogIdentifier("foobar")
                .datasetIdentifier("SD0001")
                .identifier("name")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(a::delete);
    }
}
