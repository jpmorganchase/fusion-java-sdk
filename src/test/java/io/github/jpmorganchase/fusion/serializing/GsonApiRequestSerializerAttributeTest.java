package io.github.jpmorganchase.fusion.serializing;

import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.model.Attribute;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GsonApiRequestSerializerAttributeTest {

    @Test
    public void testAttributeSerializesCorrectly() {
        // Given
        Attribute a = Attribute.builder()
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
                .apiManager(Mockito.mock(APIManager.class))
                .rootUrl("http://foo/bar")
                .catalogIdentifier("foobar")
                .dataset("set me")
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(a);

        String expected = loadTestResource("attribute-request.json");
        MatcherAssert.assertThat(actual, Matchers.is(Matchers.equalTo(expected)));
    }

    @Test
    public void testAttributeWithoutVarArgsSerializesCorrectly() {
        // Given
        Attribute a = Attribute.builder()
                .identifier("name")
                .key(true)
                .dataType("String")
                .description("The name")
                .title("Name")
                .index(0)
                .apiManager(Mockito.mock(APIManager.class))
                .rootUrl("http://foo/bar")
                .catalogIdentifier("foobar")
                .dataset("set me")
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(a);

        String expected = loadTestResource("attribute-no-varargs-request.json");
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
