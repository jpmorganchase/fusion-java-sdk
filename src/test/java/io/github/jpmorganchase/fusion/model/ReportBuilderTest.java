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

public class ReportBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Fusion fusion = Mockito.mock(Fusion.class);
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        DataNodeId dataNodeId = new DataNodeId("id", "name", "type");
        Domain domain = new Domain("id", "name");

        Report r = Report.builder()
                .title("title")
                .description("description")
                .frequency("frequency")
                .category("category")
                .subCategory("subCategory")
                .regulatoryRelated(true)
                .domain(domain)
                .dataNodeId(dataNodeId)
                .varArgs(varArgs)
                .fusion(fusion)
                .build();

        assertThat(r.getTitle(), is(equalTo("title")));
        assertThat(r.getDescription(), is(equalTo("description")));
        assertThat(r.getFrequency(), is(equalTo("frequency")));
        assertThat(r.getCategory(), is(equalTo("category")));
        assertThat(r.getSubCategory(), is(equalTo("subCategory")));
        assertThat(r.isRegulatoryRelated(), is(equalTo(true)));
        assertThat(r.getDomain(), is(equalTo(domain)));
        assertThat(r.getDataNodeId(), is(equalTo(dataNodeId)));
        assertThat(r.getVarArgs(), is(equalTo(varArgs)));
        assertThat(r.getFusion(), notNullValue());
    }
}
