package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.api.APIManager;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DistributionBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        APIManager apiManager = Mockito.mock(APIManager.class);
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        Distribution d = Distribution.builder()
                .identifier("The identifier")
                .varArgs(varArgs)
                .description("The description")
                .linkedEntity("The entity")
                .title("The title")
                .fileExtension("The extension")
                .mediaType("The media type")
                .rootUrl("http://foobar/api/v1/")
                .catalogIdentifier("foobar")
                .apiManager(apiManager)
                .build();

        assertThat(d.getIdentifier(), is(equalTo("The identifier")));
        assertThat(d.getVarArgs(), is(equalTo(varArgs)));
        assertThat(d.getDescription(), is(equalTo("The description")));
        assertThat(d.getLinkedEntity(), is(equalTo("The entity")));
        assertThat(d.getTitle(), is(equalTo("The title")));
        assertThat(d.getFileExtension(), is(equalTo("The extension")));
        assertThat(d.getMediaType(), is(equalTo("The media type")));
        assertThat(d.getRootUrl(), is(equalTo("http://foobar/api/v1/")));
        assertThat(d.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(d.getApiManager(), is(equalTo(apiManager)));
    }
}
