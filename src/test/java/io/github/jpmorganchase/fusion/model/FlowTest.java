package io.github.jpmorganchase.fusion.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class FlowTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Flow flow = Flow.builder().flowDirection("The direction").build();
        assertThat(flow.getFlowDirection(), is(equalTo("The direction")));
    }
}
