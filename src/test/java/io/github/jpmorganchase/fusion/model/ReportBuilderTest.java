package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ReportBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Fusion fusion = Mockito.mock(Fusion.class);
        ReportDetail reportDetail = ReportDetail.builder().tier("The tier").build();
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        ReportObj r = ReportObj.builder()
                .identifier("The identifier")
                .varArgs(varArgs)
                .description("The description")
                .linkedEntity("The entity")
                .title("The title")
                .frequency("The frequency")
                .fusion(fusion)
                .catalogIdentifier("foobar")
                .publisher("J.P. Morgan")
                .tier("The tier")
                .build();

        assertThat(r.getIdentifier(), is(equalTo("The identifier")));
        assertThat(r.getVarArgs(), is(equalTo(varArgs)));
        assertThat(r.getDescription(), is(equalTo("The description")));
        assertThat(r.getLinkedEntity(), is(equalTo("The entity")));
        assertThat(r.getTitle(), is(equalTo("The title")));
        assertThat(r.getFrequency(), is(equalTo("The frequency")));
        assertThat(r.getType(), is(equalTo("Report")));
        assertThat(r.getReport(), is(equalTo(reportDetail)));
        assertThat(r.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(r.getPublisher(), is(equalTo("J.P. Morgan")));
        assertThat(r.getFusion(), notNullValue());
    }

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFieldsWhenSingleVarArgAdded() {
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        ReportObj r = ReportObj.builder().varArg("key1", "value1").build();

        assertThat(r.getVarArgs(), is(equalTo(varArgs)));
    }

    @Test
    void constructionWithBuilderCorrectlyReturnsApiPath() {
        // Given
        Fusion fusion = Mockito.mock(Fusion.class);
        Mockito.when(fusion.getRootURL()).thenReturn("http://foobar/api/v1/");

        // When
        ReportObj r = ReportObj.builder()
                .identifier("The identifier")
                .fusion(fusion)
                .catalogIdentifier("foobar")
                .build();

        // Then
        assertThat(r.getApiPath(), is(equalTo("http://foobar/api/v1/catalogs/foobar/datasets/The identifier")));
    }

    @Test
    void constructionWithBuilderCorrectlyPopulatesForNullReportTier() {
        Fusion fusion = Mockito.mock(Fusion.class);
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        ReportObj d = ReportObj.builder()
                .identifier("The identifier")
                .varArgs(varArgs)
                .description("The description")
                .linkedEntity("The entity")
                .title("The title")
                .frequency("The frequency")
                .tier(null)
                .fusion(fusion)
                .catalogIdentifier("foobar")
                .publisher("J.P. Morgan")
                .build();

        assertThat(d.getType(), is(equalTo("Report")));
        assertThat(d.getReport(), is(equalTo(null)));
        assertThat(d.getTier(), is(equalTo(null)));
    }

    @Test
    void mutationViaToBuilderCorrectlySetsAllFields() {
        Fusion fusion = Mockito.mock(Fusion.class);
        ReportDetail reportDetail = ReportDetail.builder().tier("The tier").build();
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        ReportObj r = ReportObj.builder()
                .identifier("The identifier")
                .varArgs(varArgs)
                .description("The description")
                .linkedEntity("The entity")
                .title("The title")
                .frequency("The frequency")
                .fusion(fusion)
                .catalogIdentifier("foobar")
                .publisher("J.P. Morgan")
                .tier("The tier")
                .build();

        assertThat(r.getIdentifier(), is(equalTo("The identifier")));
        assertThat(r.getVarArgs(), is(equalTo(varArgs)));
        assertThat(r.getDescription(), is(equalTo("The description")));
        assertThat(r.getLinkedEntity(), is(equalTo("The entity")));
        assertThat(r.getTitle(), is(equalTo("The title")));
        assertThat(r.getFrequency(), is(equalTo("The frequency")));
        assertThat(r.getType(), is(equalTo("Report")));
        assertThat(r.getReport(), is(equalTo(reportDetail)));
        assertThat(r.getTier(), is(equalTo("The tier")));
        assertThat(r.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(r.getPublisher(), is(equalTo("J.P. Morgan")));
        assertThat(r.getFusion(), notNullValue());

        ReportObj mr = r.toBuilder().tier("Updated Tier").build();

        assertThat(mr.getIdentifier(), is(equalTo("The identifier")));
        assertThat(mr.getVarArgs(), is(equalTo(varArgs)));
        assertThat(mr.getDescription(), is(equalTo("The description")));
        assertThat(mr.getLinkedEntity(), is(equalTo("The entity")));
        assertThat(mr.getTitle(), is(equalTo("The title")));
        assertThat(mr.getFrequency(), is(equalTo("The frequency")));
        assertThat(mr.getType(), is(equalTo("Report")));
        assertThat(mr.getTier(), is(equalTo("Updated Tier")));
        assertThat(
                mr.getReport(),
                is(equalTo(ReportDetail.builder().tier("Updated Tier").build())));
        assertThat(mr.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(mr.getPublisher(), is(equalTo("J.P. Morgan")));
        assertThat(mr.getFusion(), notNullValue());
    }

    @Test
    public void testRegisteredAttributesReturnedCorrectly() {
        // Given
        Set<String> expected = VarArgsHelper.getFieldNames(new HashSet<>(), CatalogResource.class);
        VarArgsHelper.getFieldNames(expected, Dataset.class);
        VarArgsHelper.getFieldNames(expected, ReportObj.class);
        expected.addAll(Arrays.asList("@id", "@context", "@base"));

        // When
        Set<String> actual = ReportObj.builder().build().getRegisteredAttributes();

        // Then
        assertThat(actual, Matchers.containsInAnyOrder(expected.toArray()));
    }
}
