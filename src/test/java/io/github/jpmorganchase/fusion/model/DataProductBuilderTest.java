package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.api.APIManager;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DataProductBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        APIManager apiManager = Mockito.mock(APIManager.class);
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        DataProduct p = DataProduct.builder()
                .identifier("The identifier")
                .varArgs(varArgs)
                .description("The description")
                .linkedEntity("The entity")
                .title("The title")
                .status("The status")
                .rootUrl("http://foobar/api/v1/")
                .catalogIdentifier("foobar")
                .apiManager(apiManager)
                .build();

        assertThat(p.getIdentifier(), is(equalTo("The identifier")));
        assertThat(p.getVarArgs(), is(equalTo(varArgs)));
        assertThat(p.getDescription(), is(equalTo("The description")));
        assertThat(p.getLinkedEntity(), is(equalTo("The entity")));
        assertThat(p.getTitle(), is(equalTo("The title")));
        assertThat(p.getStatus(), is(equalTo("The status")));
        assertThat(p.getRootUrl(), is(equalTo("http://foobar/api/v1/")));
        assertThat(p.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(p.getApiManager(), is(equalTo(apiManager)));
    }
}
