package io.github.jpmorganchase.fusion.parsing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.model.Attribute;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GsonAPIResponseParserAttributeTest {

    private static final String singleAttributeJson = loadTestResource("single-attribute-response.json");
    private static final String multipleAttributeJson = loadTestResource("multiple-attribute-response.json");
    private static final String duplicateAttributeJson = loadTestResource("duplicate-attribute-response.json");

    private final Attribute testAttribute = Attribute.builder()
            .identifier("name")
            .key(true)
            .dataType("String")
            .description("The name")
            .title("Name")
            .index(0)
            .varArg("isDatasetKey", true)
            .varArg("term", "bizterm1")
            .varArg("id", 1.0)
            .varArg("source", "Source System 1")
            .varArg("sourceFieldId", "src_name")
            .fusion(fusion)
            .catalogIdentifier("foobar")
            .dataset("SD0001")
            .build();

    private final Attribute testAttribute2 = Attribute.builder()
            .identifier("currency")
            .key(false)
            .dataType("String")
            .description("The currency")
            .title("Currency")
            .index(1)
            .varArg("isDatasetKey", false)
            .varArg("term", "bizterm1")
            .varArg("id", 2.0)
            .varArg("source", "Source System 1")
            .varArg("sourceFieldId", "")
            .fusion(fusion)
            .catalogIdentifier("foobar")
            .dataset("SD0001")
            .build();

    private final Attribute testAttribute3 = Attribute.builder()
            .identifier("term")
            .key(false)
            .dataType("String")
            .description("The term")
            .title("Term")
            .index(2)
            .varArg("isDatasetKey", false)
            .varArg("term", "bizterm1")
            .varArg("id", 3.0)
            .varArg("source", "Source System 1")
            .varArg("sourceFieldId", "")
            .fusion(fusion)
            .catalogIdentifier("foobar")
            .dataset("SD0001")
            .build();

    private static final Fusion fusion = Mockito.mock(Fusion.class);
    private static final APIResponseParser responseParser = GsonAPIResponseParser.builder()
            .gson(DefaultGsonConfig.gson())
            .fusion(fusion)
            .build();

    @Test
    public void singleAttributeInResourcesParsesCorrectly() {
        Map<String, Attribute> attributeMap =
                responseParser.parseAttributeResponse(singleAttributeJson, "foobar", "SD0001");
        assertThat(attributeMap.size(), is(1));

        Attribute testAttributeResponse = attributeMap.get("name");
        assertThat(testAttributeResponse, is(equalTo(testAttribute)));
    }

    @Test
    public void multipleCatalogsInResourcesParseCorrectly() {
        Map<String, Attribute> attributeMap =
                responseParser.parseAttributeResponse(multipleAttributeJson, "foobar", "SD0001");
        assertThat(attributeMap.size(), is(3));

        Attribute testAttributeResponse = attributeMap.get("name");
        assertThat(testAttributeResponse, is(equalTo(testAttribute)));

        Attribute testAttributeResponse2 = attributeMap.get("currency");
        assertThat(testAttributeResponse2, is(equalTo(testAttribute2)));

        Attribute testAttributeResponse3 = attributeMap.get("term");
        assertThat(testAttributeResponse3, is(equalTo(testAttribute3)));
    }

    @Test
    public void duplicateAttributesAreIgnored() {
        Map<String, Attribute> attributeMap =
                responseParser.parseAttributeResponse(duplicateAttributeJson, "foobar", "SD0001");
        assertThat(attributeMap.size(), is(3));

        Attribute testAttributeResponse = attributeMap.get("name");
        assertThat(testAttributeResponse, is(equalTo(testAttribute)));

        Attribute testAttributeResponse2 = attributeMap.get("currency");
        assertThat(testAttributeResponse2, is(equalTo(testAttribute2)));

        Attribute testAttributeResponse3 = attributeMap.get("term");
        assertThat(testAttributeResponse3, is(equalTo(testAttribute3)));
    }

    @Test
    public void untypedParsingReturnsCorrectly() {
        Map<String, Map<String, Object>> attributeMap = responseParser.parseResourcesUntyped(multipleAttributeJson);
        assertThat(attributeMap.size(), is(3));

        Map<String, Object> testAttributeResponse = attributeMap.get("name");
        assertThat(testAttributeResponse.size(), is(10));
        assertThat(testAttributeResponse.get("id"), is(equalTo(1.0)));
        assertThat(testAttributeResponse.get("source"), is(equalTo("Source System 1")));
        assertThat(testAttributeResponse.get("term"), is(equalTo("bizterm1")));
        assertThat(testAttributeResponse.get("dataType"), is(equalTo(testAttribute.getDataType())));
        assertThat(testAttributeResponse.get("description"), is(equalTo(testAttribute.getDescription())));
        assertThat(testAttributeResponse.get("identifier"), is(equalTo(testAttribute.getIdentifier())));
        assertThat(testAttributeResponse.get("index"), is(equalTo(0.0)));
        assertThat(testAttributeResponse.get("isDatasetKey"), is(equalTo(testAttribute.isKey())));
        assertThat(testAttributeResponse.get("sourceFieldId"), is(equalTo("src_name")));
        assertThat(testAttributeResponse.get("title"), is(equalTo(testAttribute.getTitle())));

        Map<String, Object> testAttributeResponse2 = attributeMap.get("currency");
        assertThat(testAttributeResponse2.size(), is(10));
        assertThat(testAttributeResponse2.get("id"), is(equalTo(2.0)));
        assertThat(testAttributeResponse2.get("source"), is(equalTo("Source System 1")));
        assertThat(testAttributeResponse2.get("term"), is(equalTo("bizterm1")));
        assertThat(testAttributeResponse2.get("dataType"), is(equalTo(testAttribute2.getDataType())));
        assertThat(testAttributeResponse2.get("description"), is(equalTo(testAttribute2.getDescription())));
        assertThat(testAttributeResponse2.get("identifier"), is(equalTo(testAttribute2.getIdentifier())));
        assertThat(testAttributeResponse2.get("index"), is(equalTo(1.0)));
        assertThat(testAttributeResponse2.get("isDatasetKey"), is(equalTo(testAttribute2.isKey())));
        assertThat(testAttributeResponse2.get("sourceFieldId"), is(equalTo("")));
        assertThat(testAttributeResponse2.get("title"), is(equalTo(testAttribute2.getTitle())));

        Map<String, Object> testAttributeResponse3 = attributeMap.get("term");
        assertThat(testAttributeResponse3.size(), is(10));
        assertThat(testAttributeResponse3.get("id"), is(equalTo(3.0)));
        assertThat(testAttributeResponse3.get("source"), is(equalTo("Source System 1")));
        assertThat(testAttributeResponse3.get("term"), is(equalTo("bizterm1")));
        assertThat(testAttributeResponse3.get("dataType"), is(equalTo(testAttribute3.getDataType())));
        assertThat(testAttributeResponse3.get("description"), is(equalTo(testAttribute3.getDescription())));
        assertThat(testAttributeResponse3.get("identifier"), is(equalTo(testAttribute3.getIdentifier())));
        assertThat(testAttributeResponse3.get("index"), is(equalTo(2.0)));
        assertThat(testAttributeResponse3.get("isDatasetKey"), is(equalTo(testAttribute3.isKey())));
        assertThat(testAttributeResponse3.get("sourceFieldId"), is(equalTo("")));
        assertThat(testAttributeResponse3.get("title"), is(equalTo(testAttribute3.getTitle())));
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
