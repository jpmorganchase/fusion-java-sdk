package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
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
    public void testGetFieldNamesWithValidClass() {
        Set<String> result = VarArgsHelper.getFieldNames(TestClass.class);

        Set<String> expected = new LinkedHashSet<>(Arrays.asList("field1", "field2"));
        assertThat(
                "The method should return all declared fields of the given class.",
                result,
                containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void testGetFieldNamesWithInheritedClass() {
        Set<String> result = VarArgsHelper.getFieldNames(SubClass.class);

        Set<String> expected = new LinkedHashSet<>(Arrays.asList("field3"));
        assertThat(
                "The method should only return fields declared in the subclass, not inherited fields.",
                result,
                containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void testGetFieldNamesWithEmptyClass() {
        class EmptyClass extends CatalogResource {
            public EmptyClass(String identifier, Map<String, Object> varArgs, Fusion fusion, String catalogIdentifier) {
                super(identifier, varArgs, fusion, catalogIdentifier);
            }

            @Override
            protected String getApiPath() {
                return null;
            }
        }

        Set<String> result = VarArgsHelper.getFieldNames(EmptyClass.class);

        assertThat("The method should return an empty set for a class with no declared fields.", result, is(empty()));
    }

    @Test
    public void testGetFieldNamesWithNullClass() {
        Set<String> result = VarArgsHelper.getFieldNames(null);

        assertThat("The method should return an empty set for a null class.", result, is(empty()));
    }

    @Test
    public void testGetFieldNamesExcludesSyntheticFields() {
        class SyntheticFieldClass extends CatalogResource {
            private String field1;
            private transient String this$0; // Synthetic field

            public SyntheticFieldClass(
                    String identifier, Map<String, Object> varArgs, Fusion fusion, String catalogIdentifier) {
                super(identifier, varArgs, fusion, catalogIdentifier);
            }

            @Override
            protected String getApiPath() {
                return null;
            }
        }

        Set<String> result = VarArgsHelper.getFieldNames(SyntheticFieldClass.class);

        assertThat("Synthetic field 'this$0' should not be included.", result, not(hasItem("this$0")));
    }

    @Test
    public void testGetFieldNamesHandlesExactMatch() {
        class ExactMatchTestClass extends CatalogResource {
            private String field1;
            private int field2;

            public ExactMatchTestClass(
                    String identifier, Map<String, Object> varArgs, Fusion fusion, String catalogIdentifier) {
                super(identifier, varArgs, fusion, catalogIdentifier);
            }

            @Override
            protected String getApiPath() {
                return null;
            }
        }

        Set<String> result = VarArgsHelper.getFieldNames(ExactMatchTestClass.class);

        Set<String> expected = new LinkedHashSet<>(Arrays.asList("field1", "field2"));
        assertThat(
                "Field names should match exactly without extra or missing fields.",
                result,
                containsInAnyOrder(expected.toArray()));
    }

    static class TestClass extends CatalogResource {
        private String field1;
        private int field2;

        public TestClass(String identifier, Map<String, Object> varArgs, Fusion fusion, String catalogIdentifier) {
            super(identifier, varArgs, fusion, catalogIdentifier);
        }

        @Override
        protected String getApiPath() {
            return null;
        }
    }

    static class SubClass extends TestClass {
        private double field3;

        public SubClass(String identifier, Map<String, Object> varArgs, Fusion fusion, String catalogIdentifier) {
            super(identifier, varArgs, fusion, catalogIdentifier);
        }
    }
}
