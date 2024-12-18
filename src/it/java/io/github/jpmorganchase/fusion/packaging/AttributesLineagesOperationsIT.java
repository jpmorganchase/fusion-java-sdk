package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class AttributesLineagesOperationsIT extends BaseOperationsIT {


    @Test
    public void testCreateAttributeLineages() {

        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/c1/attributes/lineage"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("attribute-lineages/attribute-lineages-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        AttributeLineage l1 = AttributeLineage.builder()
                .source(givenAttributeReference("AR0001", "c1", "111111"))
                .target(givenAttributeReference("AR0002", "c2", "222222"))
                .target(givenAttributeReference("AR0003", "c3", "333333"))
                .build();

        AttributeLineage l2 = AttributeLineage.builder()
                .source(givenAttributeReference("AR0004", "c1", "111111"))
                .target(givenAttributeReference("AR0005", "c4", "444444"))
                .target(givenAttributeReference("AR0006", "c5", "555555"))
                .build();

        AttributeLineages lineages  = getSdk().builders().attributeLineages()
                .catalogIdentifier("c1")
                .attributeLineage(l1)
                .attributeLineage(l2)
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(lineages::create);
    }

    private static AttributeReference givenAttributeReference(String attribute, String catalog, String sealId) {
        AttributeReference source = AttributeReference.builder()
                .attribute(attribute)
                .catalog(catalog)
                .applicationId(Application.builder().sealId(sealId).build())
                .build();
        return source;
    }

}
