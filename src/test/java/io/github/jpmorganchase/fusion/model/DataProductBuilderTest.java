package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DataProductBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        DataProduct p = DataProduct.builder()
                .identifier("The identifier")
                .varArgs(varArgs)
                .description("The description")
                .linkedEntity("The entity")
                .title("The title")
                .status("The status")
                .build();

        assertThat(p.getIdentifier(), is(equalTo("The identifier")));
        assertThat(p.getVarArgs(), is(equalTo(varArgs)));
        assertThat(p.getDescription(), is(equalTo("The description")));
        assertThat(p.getLinkedEntity(), is(equalTo("The entity")));
        assertThat(p.getTitle(), is(equalTo("The title")));
        assertThat(p.getStatus(), is(equalTo("The status")));
    }
}
