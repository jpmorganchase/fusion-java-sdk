package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DataDictionaryAttributeBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Fusion fusion = Mockito.mock(Fusion.class);
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");

        DataDictionaryAttribute a = DataDictionaryAttribute.builder()
                .identifier("The identifier")
                .description("The description")
                .title("The title")
                .catalogIdentifier("foobar")
                .fusion(fusion)
                .dataType("String")
                .publisher("Bloomberg")
                .varArgs(varArgs)
                .build();

        assertThat(a.getIdentifier(), is(equalTo("The identifier")));
        assertThat(a.getVarArgs(), is(equalTo(varArgs)));
        assertThat(a.getDescription(), is(equalTo("The description")));
        assertThat(a.getTitle(), is(equalTo("The title")));
        assertThat(a.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(a.getFusion(), is(equalTo(fusion)));
        assertThat(a.getDataType(), is(equalTo("String")));
        assertThat(a.getPublisher(), is(equalTo("Bloomberg")));
    }

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFieldsWhenSingleVarArgAdded() {
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        DataDictionaryAttribute a =
                DataDictionaryAttribute.builder().varArg("key1", "value1").build();

        assertThat(a.getVarArgs(), is(equalTo(varArgs)));
    }

    @Test
    void constructionWithBuilderCorrectlyReturnsApiPath() {
        // Given
        Fusion fusion = Mockito.mock(Fusion.class);
        Mockito.when(fusion.getRootURL()).thenReturn("http://foo/api/v1/");

        // When
        DataDictionaryAttribute a = DataDictionaryAttribute.builder()
                .identifier("The identifier")
                .fusion(fusion)
                .catalogIdentifier("bar")
                .build();

        // Then
        Assertions.assertThrows(UnsupportedOperationException.class, a::getApiPath);
    }

    @Test
    void testCreateIsUnsupportedOperation() {
        // Given
        DataDictionaryAttribute a =
                DataDictionaryAttribute.builder().identifier("The identifier").build();

        // When & Then
        Assertions.assertThrows(UnsupportedOperationException.class, a::create);
    }

    @Test
    void testUpdateIsUnsupportedOperation() {
        // Given
        DataDictionaryAttribute a =
                DataDictionaryAttribute.builder().identifier("The identifier").build();

        // When & Then
        Assertions.assertThrows(UnsupportedOperationException.class, a::update);
    }

    @Test
    void testDeleteIsUnsupportedOperation() {
        // Given
        DataDictionaryAttribute a =
                DataDictionaryAttribute.builder().identifier("The identifier").build();

        // When & Then
        Assertions.assertThrows(UnsupportedOperationException.class, a::delete);
    }

    @Test
    public void testRegisteredAttributesReturnedCorrectly() {
        // Given
        Set<String> expected = VarArgsHelper.getFieldNames(new HashSet<>(), CatalogResource.class);
        VarArgsHelper.getFieldNames(expected, DataDictionaryAttribute.class);
        expected.addAll(Arrays.asList("@id", "@context", "@base"));

        // When
        Set<String> actual = DataDictionaryAttribute.builder().build().getRegisteredAttributes();

        // Then
        assertThat(actual, Matchers.containsInAnyOrder(expected.toArray()));
    }
}
