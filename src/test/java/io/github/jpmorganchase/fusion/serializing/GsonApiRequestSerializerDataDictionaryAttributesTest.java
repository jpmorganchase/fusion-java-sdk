package io.github.jpmorganchase.fusion.serializing;

import io.github.jpmorganchase.fusion.model.Application;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttributes;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class GsonApiRequestSerializerDataDictionaryAttributesTest {

    @Test
    public void testDataDictionaryAttributesSerializesCorrectly() {
        // Given
        DataDictionaryAttribute dda1 = DataDictionaryAttribute.builder()
                .identifier("AT0001")
                .description("Sample dd attribute description 1")
                .title("Sample Attribute 1")
                .varArg("applicationId", Application.builder().sealId("12345"))
                .build();

        DataDictionaryAttribute dda2 = DataDictionaryAttribute.builder()
                .identifier("AT0002")
                .description("Sample dd attribute description 2")
                .title("Sample Attribute 2")
                .varArg("applicationId", Application.builder().sealId("12345"))
                .build();

        DataDictionaryAttributes ddAttributes = DataDictionaryAttributes.builder()
                .dataDictionaryAttribute(dda1)
                .dataDictionaryAttribute(dda2)
                .build();

        GsonAPIRequestSerializer serializer = new GsonAPIRequestSerializer();

        // When
        String actual = serializer.serialize(ddAttributes.getDataDictionaryAttributes());

        String expected = loadTestResource("dd-attributes-request.json");
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
