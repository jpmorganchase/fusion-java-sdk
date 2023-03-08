package com.jpmorganchase.fusion;

import com.jpmorganchase.fusion.model.Attribute;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class APIResponseParserAttributeTest {

    private static final String singleAttributeJson = loadTestResource("single-attribute-response.json");
    private static final String multipleAttributeJson = loadTestResource("multiple-attribute-response.json");

    //TODO: Need to map out all the fields
    private static final Attribute testAttribute = Attribute.builder()
            .identifier("name")
            .key(true)
            .dataType("String")
            .description("The name")
            .title("Name")
            .index(0)
            .build();

    private static final Attribute testAttribute2 = Attribute.builder()
            .identifier("currency")
            .key(false)
            .dataType("String")
            .description("The currency")
            .title("Currency")
            .index(1)
            .build();

    private static final Attribute testAttribute3 = Attribute.builder()
            .identifier("term")
            .key(false)
            .dataType("String")
            .description("The term")
            .title("Term")
            .index(2)
            .build();

    private static final APIResponseParser responseParser = new APIResponseParser();

    @Test
    public void singleAttributeInResourcesParsesCorrectly() {
        Map<String, Attribute> attributeMap = responseParser.parseAttributeResponse(singleAttributeJson);
        assertThat(attributeMap.size(), is(1));

        Attribute testAttributeResponse = attributeMap.get("name");
        assertThat(testAttributeResponse, is(equalTo(testAttribute)));
    }

    @Test
    public void multipleCatalogsInResourcesParseCorrectly() {
        Map<String, Attribute> attributeMap = responseParser.parseAttributeResponse(multipleAttributeJson);
        assertThat(attributeMap.size(), is(3));

        Attribute testAttributeResponse = attributeMap.get("name");
        assertThat(testAttributeResponse, is(equalTo(testAttribute)));

        Attribute testAttributeResponse2 = attributeMap.get("currency");
        assertThat(testAttributeResponse2, is(equalTo(testAttribute2)));

        Attribute testAttributeResponse3 = attributeMap.get("term");
        assertThat(testAttributeResponse3, is(equalTo(testAttribute3)));
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
