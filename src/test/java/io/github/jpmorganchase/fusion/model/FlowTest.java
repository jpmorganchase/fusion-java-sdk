package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class FlowTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Flow flow = Flow.builder()
                .flowDirection("The direction")
                .producerApplicationId(Application.builder().sealId("123456").build())
                .consumerApplicationId(Application.builder().sealId("456789").build())
                .consumerApplicationId(Application.builder().sealId("901234").build())
                .build();
        assertThat(flow.getFlowDirection(), is(equalTo("The direction")));
        assertThat(flow.getProducerApplicationId().getId(), is(equalTo("123456")));
        assertThat(flow.getConsumerApplicationId().get(0).getId(), is(equalTo("456789")));
        assertThat(flow.getConsumerApplicationId().get(1).getId(), is(equalTo("901234")));
    }
}
