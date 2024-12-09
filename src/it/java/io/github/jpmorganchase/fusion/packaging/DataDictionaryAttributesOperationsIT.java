package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttributes;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;

public class DataDictionaryAttributesOperationsIT extends BaseOperationsIT {


    @Test
    public void testCreateDataDictionaryAttributes() {

        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/attributes"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-attribute/attributes-bulk-create-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda1 = getSdk().builders().dataDictionaryAttribute()
                .identifier("AT0001")
                .title("Sample Attribute 1")
                .description("Sample dd attribute description 1")
                .varArg("applicationId", Application.builder().sealId("12345"))
                .build();

        DataDictionaryAttribute dda2 = getSdk().builders().dataDictionaryAttribute()
                .identifier("AT0002")
                .title("Sample Attribute 2")
                .description("Sample dd attribute description 2")
                .varArg("applicationId", Application.builder().sealId("12345"))
                .build();

        DataDictionaryAttributes ddas = getSdk().builders().dataDictionaryAttributes()
                .dataDictionaryAttribute(dda1)
                .dataDictionaryAttribute(dda2)
                .build();

        //When
        ddas.create();

        //Then

    }

}
