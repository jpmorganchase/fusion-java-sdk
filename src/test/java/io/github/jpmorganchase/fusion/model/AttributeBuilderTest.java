package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class AttributeBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
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
                .build();

        assertThat(a.getIdentifier(), is(equalTo("The identifier")));
        assertThat(a.getVarArgs(), is(equalTo(varArgs)));
        assertThat(a.isKey(), is(true));
        assertThat(a.getDataType(), is(equalTo("The type")));
        assertThat(a.getIndex(), is(100L));
        assertThat(a.getDescription(), is(equalTo("The description")));
        assertThat(a.getTitle(), is(equalTo("The title")));
    }
}
