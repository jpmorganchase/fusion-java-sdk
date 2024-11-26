package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;

@ExtendWith(WireMockExtension.class)
public class DataDictionaryAttributeOperationsIT {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @RegisterExtension
    public static WireMockExtension wireMockRule = WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig().dynamicPort()).build();

    private Fusion sdk;

    @BeforeEach
    public void setUp() {
        int port = wireMockRule.getRuntimeInfo().getHttpPort();
        logger.debug("Wiremock is configured to port {}", port);

        sdk = Fusion.builder()
                .bearerToken("my-token")
                .configuration(FusionConfiguration.builder()
                        .rootURL("http://localhost:" + port + "/")
                        .build()).build();
    }


    @Test
    public void testCreateDataDictionaryAttribute() {

        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/attributes/AT0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-attribute/attribute-AT0001-create-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda = sdk.builders().dataDictionaryAttribute()
                .identifier("AT0001")
                .title("Sample Attribute 1")
                .description("Sample dd attribute description 1")
                .varArg("applicationId", Application.builder().sealId("12345"))
                .build();

        //When
        dda.create();

        //Then
        //TODO :: Need to assert something here; return to be formulated

    }

    @Test
    public void testCreateDataDictionaryAttributeWithVarArgs() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/attributes/AT0002"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-attribute/attribute-AT0002-create-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda = sdk.builders().dataDictionaryAttribute()
                .identifier("AT0002")
                .title("Sample Attribute 2")
                .description("Sample dd attribute description 2")
                .build();

        //When
        dda.create();

        //Then
        //TODO :: Need to assert something here; return to be formulated
    }

    @Test
    public void testCreateDataDictionaryAttributeWithOverrideDefaultCatalog() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/foobar/attributes/AT0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-attribute/attribute-AT0001-create-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda = sdk.builders().dataDictionaryAttribute()
                .identifier("AT0001")
                .title("Sample Attribute 1")
                .description("Sample dd attribute description 1")
                .varArg("applicationId", Application.builder().sealId("12345"))
                .catalogIdentifier("foobar")
                .build();

        //When
        dda.create();

        //Then
        //TODO :: Need to assert something here; return to be formulated
    }

    @Test
    public void testUpdateDataDictionaryAttribute() {
        // Given
        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/common/attributes/AT0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-attribute/attribute-AT0001-create-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda = sdk.builders().dataDictionaryAttribute()
                .identifier("AT0001")
                .title("Sample Attribute 1")
                .description("Sample dd attribute description 1")
                .varArg("applicationId", Application.builder().sealId("12345"))
                .build();

        //When
        dda.update();

        //Then
        //TODO :: Need to assert something here; return to be formulated
    }

    @Test
    public void testUpdateDataDictionaryAttributeWithOverrideDefaultCatalog() {
        // Given
        wireMockRule.stubFor(WireMock.put(WireMock.urlEqualTo("/catalogs/foobar/attributes/AT0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("data-dictionary-attribute/attribute-AT0001-create-request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda = sdk.builders().dataDictionaryAttribute()
                .identifier("AT0001")
                .title("Sample Attribute 1")
                .description("Sample dd attribute description 1")
                .varArg("applicationId", Application.builder().sealId("12345"))
                .catalogIdentifier("foobar")
                .build();

        //When
        dda.update();

        //Then
        //TODO :: Need to assert something here; return to be formulated
    }

    @Test
    public void testUpdateDataDictionaryAttributeRetrievedFromListFunction(){}

    @Test
    public void testDeleteDataDictionaryAttribute(){
        // Given
        wireMockRule.stubFor(WireMock.delete(WireMock.urlEqualTo("/catalogs/common/attributes/AT0001"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda = sdk.builders().dataDictionaryAttribute()
                .identifier("AT0001")
                .build();

        //When
        dda.delete();

        //Then
        //TODO :: Need to assert something here; return to be formulated
    }

    @Test
    public void testDeleteDataDictionaryAttributeWithCatalogOverride(){
        // Given
        wireMockRule.stubFor(WireMock.delete(WireMock.urlEqualTo("/catalogs/foobar/attributes/AT0001"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        DataDictionaryAttribute dda = sdk.builders().dataDictionaryAttribute()
                .identifier("AT0001")
                .catalogIdentifier("foobar")
                .build();

        //When
        dda.delete();

        //Then
        //TODO :: Need to assert something here; return to be formulated
    }


}
