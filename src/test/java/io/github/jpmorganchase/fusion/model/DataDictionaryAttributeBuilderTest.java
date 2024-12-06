package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.HashMap;
import java.util.Map;
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
                .varArgs(varArgs)
                .build();

        assertThat(a.getIdentifier(), is(equalTo("The identifier")));
        assertThat(a.getVarArgs(), is(equalTo(varArgs)));
        assertThat(a.getDescription(), is(equalTo("The description")));
        assertThat(a.getTitle(), is(equalTo("The title")));
        assertThat(a.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(a.getFusion(), is(equalTo(fusion)));
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
        assertThat(a.getApiPath(), is(equalTo("http://foo/api/v1/catalogs/bar/attributes/The identifier")));
    }
}
