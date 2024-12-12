package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.jpmorganchase.fusion.model.Attribute;
import io.github.jpmorganchase.fusion.model.Attributes;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;

public class AttributesOperationsIT extends BaseOperationsIT {

    @Test
    public void testCreateAttributes() {
        // Given
        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/datasets/dataset1/attributes"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("attribute/attributes-bulk-update-request.json")))
                        .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Attribute attr1 = getSdk().builders().attribute()
                .identifier("AT0001")
                .title("Attribute 1")
                .description("Description of Attribute 1")
                .datasetIdentifier("dataset1")
                .index(0)
                .build();

        Attribute attr2 = getSdk().builders().attribute()
                .identifier("AT0002")
                .title("Attribute 2")
                .description("Description of Attribute 2")
                .datasetIdentifier("dataset1")
                .index(1)
                .build();

        Attributes attributes = getSdk().builders().attributes()
                .attribute(attr1)
                .attribute(attr2)
                .catalogIdentifier("common")
                .datasetIdentifier("dataset1")
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(attributes::create);
    }


    @Test
    public void testUpdateAttributes() {
        // Given
        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/datasets/dataset1/attributes"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("attribute/attributes-bulk-update-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        Attribute attr1 = getSdk().builders().attribute()
                .identifier("AT0001")
                .title("Attribute 1")
                .description("Description of Attribute 1")
                .datasetIdentifier("dataset1")
                .index(0)
                .build();

        Attribute attr2 = getSdk().builders().attribute()
                .identifier("AT0002")
                .title("Attribute 2")
                .description("Description of Attribute 2")
                .datasetIdentifier("dataset1")
                .index(1)
                .build();

        Attributes attributes = getSdk().builders().attributes()
                .attribute(attr1)
                .attribute(attr2)
                .catalogIdentifier("common")
                .datasetIdentifier("dataset1")
                .build();


        // When & Then
        Assertions.assertDoesNotThrow(attributes::update);
    }
}
