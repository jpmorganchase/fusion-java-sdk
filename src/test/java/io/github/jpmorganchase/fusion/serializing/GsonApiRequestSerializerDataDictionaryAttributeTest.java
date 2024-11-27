package io.github.jpmorganchase.fusion.serializing;

import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class GsonApiRequestSerializerDataDictionaryAttributeTest {

    @Test
    public void testDataDictionaryAttributeSerializesCorrectly() {
        // Given
        DataDictionaryAttribute dda = DataDictionaryAttribute.builder()
                .identifier("AT0001")
                .description("Sample dd attribute description 1")
                .title("Sample Attribute 1")
                .varArg("applicationId", Application.builder().sealId("12345"))
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(dda);

        String expected = loadTestResource("dd-attribute-request.json");
        MatcherAssert.assertThat(actual, Matchers.is(Matchers.equalTo(expected)));
    }

    @Test
    public void testDataDictionaryAttributeWithoutVarArgsSerializesCorrectly() {
        // Given
        DataDictionaryAttribute dda = DataDictionaryAttribute.builder()
                .identifier("AT0001")
                .description("Sample dd attribute description 1")
                .title("Sample Attribute 1")
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(dda);

        String expected = loadTestResource("dd-attribute-no-varargs-request.json");
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
