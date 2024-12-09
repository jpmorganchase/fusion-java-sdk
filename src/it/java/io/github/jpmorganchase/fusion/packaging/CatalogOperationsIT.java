package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.jpmorganchase.fusion.model.Catalog;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class CatalogOperationsIT extends BaseOperationsIT {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void testSingleCatalogUsingIdContains() {
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs/common"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("catalog/catalog-common-response.json")));

        // When
        Map<String, Map<String, Object>> catalogResources = getSdk().catalogResources("common");

        // Then Verify the response
        assertThat(catalogResources.containsKey("datasets"), is(equalTo(true)));
        assertThat(catalogResources.containsKey("products"), is(equalTo(true)));
        assertThat(catalogResources.containsKey("attributes"), is(equalTo(true)));
        assertThat(catalogResources.containsKey("concepts"), is(equalTo(true)));
    }

    @Test
    public void testMultipleCatalogs() {
        // Given
        wireMockRule.stubFor(WireMock.get(WireMock.urlEqualTo("/catalogs"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("catalog/multiple-catalog-response.json")));

        // When
        Map<String, Catalog> catalog = getSdk().listCatalogs();

        // Then Verify the response
        assertThat(catalog.containsKey("test"), is(equalTo(true)));
        assertThat(catalog.containsKey("test2"), is(equalTo(true)));
        assertThat(catalog.containsKey("test3"), is(equalTo(true)));
    }
}
