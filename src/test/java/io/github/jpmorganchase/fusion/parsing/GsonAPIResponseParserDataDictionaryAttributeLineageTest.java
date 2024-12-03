package io.github.jpmorganchase.fusion.parsing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.api.context.APIContext;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.Catalog;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttributeLineage;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GsonAPIResponseParserDataDictionaryAttributeLineageTest {

    private static final String singleDataDictionaryAttributeLineageJson =
            loadTestResource("single-dd-attribute-lineage-response.json");

    private final DataDictionaryAttributeLineage lineage = DataDictionaryAttributeLineage.builder()
            .identifier("DR0001")
            .description("Derived Attribute Description")
            .title("Derived Attribute Title")
            .linkedEntity("lineage/")
            .catalogIdentifier(apiContext.getDefaultCatalog())
            .applicationId(Application.builder().sealId("12345").build())
            .apiManager(apiContext.getApiManager())
            .rootUrl(apiContext.getRootUrl())
            .catalogIdentifier("derived")
            .catalog(Catalog.builder()
                    .identifier("derived")
                    .description("Derived Catalog Description")
                    .title("Derived Catalog Title")
                    .linkedEntity("derived/")
                    .build())
            .varArgs(new HashMap<>())
            .baseCatalogIdentifier("base")
            .baseIdentifier("BA0001")
            .build();

    private static final APIContext apiContext = APIContext.builder()
            .apiManager(Mockito.mock(APIManager.class))
            .rootUrl("http://foobar/api/v1/")
            .defaultCatalog("foobar")
            .build();

    private static final APIResponseParser responseParser = new GsonAPIResponseParser(apiContext);

    @Test
    public void singleDataDictionaryAttributeResourceIsParsedCorrectly() {
        DataDictionaryAttributeLineage l = responseParser.parseDataDictionaryAttributeLineageResponse(
                singleDataDictionaryAttributeLineageJson, "base", "BA0001");
        assertThat(l, is(notNullValue()));

        assertThat(l, is(equalTo(lineage)));
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
