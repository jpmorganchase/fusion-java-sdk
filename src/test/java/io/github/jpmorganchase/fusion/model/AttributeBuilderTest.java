package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.api.APIManager;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AttributeBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        APIManager apiManager = Mockito.mock(APIManager.class);
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
                .rootUrl("http://foobar/api/v1/")
                .catalogIdentifier("foobar")
                .apiManager(apiManager)
                .build();

        assertThat(a.getIdentifier(), is(equalTo("The identifier")));
        assertThat(a.getVarArgs(), is(equalTo(varArgs)));
        assertThat(a.isKey(), is(true));
        assertThat(a.getDataType(), is(equalTo("The type")));
        assertThat(a.getIndex(), is(100L));
        assertThat(a.getDescription(), is(equalTo("The description")));
        assertThat(a.getTitle(), is(equalTo("The title")));
        assertThat(a.getRootUrl(), is(equalTo("http://foobar/api/v1/")));
        assertThat(a.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(a.getApiManager(), is(equalTo(apiManager)));
    }
}
