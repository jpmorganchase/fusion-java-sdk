package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class ReportDetailTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        ReportDetail reportDetail = ReportDetail.builder().tier("The tier").build();
        assertThat(reportDetail.getTier(), is(equalTo("The tier")));
    }
}
