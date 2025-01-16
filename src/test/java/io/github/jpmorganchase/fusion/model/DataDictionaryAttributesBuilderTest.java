package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DataDictionaryAttributesBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        // Given
        Fusion fusion = Mockito.mock(Fusion.class);
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");

        List<DataDictionaryAttribute> attributes = new ArrayList<>();
        attributes.add(DataDictionaryAttribute.builder().identifier("attr1").build());

        // When
        DataDictionaryAttributes dataDictionaryAttributes = DataDictionaryAttributes.builder()
                .identifier("attributes")
                .catalogIdentifier("foobar")
                .fusion(fusion)
                .varArgs(varArgs)
                .dataDictionaryAttributes(attributes)
                .build();

        // Then
        assertThat(dataDictionaryAttributes.getIdentifier(), is(equalTo("attributes")));
        assertThat(dataDictionaryAttributes.getVarArgs(), is(equalTo(varArgs)));
        assertThat(dataDictionaryAttributes.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(dataDictionaryAttributes.getFusion(), is(equalTo(fusion)));
        assertThat(dataDictionaryAttributes.getDataDictionaryAttributes(), is(equalTo(attributes)));
    }

    @Test
    void constructionWithBuilderCorrectlyAddsSingleAttribute() {
        // Given
        DataDictionaryAttribute attribute =
                DataDictionaryAttribute.builder().identifier("attr1").build();

        // When
        DataDictionaryAttributes dataDictionaryAttributes = DataDictionaryAttributes.builder()
                .dataDictionaryAttribute(attribute)
                .build();

        // Then
        assertThat(dataDictionaryAttributes.getDataDictionaryAttributes(), hasSize(1));
        assertThat(dataDictionaryAttributes.getDataDictionaryAttributes(), contains(attribute));
    }

    @Test
    void constructionWithBuilderCorrectlyAddsMultipleAttributes() {
        // Given
        List<DataDictionaryAttribute> attributes = new ArrayList<>();
        attributes.add(DataDictionaryAttribute.builder().identifier("attr1").build());
        attributes.add(DataDictionaryAttribute.builder().identifier("attr2").build());

        // When
        DataDictionaryAttributes dataDictionaryAttributes = DataDictionaryAttributes.builder()
                .dataDictionaryAttributes(attributes)
                .build();

        // Then
        assertThat(dataDictionaryAttributes.getDataDictionaryAttributes(), hasSize(2));
        assertThat(dataDictionaryAttributes.getDataDictionaryAttributes(), contains(attributes.toArray()));
    }

    @Test
    void constructionWithBuilderCorrectlyReturnsApiPath() {
        // Given
        Fusion fusion = Mockito.mock(Fusion.class);
        Mockito.when(fusion.getRootURL()).thenReturn("http://foo/api/v1/");

        // When
        DataDictionaryAttributes dataDictionaryAttributes = DataDictionaryAttributes.builder()
                .fusion(fusion)
                .catalogIdentifier("bar")
                .build();

        // Then
        assertThat(dataDictionaryAttributes.getApiPath(), is(equalTo("http://foo/api/v1/catalogs/bar/attributes")));
    }

    @Test
    void constructionWithBuilderCorrectlyPopulatesVarArgs() {
        // Given
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");

        // When
        DataDictionaryAttributes dataDictionaryAttributes =
                DataDictionaryAttributes.builder().varArgs(varArgs).build();

        // Then
        assertThat(dataDictionaryAttributes.getVarArgs(), is(equalTo(varArgs)));
    }

    @Test
    void constructionWithBuilderCorrectlyPopulatesVarArgUsingKeyValue() {
        // When
        DataDictionaryAttributes dataDictionaryAttributes =
                DataDictionaryAttributes.builder().varArg("key1", "value1").build();

        // Then
        assertThat(dataDictionaryAttributes.getVarArgs().get("key1"), is(equalTo("value1")));
    }

    @Test
    void testCreateIsSupportedOperation() {
        // Given
        DataDictionaryAttributes a = DataDictionaryAttributes.builder()
                .fusion(Mockito.mock(Fusion.class))
                .build();

        // When & Then
        a.create();
    }

    @Test
    void testUpdateIsUnsupportedOperation() {
        // Given
        DataDictionaryAttributes a = DataDictionaryAttributes.builder()
                .fusion(Mockito.mock(Fusion.class))
                .build();

        // When & Then
        Assertions.assertThrows(UnsupportedOperationException.class, a::update);
    }

    @Test
    void testDeleteIsUnsupportedOperation() {
        DataDictionaryAttributes a = DataDictionaryAttributes.builder()
                .fusion(Mockito.mock(Fusion.class))
                .build();

        // When & Then
        Assertions.assertThrows(UnsupportedOperationException.class, a::update);
    }

    @Test
    public void testRegisteredAttributesReturnedCorrectly() {
        // Given
        Set<String> expected = VarArgsHelper.getFieldNames(CatalogResource.class);
        expected.addAll(VarArgsHelper.getFieldNames(DataDictionaryAttributes.class));
        expected.addAll(Arrays.asList("@id", "@context", "@base"));

        // When
        Set<String> actual = DataDictionaryAttributes.builder().build().getRegisteredAttributes();

        // Then
        assertThat(actual, Matchers.containsInAnyOrder(expected.toArray()));
    }
}
