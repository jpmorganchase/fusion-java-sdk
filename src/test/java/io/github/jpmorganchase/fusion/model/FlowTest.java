package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class FlowTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Flow flow = Flow.builder().flowDirection("The direction").build();
        assertThat(flow.getFlowDirection(), is(equalTo("The direction")));
    }
}
