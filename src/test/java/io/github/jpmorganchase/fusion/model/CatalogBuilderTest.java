package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class CatalogBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        Catalog c = Catalog.builder()
                .identifier("The identifier")
                .varArgs(varArgs)
                .description("The description")
                .linkedEntity("The entity")
                .title("The title")
                .build();

        assertThat(c.getIdentifier(), is(equalTo("The identifier")));
        assertThat(c.getVarArgs(), is(equalTo(varArgs)));
        assertThat(c.getDescription(), is(equalTo("The description")));
        assertThat(c.getLinkedEntity(), is(equalTo("The entity")));
        assertThat(c.getTitle(), is(equalTo("The title")));
    }
}
