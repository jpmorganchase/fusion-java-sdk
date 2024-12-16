package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AttributeLineageTest {

    @Test
    void testBuilderWithSingleTarget() {
        // Given
        AttributeReference source = AttributeReference.builder()
                .attribute("AR0001")
                .catalog("c1")
                .applicationId(Application.builder().sealId("111111").build())
                .build();

        AttributeReference targetReference = AttributeReference.builder()
                .attribute("AR0002")
                .catalog("c2")
                .applicationId(Application.builder().sealId("222222").build())
                .build();

        // When
        AttributeLineage lineage = AttributeLineage.builder()
                .source(source)
                .target(targetReference)
                .build();

        // Then
        assertThat(lineage.getSource(), is(source));
        assertThat(lineage.getTarget(), is(notNullValue()));
        assertThat(lineage.getTarget(), hasSize(1));
        assertThat(lineage.getTarget(), contains(targetReference));
    }

    @Test
    void testBuilderWithMultipleTargets() {
        // Given
        AttributeReference source = AttributeReference.builder()
                .attribute("AR0001")
                .catalog("c1")
                .applicationId(Application.builder().sealId("111111").build())
                .build();

        AttributeReference target1 = AttributeReference.builder()
                .attribute("AR0002")
                .catalog("c2")
                .applicationId(Application.builder().sealId("222222").build())
                .build();

        AttributeReference target2 = AttributeReference.builder()
                .attribute("AR0003")
                .catalog("c3")
                .applicationId(Application.builder().sealId("333333").build())
                .build();

        Set<AttributeReference> targetReferences = new HashSet<>();
        targetReferences.add(target1);
        targetReferences.add(target2);

        // When
        AttributeLineage lineage = AttributeLineage.builder()
                .source(source)
                .target(targetReferences)
                .build();

        // Then
        assertThat(lineage.getSource(), is(source));
        assertThat(lineage.getTarget(), is(notNullValue()));
        assertThat(lineage.getTarget(), hasSize(2));
        assertThat(lineage.getTarget(), containsInAnyOrder(target1, target2));
    }

    @Test
    void testBuilderWithEmptyTargets() {
        // Given
        AttributeReference source = AttributeReference.builder()
                .attribute("AR0001")
                .catalog("c1")
                .applicationId(Application.builder().sealId("111111").build())
                .build();

        // When
        AttributeLineage lineage = AttributeLineage.builder().source(source).build();

        // Then
        assertThat(lineage.getSource(), is(source));
        assertThat(lineage.getTarget(), is(nullValue()));
    }

    @Test
    void testBuilderWithCombinationOfSingleAndMultipleTargets() {
        // Given
        AttributeReference source = AttributeReference.builder()
                .attribute("AR0001")
                .catalog("c1")
                .applicationId(Application.builder().sealId("111111").build())
                .build();

        AttributeReference target1 = AttributeReference.builder()
                .attribute("AR0002")
                .catalog("c2")
                .applicationId(Application.builder().sealId("222222").build())
                .build();

        AttributeReference target2 = AttributeReference.builder()
                .attribute("AR0003")
                .catalog("c3")
                .applicationId(Application.builder().sealId("333333").build())
                .build();

        AttributeReference target3 = AttributeReference.builder()
                .attribute("AR0004")
                .catalog("c4")
                .applicationId(Application.builder().sealId("444444").build())
                .build();

        Set<AttributeReference> initialTargets = new HashSet<>();
        initialTargets.add(target1);
        initialTargets.add(target2);

        // When
        AttributeLineage lineage = AttributeLineage.builder()
                .source(source)
                .target(initialTargets)
                .target(target3)
                .build();

        // Then
        assertThat(lineage.getSource(), is(source));
        assertThat(lineage.getTarget(), is(notNullValue()));
        assertThat(lineage.getTarget(), hasSize(3));
        assertThat(lineage.getTarget(), containsInAnyOrder(target1, target2, target3));
    }
}
