package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ApplicationTest {

    @Test
    void testSealIdTypeBuildsCorrectApplication() {
        Application application = Application.builder().sealId("12345").build();

        assertThat(application, is(notNullValue()));
        assertThat(application.getId(), is(equalTo("12345")));
        assertThat(application.getType(), is(equalTo("Application (SEAL)")));
    }

    @Test
    void testUtIdTypeBuildsCorrectApplication() {
        Application application = Application.builder().userToolId("67890").build();

        assertThat(application, is(notNullValue()));
        assertThat(application.getId(), is(equalTo("67890")));
        assertThat(application.getType(), is(equalTo("User Tool")));
    }

    @Test
    void testIsIdTypeBuildsCorrectApplication() {
        Application application =
                Application.builder().intelligentSolutionsId("54321").build();

        assertThat(application, is(notNullValue()));
        assertThat(application.getId(), is(equalTo("54321")));
        assertThat(application.getType(), is(equalTo("Intelligent Solutions")));
    }

    @Test
    void testDefaultIdTypeWhenNoCustomMethodCalled() {
        Application application = Application.builder().id("99999").build();

        assertThat(application, is(notNullValue()));
        assertThat(application.getId(), is(equalTo("99999")));
        assertThat(application.getType(), is(nullValue()));
    }

    @Test
    void testWhenBothIdAndIdTypeSpecified() {
        Application application =
                Application.builder().id("12345").type("foobar").build();

        assertThat(application, is(notNullValue()));
        assertThat(application.getId(), is(equalTo("12345")));
        assertThat(application.getType(), is(equalTo("foobar")));
    }

    @Test
    void testToMapWithValidIdAndIdType() {
        // Arrange
        Application application =
                Application.builder().id("12345").type("Application (SEAL)").build();

        // Act
        Map<String, String> result = application.toMap();

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(2));
        assertThat(result, hasEntry("id", "12345"));
        assertThat(result, hasEntry("type", "Application (SEAL)"));
    }

    @Test
    void testToMapWithNullIdAndIdType() {
        // Arrange
        Application application = Application.builder().id(null).type(null).build();

        // Act
        Map<String, String> result = application.toMap();

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(2));
        assertThat(result, hasEntry("id", null));
        assertThat(result, hasEntry("type", null));
    }

    @Test
    void testToMapWithEmptyIdAndIdType() {
        // Arrange
        Application application = Application.builder().id("").type("").build();

        // Act
        Map<String, String> result = application.toMap();

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(2));
        assertThat(result, hasEntry("id", ""));
        assertThat(result, hasEntry("type", ""));
    }
}
