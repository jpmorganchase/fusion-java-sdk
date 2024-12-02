package io.github.jpmorganchase.fusion.serializing;

import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.Catalog;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttributeLineage;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GsonApiRequestSerializerDataDictionaryAttributeLineageTest {

    @Test
    public void testDataDictionaryAttributeLineageSerializesCorrectly() {
        // Given

        APIManager apiManager = Mockito.mock(APIManager.class);
        Application appId = Application.builder().sealId("12345").build();
        Catalog catalog = Catalog.builder()
                .linkedEntity("derived/")
                .identifier("derived")
                .description("derived catalog description")
                .title("Derived Catalog")
                .isInternal(true)
                .build();

        DataDictionaryAttributeLineage lineage = DataDictionaryAttributeLineage.builder()
                .identifier("DERIVED_IDENTIFIER")
                .description("Description of derived attribute")
                .title("Title of Derived Attribute")
                .linkedEntity("lineage/")
                .catalogIdentifier("derived")
                .applicationId(appId)
                .catalog(catalog)
                .apiManager(apiManager)
                .rootUrl("http://foobar/api/v1/")
                .baseIdentifier("BASE_IDENTIFIER")
                .baseCatalogIdentifier("base")
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(lineage);

        String expected = loadTestResource("dd-attribute-lineage-request.json");
        MatcherAssert.assertThat(actual, Matchers.is(Matchers.equalTo(expected)));
    }

    private static String loadTestResource(String resourceName) {
        URL url = GsonAPIRequestSerializer.class.getResource(resourceName);
        try {
            Path path = Paths.get(url.toURI());
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data", e);
        }
    }
}
