package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttributes;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DataDictionaryAttributesOperationsIT extends BaseOperationsIT {


    @Test
    public void testCreateDataDictionaryAttributes() {

        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/attributes"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-attribute/attributes-bulk-create-request.json")))
                .withHeader("Content-Type", WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda1 = getSdk().builders().dataDictionaryAttribute()
                .identifier("AT0001")
                .title("Sample Attribute 1")
                .description("Sample dd attribute description 1")
                .applicationId(Application.builder().sealId("12345").build())
                .build();

        DataDictionaryAttribute dda2 = getSdk().builders().dataDictionaryAttribute()
                .identifier("AT0002")
                .title("Sample Attribute 2")
                .description("Sample dd attribute description 2")
                .applicationId(Application.builder().sealId("12345").build())
                .build();

        DataDictionaryAttributes ddas = getSdk().builders().dataDictionaryAttributes()
                .dataDictionaryAttribute(dda1)
                .dataDictionaryAttribute(dda2)
                .build();

        // When & Then
        Assertions.assertDoesNotThrow(ddas::create);
    }


    @Test
    public void testListDataDictionaryAttributes(){
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common/attributes"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("data-dictionary-attribute/multiple-dd-attribute-response.json")));

        //When
        Map<String, DataDictionaryAttribute> attributes = getSdk().listDataDictionaryAttributes("common");

        //Then
        thenTheReturnedAttributesShouldContain(attributes, 1);
        thenTheReturnedAttributesShouldContain(attributes, 2);
        thenTheReturnedAttributesShouldContain(attributes, 3);

    }

    private void thenTheReturnedAttributesShouldContain(Map<String, DataDictionaryAttribute> attributes, int attNum){
        //Then
        String key = "AT000" + attNum;
        assertThat(attributes.containsKey(key), is(true));
        assertThat(attributes.get(key), is(equalTo(getSdk().builders().dataDictionaryAttribute()
                .identifier(key)
                .title("Sample Attribute " + attNum)
                .description("Sample attribute description " + attNum)
                .applicationId(Application.builder().sealId("12345").build())
                .catalogIdentifier("common")
                .varArgs(new HashMap<>())
                .build())));
    }


}
