package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AttributesTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        // Given
        Fusion fusion = Mockito.mock(Fusion.class);
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");

        List<Attribute> nestedAttributes = new ArrayList<>();
        nestedAttributes.add(Attribute.builder().identifier("nested1").build());

        // When
        Attributes attributes = Attributes.builder()
                .identifier("attributes")
                .catalogIdentifier("foobar")
                .datasetIdentifier("dataset1")
                .fusion(fusion)
                .varArgs(varArgs)
                .attributes(nestedAttributes)
                .build();

        // Then
        assertThat(attributes.getIdentifier(), is(equalTo("attributes")));
        assertThat(attributes.getVarArgs(), is(equalTo(varArgs)));
        assertThat(attributes.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(attributes.getDatasetIdentifier(), is(equalTo("dataset1")));
        assertThat(attributes.getFusion(), is(equalTo(fusion)));
        assertThat(attributes.getAttributes(), is(equalTo(nestedAttributes)));
    }

    @Test
    void constructionWithBuilderCorrectlyAddsSingleNestedAttribute() {
        // Given
        Attribute nestedAttribute = Attribute.builder().identifier("nested1").build();

        // When
        Attributes attributes = Attributes.builder().attribute(nestedAttribute).build();

        // Then
        assertThat(attributes.getAttributes(), hasSize(1));
        assertThat(attributes.getAttributes(), contains(nestedAttribute));
    }

    @Test
    void constructionWithBuilderCorrectlyAddsMultipleNestedAttributes() {
        // Given
        List<Attribute> nestedAttributes = new ArrayList<>();
        nestedAttributes.add(Attribute.builder().identifier("nested1").build());
        nestedAttributes.add(Attribute.builder().identifier("nested2").build());

        // When
        Attributes attributes =
                Attributes.builder().attributes(nestedAttributes).build();

        // Then
        assertThat(attributes.getAttributes(), hasSize(2));
        assertThat(attributes.getAttributes(), contains(nestedAttributes.toArray()));
    }

    @Test
    void constructionWithBuilderCorrectlyReturnsApiPath() {
        // Given
        Fusion fusion = Mockito.mock(Fusion.class);
        when(fusion.getRootURL()).thenReturn("http://foo/api/v1/");

        // When
        Attributes attributes = Attributes.builder()
                .fusion(fusion)
                .catalogIdentifier("bar")
                .datasetIdentifier("dataset1")
                .build();

        // Then
        assertThat(attributes.getApiPath(), is(equalTo("http://foo/api/v1/catalogs/bar/datasets/dataset1/attributes")));
    }

    @Test
    void constructionWithBuilderCorrectlyPopulatesVarArgs() {
        // Given
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");

        // When
        Attributes attributes = Attributes.builder().varArgs(varArgs).build();

        // Then
        assertThat(attributes.getVarArgs(), is(equalTo(varArgs)));
    }

    @Test
    void constructionWithBuilderCorrectlyPopulatesVarArgUsingKeyValue() {
        // When
        Attributes attributes = Attributes.builder().varArg("key1", "value1").build();

        // Then
        assertThat(attributes.getVarArgs().get("key1"), is(equalTo("value1")));
    }

    @Test
    void updateOperationIsSupported() {
        // Given
        Fusion fusion = Mockito.mock(Fusion.class);

        Attributes attributes = Attributes.builder()
                .catalogIdentifier("foo")
                .datasetIdentifier("bar")
                .fusion(fusion)
                .build();

        when(fusion.getRootURL()).thenReturn("http://foo/api/v1/");
        when(fusion.update("http://foo/api/v1/catalogs/foo/datasets/bar/attributes", attributes))
                .thenReturn("API Call Successful");

        // When & Then
        attributes.update();

        verify(fusion).update(eq("http://foo/api/v1/catalogs/foo/datasets/bar/attributes"), eq(attributes));
    }

    @Test
    void createOperationIsSupported() {
        // Given
        Fusion fusion = Mockito.mock(Fusion.class);

        Attributes attributes = Attributes.builder()
                .catalogIdentifier("foo")
                .datasetIdentifier("bar")
                .fusion(fusion)
                .build();

        when(fusion.getRootURL()).thenReturn("http://foo/api/v1/");
        when(fusion.update("http://foo/api/v1/catalogs/foo/datasets/bar/attributes", attributes))
                .thenReturn("API Call Successful");

        // When & Then
        attributes.update();

        verify(fusion).update(eq("http://foo/api/v1/catalogs/foo/datasets/bar/attributes"), eq(attributes));
    }

    @Test
    void deleteThrowsUnsupportedOperationException() {
        // Given
        Attributes attributes =
                Attributes.builder().fusion(Mockito.mock(Fusion.class)).build();

        // When & Then
        Assertions.assertThrows(UnsupportedOperationException.class, attributes::delete);
    }

    @Test
    public void testRegisteredAttributesReturnedCorrectly() {
        // Given
        Set<String> expected = VarArgsHelper.getFieldNames(new HashSet<>(), CatalogResource.class);
        VarArgsHelper.getFieldNames(expected, Attributes.class);
        expected.addAll(Arrays.asList("@id", "@context", "@base"));

        // When
        Set<String> actual = Attributes.builder().build().getRegisteredAttributes();

        // Then
        assertThat(actual, Matchers.containsInAnyOrder(expected.toArray()));
    }
}
