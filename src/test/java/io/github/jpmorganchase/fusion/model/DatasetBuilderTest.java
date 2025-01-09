package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DatasetBuilderTest {

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFields() {
        Fusion fusion = Mockito.mock(Fusion.class);
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        Dataset d = Dataset.builder()
                .identifier("The identifier")
                .varArgs(varArgs)
                .description("The description")
                .linkedEntity("The entity")
                .title("The title")
                .frequency("The frequency")
                .fusion(fusion)
                .catalogIdentifier("foobar")
                .publisher("J.P. Morgan")
                .build();

        assertThat(d.getIdentifier(), is(equalTo("The identifier")));
        assertThat(d.getVarArgs(), is(equalTo(varArgs)));
        assertThat(d.getDescription(), is(equalTo("The description")));
        assertThat(d.getLinkedEntity(), is(equalTo("The entity")));
        assertThat(d.getTitle(), is(equalTo("The title")));
        assertThat(d.getFrequency(), is(equalTo("The frequency")));
        assertThat(d.getType(), is(equalTo(null)));
        assertThat(d.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(d.getPublisher(), is(equalTo("J.P. Morgan")));
        assertThat(d.getFusion(), notNullValue());
    }

    @Test
    void constructionWithBuilderCorrectlyPopulatesForTypeFlowInput() {
        Fusion fusion = Mockito.mock(Fusion.class);
        Application producerApplicationId =
                Application.builder().sealId("123456").build();
        Application consumerApplicationId =
                Application.builder().sealId("456789").build();
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        Dataset d = Dataset.builder()
                .identifier("The identifier")
                .varArgs(varArgs)
                .description("The description")
                .linkedEntity("The entity")
                .title("The title")
                .frequency("The frequency")
                .flow(Flow.builder()
                        .flowDirection("Input")
                        .producerApplicationId(producerApplicationId)
                        .consumerApplicationId(consumerApplicationId)
                        .build())
                .publisher("J.P. Morgan")
                .catalogIdentifier("foobar")
                .fusion(fusion)
                .build();

        assertThat(d.getIdentifier(), is(equalTo("The identifier")));
        assertThat(d.getVarArgs(), is(equalTo(varArgs)));
        assertThat(d.getDescription(), is(equalTo("The description")));
        assertThat(d.getLinkedEntity(), is(equalTo("The entity")));
        assertThat(d.getTitle(), is(equalTo("The title")));
        assertThat(d.getFrequency(), is(equalTo("The frequency")));
        assertThat(d.getType(), is(equalTo("Flow")));
        assertThat(d.getFlowDetails().getFlowDirection(), is(equalTo("Input")));
        assertThat(d.getProducerApplicationId(), is(equalTo(producerApplicationId)));
        assertThat(d.getConsumerApplicationId().get(0), is(equalTo(consumerApplicationId)));
        assertThat(d.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(d.getPublisher(), is(equalTo("J.P. Morgan")));
        assertThat(d.getFusion(), is(equalTo(fusion)));
    }

    @Test
    void constructionWithBuilderCorrectlyPopulatesForTypeFlowOutput() {
        Fusion fusion = Mockito.mock(Fusion.class);
        Application producerApplicationId =
                Application.builder().sealId("123456").build();
        Application consumerApplicationId =
                Application.builder().sealId("456789").build();
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        Dataset d = Dataset.builder()
                .identifier("The identifier")
                .varArgs(varArgs)
                .description("The description")
                .linkedEntity("The entity")
                .title("The title")
                .frequency("The frequency")
                .flow(Flow.builder()
                        .flowDirection("Output")
                        .producerApplicationId(producerApplicationId)
                        .consumerApplicationId(consumerApplicationId)
                        .build())
                .publisher("J.P. Morgan")
                .catalogIdentifier("foobar")
                .fusion(fusion)
                .build();

        assertThat(d.getIdentifier(), is(equalTo("The identifier")));
        assertThat(d.getVarArgs(), is(equalTo(varArgs)));
        assertThat(d.getDescription(), is(equalTo("The description")));
        assertThat(d.getLinkedEntity(), is(equalTo("The entity")));
        assertThat(d.getTitle(), is(equalTo("The title")));
        assertThat(d.getFrequency(), is(equalTo("The frequency")));
        assertThat(d.getType(), is(equalTo("Flow")));
        assertThat(d.getFlowDetails().getFlowDirection(), is(equalTo("Output")));
        assertThat(d.getProducerApplicationId(), is(equalTo(producerApplicationId)));
        assertThat(d.getConsumerApplicationId().get(0), is(equalTo(consumerApplicationId)));
        assertThat(d.getCatalogIdentifier(), is(equalTo("foobar")));
        assertThat(d.getPublisher(), is(equalTo("J.P. Morgan")));
        assertThat(d.getFusion(), is(equalTo(fusion)));
    }

    @Test
    void constructionWithBuilderCorrectlyPopulatesAllFieldsWhenSingleVarArgAdded() {
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("key1", "value1");
        Dataset d = Dataset.builder().varArg("key1", "value1").build();

        assertThat(d.getVarArgs(), is(equalTo(varArgs)));
    }

    @Test
    void constructionWithBuilderCorrectlyReturnsApiPath() {
        // Given
        Fusion fusion = Mockito.mock(Fusion.class);
        Mockito.when(fusion.getRootURL()).thenReturn("http://foobar/api/v1/");

        // When
        Dataset d = Dataset.builder()
                .identifier("The identifier")
                .fusion(fusion)
                .catalogIdentifier("foobar")
                .build();

        // Then
        assertThat(d.getApiPath(), is(equalTo("http://foobar/api/v1/catalogs/foobar/datasets/The identifier")));
    }

    @Test
    public void testRegisteredAttributesReturnedCorrectly() {
        // Given
        Set<String> expected = VarArgsHelper.getFieldNames(new HashSet<>(), CatalogResource.class);
        VarArgsHelper.getFieldNames(expected, Dataset.class);
        expected.addAll(Arrays.asList("@id", "@context", "@base"));

        // When
        Set<String> actual = Dataset.builder().build().getRegisteredAttributes();

        // Then
        assertThat(actual, Matchers.containsInAnyOrder(expected.toArray()));
    }
}
