package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.api.APIManager;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DataDictionaryAttributeBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        APIManager apiManager = Mockito.mock(APIManager.class);
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");

        DataDictionaryAttribute a = DataDictionaryAttribute.builder()
                .identifier("The identifier")
                .description("The description")
                .title("The title")
                .rootUrl("http://foobar/api/v1/")
                .catalogIdentifier("foobar")
                .apiManager(apiManager)
                .varArgs(varArgs)
                .build();

        assertThat(a.getIdentifier(), is(equalTo("The identifier")));
        assertThat(a.getVarArgs(), is(equalTo(varArgs)));
        assertThat(a.getDescription(), is(equalTo("The description")));
        assertThat(a.getTitle(), is(equalTo("The title")));
        assertThat(a.getRootUrl(), is(equalTo("http://foobar/api/v1/")));
        assertThat(a.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(a.getApiManager(), is(equalTo(apiManager)));
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

        DataDictionaryAttribute a = DataDictionaryAttribute.builder()
                .identifier("The identifier")
                .rootUrl("http://foobar/api/v1/")
                .catalogIdentifier("foobar")
                .build();

        assertThat(a.getApiPath(), is(equalTo("http://foobar/api/v1/catalogs/foobar/attributes/The identifier")));
    }
}
