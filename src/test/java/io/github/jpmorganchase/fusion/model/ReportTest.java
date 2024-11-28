package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class ReportTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Report report = Report.builder().tier("The tier").build();
        assertThat(report.getTier(), is(equalTo("The tier")));
    }
}
