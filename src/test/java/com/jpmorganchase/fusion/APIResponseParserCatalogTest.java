package com.jpmorganchase.fusion;

import com.jpmorganchase.fusion.model.Catalog;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

public class APIResponseParserCatalogTest {

    private static final String singleCatalogJson = loadTestResource("single-catalog-response.json");
    private static final String multipleCatalogJson = loadTestResource("multiple-catalog-response.json");

    private static final Catalog testCatalog = Catalog.builder()
            .identifier("test")
            .description("A catalog used for test cases")
            .linkedEntity("test/")
            .title("Test data catalog")
            .build();

    private static final Catalog testCatalog2 = Catalog.builder()
            .identifier("test2")
            .description("A second catalog used for test cases")
            .linkedEntity("test2/")
            .title("Test data catalog 2")
            .build();

    private static final Catalog testCatalog3 = Catalog.builder()
            .identifier("test3")
            .description("A third catalog used for test cases")
            .linkedEntity("test3/")
            .title("Test data catalog 3")
            .build();

    private static final APIResponseParser responseParser = new APIResponseParser();

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

    private static String loadTestResource(String resourceName) {
        URL url = APIResponseParser.class.getResource(resourceName);
        try {
            Path path = Paths.get(url.toURI());
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data", e);
        }
    }
}
