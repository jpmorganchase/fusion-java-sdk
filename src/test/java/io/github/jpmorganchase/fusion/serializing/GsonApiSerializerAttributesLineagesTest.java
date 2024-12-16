package io.github.jpmorganchase.fusion.serializing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.serializing.adapters.AttributeLineagesSerializer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class GsonApiSerializerAttributesLineagesTest {

    @Test
    public void testAttributeLineagesSerialization() {
        // Given
        AttributeLineage l1 = AttributeLineage.builder()
                .source(givenAttributeReference("AR0001", "c1", "111111"))
                .target(givenAttributeReference("AR0002", "c2", "222222"))
                .target(givenAttributeReference("AR0003", "c3", "333333"))
                .build();

        AttributeLineage l2 = AttributeLineage.builder()
                .source(givenAttributeReference("AR0004", "c1", "111111"))
                .target(givenAttributeReference("AR0005", "c4", "444444"))
                .target(givenAttributeReference("AR0006", "c5", "555555"))
                .build();

        Set<AttributeLineage> lineageSet = new HashSet<>();
        lineageSet.add(l1);
        lineageSet.add(l2);

        AttributeLineages attributeLineages = AttributeLineages.builder()
                .catalogIdentifier("c1")
                .attributeLineages(lineageSet)
                .build();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(AttributeLineages.class, new AttributeLineagesSerializer())
                .create();

        // When
        String actual = gson.toJson(attributeLineages);

        // Then
        String expected = loadTestResource("attribute-lineages-create-request.json");
        MatcherAssert.assertThat(actual, Matchers.is(Matchers.equalTo(expected)));
    }

    private static AttributeReference givenAttributeReference(String attribute, String catalog, String sealId) {
        return AttributeReference.builder()
                .attribute(attribute)
                .catalog(catalog)
                .applicationId(Application.builder().sealId(sealId).build())
                .build();
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
