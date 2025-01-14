package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AttributeBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Fusion fusion = Mockito.mock(Fusion.class);
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        Attribute a = Attribute.builder()
                .identifier("The identifier")
                .varArgs(varArgs)
                .key(true)
                .dataType("The type")
                .index(100)
                .description("The description")
                .title("The title")
                .datasetIdentifier("foo")
                .catalogIdentifier("foobar")
                .fusion(fusion)
                .isCriticalDataElement(true)
                .build();

        assertThat(a.getIdentifier(), is(equalTo("The identifier")));
        assertThat(a.getVarArgs(), is(equalTo(varArgs)));
        assertThat(a.isKey(), is(true));
        assertThat(a.getDataType(), is(equalTo("The type")));
        assertThat(a.getIndex(), is(100L));
        assertThat(a.getDescription(), is(equalTo("The description")));
        assertThat(a.getTitle(), is(equalTo("The title")));
        assertThat(a.getDatasetIdentifier(), is(equalTo("foo")));
        assertThat(a.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(a.getFusion(), is(equalTo(fusion)));
        assertThat(a.isCriticalDataElement(), is(equalTo(true)));
    }

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFieldsWhenSingleVarArgAdded() {
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        Attribute a = Attribute.builder().varArg("key1", "value1").build();

        assertThat(a.getVarArgs(), is(equalTo(varArgs)));
    }

    @Test
    void constructionWithBuilderCorrectlyReturnsApiPath() {
        // Given
        Fusion fusion = Mockito.mock(Fusion.class);
        Mockito.when(fusion.getRootURL()).thenReturn("http://foo/api/v1/");

        // When
        Attribute a = Attribute.builder()
                .identifier("The identifier")
                .fusion(fusion)
                .catalogIdentifier("bar")
                .datasetIdentifier("foobar")
                .build();

        // Then
        assertThat(
                a.getApiPath(),
                is(equalTo("http://foo/api/v1/catalogs/bar/datasets/foobar/attributes/The identifier")));
    }

    @Test
    public void testRegisteredAttributesReturnedCorrectly() {
        // Given
        Set<String> expected = VarArgsHelper.getFieldNames(CatalogResource.class);
        expected.addAll(VarArgsHelper.getFieldNames(Attribute.class));
        expected.addAll(Arrays.asList("@id", "@context", "@base"));

        // When
        Set<String> actual = Attribute.builder().build().getRegisteredAttributes();

        // Then
        assertThat(actual, Matchers.containsInAnyOrder(expected.toArray()));
    }
}
