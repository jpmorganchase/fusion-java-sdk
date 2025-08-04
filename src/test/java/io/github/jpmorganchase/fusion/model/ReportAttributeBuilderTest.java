package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ReportAttributeBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Fusion fusion = Mockito.mock(Fusion.class);
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");

        ReportAttribute r = ReportAttribute.builder()
                .title("title")
                .varArgs(varArgs)
                .fusion(fusion)
                .build();

        assertThat(r.getTitle(), is(equalTo("title")));
        assertThat(r.getVarArgs(), is(equalTo(varArgs)));
        assertThat(r.getFusion(), notNullValue());
    }
}
