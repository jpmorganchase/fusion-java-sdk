package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AttributeLineagesTest {

    @Test
    void testBuilderWithSingleAttributeLineage() {
        // Given
        Fusion fusion = givenFusionMock();

        AttributeReference source = AttributeReference.builder()
                .attribute("AR0001")
                .catalog("c1")
                .applicationId(Application.builder().sealId("111111").build())
                .build();

        AttributeReference target = AttributeReference.builder()
                .attribute("AR0002")
                .catalog("c2")
                .applicationId(Application.builder().sealId("222222").build())
                .build();

        AttributeLineage lineage =
                AttributeLineage.builder().source(source).target(target).build();

        // When
        AttributeLineages attributeLineages = AttributeLineages.builder()
                .identifier("id1")
                .catalogIdentifier("c1")
                .fusion(fusion)
                .attributeLineage(lineage)
                .build();

        // Then
        assertThat(attributeLineages.getIdentifier(), is("id1"));
        assertThat(attributeLineages.getCatalogIdentifier(), is("c1"));
        assertThat(attributeLineages.getAttributeLineages(), hasSize(1));
        assertThat(attributeLineages.getAttributeLineages(), contains(lineage));
    }

    @Test
    void testBuilderWithMultipleAttributeLineages() {
        // Given
        Fusion fusion = givenFusionMock();

        AttributeReference source1 = AttributeReference.builder()
                .attribute("AR0001")
                .catalog("c1")
                .applicationId(Application.builder().sealId("111111").build())
                .build();

        AttributeReference target1 = AttributeReference.builder()
                .attribute("AR0002")
                .catalog("c2")
                .applicationId(Application.builder().sealId("222222").build())
                .build();

        AttributeLineage lineage1 =
                AttributeLineage.builder().source(source1).target(target1).build();

        AttributeReference source2 = AttributeReference.builder()
                .attribute("AR0003")
                .catalog("c3")
                .applicationId(Application.builder().sealId("333333").build())
                .build();

        AttributeReference target2 = AttributeReference.builder()
                .attribute("AR0004")
                .catalog("c4")
                .applicationId(Application.builder().sealId("444444").build())
                .build();

        AttributeLineage lineage2 =
                AttributeLineage.builder().source(source2).target(target2).build();

        Set<AttributeLineage> lineageSet = new HashSet<>();
        lineageSet.add(lineage1);
        lineageSet.add(lineage2);

        // When
        AttributeLineages attributeLineages = AttributeLineages.builder()
                .identifier("id2")
                .catalogIdentifier("c3")
                .fusion(fusion)
                .attributeLineages(lineageSet)
                .build();

        // Then
        assertThat(attributeLineages.getIdentifier(), is("id2"));
        assertThat(attributeLineages.getCatalogIdentifier(), is("c3"));
        assertThat(attributeLineages.getAttributeLineages(), hasSize(2));
        assertThat(attributeLineages.getAttributeLineages(), containsInAnyOrder(lineage1, lineage2));
    }

    @Test
    void testBuilderWithEmptyAttributeLineages() {
        // Given
        Fusion fusion = givenFusionMock();

        // When
        AttributeLineages attributeLineages = AttributeLineages.builder()
                .identifier("id3")
                .catalogIdentifier("c3")
                .fusion(fusion)
                .build();

        // Then
        assertThat(attributeLineages.getIdentifier(), is("id3"));
        assertThat(attributeLineages.getCatalogIdentifier(), is("c3"));
        assertThat(attributeLineages.getAttributeLineages(), is(nullValue()));
    }

    @Test
    void testApiPathGeneration() {
        // Given
        Fusion fusion = givenFusionMock();

        // When
        AttributeLineages attributeLineages = AttributeLineages.builder()
                .catalogIdentifier("c4")
                .fusion(fusion)
                .build();

        // Then
        assertThat(attributeLineages.getApiPath(), is("https://api.example.com/catalogs/c4/attributes/lineage"));
    }

    @Test
    void testVarArgsUsage() {
        // When
        AttributeLineages attributeLineages = AttributeLineages.builder()
                .identifier("identifier-4")
                .varArg("key1", "value1")
                .varArg("key2", "value2")
                .build();

        // Then
        Map<String, Object> expectedVarArgs = new HashMap<>();
        expectedVarArgs.put("key1", "value1");
        expectedVarArgs.put("key2", "value2");

        assertThat(attributeLineages.getVarArgs(), is(notNullValue()));
        assertThat(attributeLineages.getVarArgs(), is(expectedVarArgs));
    }

    @Test
    public void testRegisteredAttributesReturnedCorrectly() {
        // Given
        Set<String> expected = VarArgsHelper.getFieldNames(CatalogResource.class);
        expected.addAll(VarArgsHelper.getFieldNames(AttributeLineages.class));
        expected.addAll(Arrays.asList("@id", "@context", "@base"));

        // When
        Set<String> actual = AttributeLineages.builder().build().getRegisteredAttributes();

        // Then
        assertThat(actual, Matchers.containsInAnyOrder(expected.toArray()));
    }

    private Fusion givenFusionMock() {
        Fusion fusion = Mockito.mock(Fusion.class);
        Mockito.when(fusion.getRootURL()).thenReturn("https://api.example.com/");
        return fusion;
    }
}
