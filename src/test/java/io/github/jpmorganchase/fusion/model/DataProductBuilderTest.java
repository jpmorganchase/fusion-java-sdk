package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DataProductBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Fusion fusion = Mockito.mock(Fusion.class);
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        DataProduct p = DataProduct.builder()
                .identifier("The identifier")
                .varArgs(varArgs)
                .description("The description")
                .linkedEntity("The entity")
                .title("The title")
                .status("The status")
                .catalogIdentifier("foobar")
                .fusion(fusion)
                .build();

        assertThat(p.getIdentifier(), is(equalTo("The identifier")));
        assertThat(p.getVarArgs(), is(equalTo(varArgs)));
        assertThat(p.getDescription(), is(equalTo("The description")));
        assertThat(p.getLinkedEntity(), is(equalTo("The entity")));
        assertThat(p.getTitle(), is(equalTo("The title")));
        assertThat(p.getStatus(), is(equalTo("The status")));
        assertThat(p.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(p.getFusion(), is(equalTo(fusion)));
    }

    @Test
    public void testRegisteredAttributesReturnedCorrectly() {
        // Given
        Set<String> expected = VarArgsHelper.getFieldNames(new HashSet<>(), CatalogResource.class);
        VarArgsHelper.getFieldNames(expected, DataProduct.class);
        expected.addAll(Arrays.asList("@id", "@context", "@base"));

        // When
        Set<String> actual = DataProduct.builder().build().getRegisteredAttributes();

        // Then
        assertThat(actual, Matchers.containsInAnyOrder(expected.toArray()));
    }
}
