package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CatalogBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Fusion fusion = Mockito.mock(Fusion.class);
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        Catalog c = Catalog.builder()
                .identifier("The identifier")
                .varArgs(varArgs)
                .description("The description")
                .linkedEntity("The entity")
                .title("The title")
                .catalogIdentifier("foobar")
                .isInternal(Boolean.FALSE)
                .fusion(fusion)
                .build();

        assertThat(c.getIdentifier(), is(equalTo("The identifier")));
        assertThat(c.getVarArgs(), is(equalTo(varArgs)));
        assertThat(c.getDescription(), is(equalTo("The description")));
        assertThat(c.getLinkedEntity(), is(equalTo("The entity")));
        assertThat(c.getTitle(), is(equalTo("The title")));
        assertThat(c.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(c.getIsInternal(), is(equalTo(Boolean.FALSE)));
        assertThat(c.getFusion(), is(equalTo(fusion)));
    }
}
