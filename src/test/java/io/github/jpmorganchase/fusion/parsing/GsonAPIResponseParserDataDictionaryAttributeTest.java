package io.github.jpmorganchase.fusion.parsing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.api.context.APIContext;
import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GsonAPIResponseParserDataDictionaryAttributeTest {

    private static final String singleDataDictionaryAttributeJson =
            loadTestResource("single-dd-attribute-response.json");
    private static final String multipleDataDictionaryAttributeJson =
            loadTestResource("multiple-dd-attribute-response.json");

    private final DataDictionaryAttribute attribute = DataDictionaryAttribute.builder()
            .identifier("AT0001")
            .description("Sample attribute description 1")
            .title("Sample Attribute 1")
            .catalogIdentifier(apiContext.getDefaultCatalog())
            .varArg(
                    "applicationId",
                    Application.builder().sealId("12345").build().toMap())
            .apiManager(apiContext.getApiManager())
            .rootUrl(apiContext.getRootUrl())
            .catalogIdentifier(apiContext.getDefaultCatalog())
            .build();

    private final DataDictionaryAttribute attribute2 = DataDictionaryAttribute.builder()
            .identifier("AT0002")
            .description("Sample attribute description 2")
            .title("Sample Attribute 2")
            .catalogIdentifier(apiContext.getDefaultCatalog())
            .varArg(
                    "applicationId",
                    Application.builder().sealId("12345").build().toMap())
            .apiManager(apiContext.getApiManager())
            .rootUrl(apiContext.getRootUrl())
            .catalogIdentifier(apiContext.getDefaultCatalog())
            .build();

    private final DataDictionaryAttribute attribute3 = DataDictionaryAttribute.builder()
            .identifier("AT0003")
            .description("Sample attribute description 3")
            .title("Sample Attribute 3")
            .catalogIdentifier(apiContext.getDefaultCatalog())
            .varArg(
                    "applicationId",
                    Application.builder().sealId("12345").build().toMap())
            .apiManager(apiContext.getApiManager())
            .rootUrl(apiContext.getRootUrl())
            .catalogIdentifier(apiContext.getDefaultCatalog())
            .build();

    private static final APIContext apiContext = APIContext.builder()
            .apiManager(Mockito.mock(APIManager.class))
            .rootUrl("http://foobar/api/v1/")
            .defaultCatalog("foobar")
            .build();

    private static final APIResponseParser responseParser = new GsonAPIResponseParser(apiContext);

    @Test
    public void singleDatasetInResourcesParsesCorrectly() {
        Map<String, DataDictionaryAttribute> datasetMap =
                responseParser.parseDataDictionaryAttributeResponse(singleDataDictionaryAttributeJson);
        assertThat(datasetMap.size(), is(1));

        DataDictionaryAttribute dda = datasetMap.get("AT0001");
        assertThat(dda, is(equalTo(attribute)));
    }

    @Test
    public void multipleCatalogsInResourcesParseCorrectly() {
        Map<String, DataDictionaryAttribute> datasetMap =
                responseParser.parseDataDictionaryAttributeResponse(multipleDataDictionaryAttributeJson);
        assertThat(datasetMap.size(), is(3));

        DataDictionaryAttribute dda = datasetMap.get("AT0001");
        assertThat(dda, is(equalTo(attribute)));

        DataDictionaryAttribute dda1 = datasetMap.get("AT0002");
        assertThat(dda1, is(equalTo(attribute2)));

        DataDictionaryAttribute dda2 = datasetMap.get("AT0003");
        assertThat(dda2, is(equalTo(attribute3)));
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