package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class VarArgsHelperTest {

    @Test
    void testVarArgCreatesNewMapWhenNull() {
        Map<String, Object> varArgs = null;
        varArgs = VarArgsHelper.varArg("key1", "value1", varArgs);

        assertNotNull(varArgs);
        assertEquals(1, varArgs.size());
        assertEquals("value1", varArgs.get("key1"));
    }

    @Test
    void testVarArgAddsToExistingMap() {
        Map<String, Object> varArgs = new HashMap<>();
        varArgs.put("existingKey", "existingValue");

        varArgs = VarArgsHelper.varArg("newKey", "newValue", varArgs);

        assertEquals(2, varArgs.size());
        assertEquals("existingValue", varArgs.get("existingKey"));
        assertEquals("newValue", varArgs.get("newKey"));
    }

    @Test
    void testVarArgHandlesNullKeyOrValue() {
        Map<String, Object> varArgs = null;

        varArgs = VarArgsHelper.varArg(null, "value1", varArgs);
        assertNotNull(varArgs);
        assertEquals(1, varArgs.size());
        assertEquals("value1", varArgs.get(null));

        varArgs = VarArgsHelper.varArg("key1", null, varArgs);
        assertEquals(2, varArgs.size());
        assertNull(varArgs.get("key1"));
    }

    @Test
    void testCopyMapCopiesNonNullMap() {
        Map<String, Object> source = new HashMap<>();
        source.put("key1", "value1");
        source.put("key2", "value2");

        Map<String, Object> target = VarArgsHelper.copyMap(source);

        assertNotNull(target);
        assertEquals(2, target.size());
        assertEquals("value1", target.get("key1"));
        assertEquals("value2", target.get("key2"));

        // Verify deep copy
        target.put("key3", "value3");
        assertFalse(source.containsKey("key3"));
    }

    @Test
    void testCopyMapReturnsNullForNullSource() {
        Map<String, Object> source = null;
        Map<String, Object> target = VarArgsHelper.copyMap(source);

        assertNull(target);
    }

    @Test
    void testInitializeMapReturnsEmptyMap() {
        Map<String, Object> map = VarArgsHelper.initializeMap();

        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testGetFieldNames() {
        Set<String> result = VarArgsHelper.getFieldNames(TestClass.class);

        Set<String> expected = new HashSet<>(Arrays.asList("field1", "field2"));
        assertThat("The method should return all declared fields.", result, is(expected));
    }

    @Test
    public void testGetFieldNamesWithInheritance() {
        Set<String> result = VarArgsHelper.getFieldNames(SubClass.class);

        assertThat(
                "The method should only include fields declared in the given class.",
                result,
                containsInAnyOrder("field3"));
    }

    @Test
    public void testGetFieldNamesWithEmptyClass() {
        class EmptyClass {}

        Set<String> result = VarArgsHelper.getFieldNames(EmptyClass.class);

        assertThat("The method should return an empty set for a class with no declared fields.", result, is(empty()));
    }

    @Test
    public void testGetFieldNamesWithNullClass() {
        Set<String> result = VarArgsHelper.getFieldNames(null);

        assertThat("The method should return an empty set for a class with no declared fields.", result, is(empty()));
    }

    static class TestClass {
        private String field1;
        private int field2;
    }

    static class SubClass extends TestClass {
        private double field3;
    }
}
