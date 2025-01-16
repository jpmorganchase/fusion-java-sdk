package io.github.jpmorganchase.fusion.parsing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.jpmorganchase.fusion.model.Catalog;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class GsonAPIResponseParserCatalogTest {

    private static final String singleCatalogJson = loadTestResource("single-catalog-response.json");
    private static final String multipleCatalogJson = loadTestResource("multiple-catalog-response.json");
    private static final String invalidCatalogJson = loadTestResource("invalid-catalog-response.json");
    private static final String noResourcesCatalogJson = loadTestResource("no-resources-catalog-response.json");

    private static final Catalog testCatalog = Catalog.builder()
            .identifier("test")
            .description("A catalog used for test cases")
            .linkedEntity("test/")
            .title("Test data catalog")
            .isInternal(Boolean.FALSE)
            .build();

    private static final Catalog testCatalog2 = Catalog.builder()
            .identifier("test2")
            .description("A second catalog used for test cases")
            .linkedEntity("test2/")
            .title("Test data catalog 2")
            .isInternal(Boolean.FALSE)
            .build();

    private static final Catalog testCatalog3 = Catalog.builder()
            .identifier("test3")
            .description("A third catalog used for test cases")
            .linkedEntity("test3/")
            .title("Test data catalog 3")
            .isInternal(Boolean.FALSE)
            .build();

    private static final APIResponseParser responseParser =
            GsonAPIResponseParser.builder().gson(DefaultGsonConfig.gson()).build();

    @Test
    public void singleCatalogInResourcesParsesCorrectly() {
        Map<String, Catalog> catalogMap = responseParser.parseCatalogResponse(singleCatalogJson);
        assertThat(catalogMap.size(), is(1));

        Catalog testCatalogResponse = catalogMap.get("test");
        assertThat(testCatalogResponse, is(equalTo(testCatalog)));
    }

    @Test
    public void multipleCatalogsInResourcesParseCorrectly() {
        Map<String, Catalog> catalogMap = responseParser.parseCatalogResponse(multipleCatalogJson);
        assertThat(catalogMap.size(), is(3));

        Catalog testCatalogResponse = catalogMap.get("test");
        assertThat(testCatalogResponse, is(equalTo(testCatalog)));

        Catalog testCatalogResponse2 = catalogMap.get("test2");
        assertThat(testCatalogResponse2, is(equalTo(testCatalog2)));

        Catalog testCatalogResponse3 = catalogMap.get("test3");
        assertThat(testCatalogResponse3, is(equalTo(testCatalog3)));
    }

    @Test
    public void missingResourcesSectionInResponseReturnsEmptyMap() {

        Map<String, Catalog> catalog = responseParser.parseCatalogResponse(invalidCatalogJson);

        assertThat(catalog, is(notNullValue()));
        assertThat(catalog, is(anEmptyMap()));
    }

    @Test
    public void emptyResourcesSectionInResponseReturnsEmptyMap() {

        Map<String, Catalog> catalog = responseParser.parseCatalogResponse(noResourcesCatalogJson);

        assertThat(catalog, is(notNullValue()));
        assertThat(catalog, is(anEmptyMap()));
    }

    @Test
    public void missingResourcesSectionInResponseCausesCorrectExceptionForUntypedData() {
        ParsingException thrown =
                assertThrows(ParsingException.class, () -> responseParser.parseResourcesUntyped(invalidCatalogJson));

        assertThat(thrown.getMessage(), is(equalTo("Failed to parse resources from JSON, none found")));
    }

    @Test
    public void emptyResourcesSectionInResponseReturnsEmptyMapForUntypedData() {

        Map<String, Map<String, Object>> actual = responseParser.parseResourcesUntyped(noResourcesCatalogJson);

        assertThat(actual, is(anEmptyMap()));
    }

    private static String loadTestResource(String resourceName) {
        URL url = GsonAPIResponseParser.class.getResource(resourceName);
        try {
            Path path = Paths.get(url.toURI());
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data", e);
        }
    }
}
