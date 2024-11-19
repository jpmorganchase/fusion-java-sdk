package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DatasetSeriesBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        DatasetSeries d = DatasetSeries.builder()
                .identifier("The identifier")
                .varArgs(varArgs)
                .fromDate(LocalDate.of(2023, 1, 2))
                .toDate(LocalDate.of(2023, 1, 3))
                .createdDate(LocalDate.of(2023, 1, 4))
                .linkedEntity("The entity")
                .build();

        assertThat(d.getIdentifier(), is(equalTo("The identifier")));
        assertThat(d.getVarArgs(), is(equalTo(varArgs)));
        assertThat(d.getFromDate(), is(equalTo(LocalDate.of(2023, 1, 2))));
        assertThat(d.getToDate(), is(equalTo(LocalDate.of(2023, 1, 3))));
        assertThat(d.getCreatedDate(), is(equalTo(LocalDate.of(2023, 1, 4))));
        assertThat(d.getLinkedEntity(), is(equalTo("The entity")));
    }
}
