package io.github.jpmorganchase.fusion.serializing;

import io.github.jpmorganchase.fusion.model.Attribute;
import io.github.jpmorganchase.fusion.model.Attributes;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class GsonApiRequestSerializerAttributesTest {

    @Test
    public void testAttributesSerializesCorrectly() {
        // Given
        Attribute attribute1 = Attribute.builder()
                .identifier("AT001")
                .title("Attribute 1")
                .index(0)
                .description("Description of Attribute 1")
                .build();

        Attribute attribute2 = Attribute.builder()
                .identifier("AT002")
                .title("Attribute 2")
                .index(1)
                .description("Description of Attribute 2")
                .build();

        Attributes attributes = Attributes.builder()
                .identifier("Attributes")
                .catalogIdentifier("common")
                .attribute(attribute1)
                .attribute(attribute2)
                .datasetIdentifier("dataset001")
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(attributes.getAttributes());

        String expected = loadTestResource("attributes-bulk-update-request.json");

        // Then
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
