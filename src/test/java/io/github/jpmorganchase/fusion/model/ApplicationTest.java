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
        assertThat(application.getIdType(), is(equalTo("SEAL")));
    }

    @Test
    void testUtIdTypeBuildsCorrectApplication() {
        Application application = Application.builder().utId("67890").build();

        assertThat(application, is(notNullValue()));
        assertThat(application.getId(), is(equalTo("67890")));
        assertThat(application.getIdType(), is(equalTo("UT")));
    }

    @Test
    void testIsIdTypeBuildsCorrectApplication() {
        Application application = Application.builder().isId("54321").build();

        assertThat(application, is(notNullValue()));
        assertThat(application.getId(), is(equalTo("54321")));
        assertThat(application.getIdType(), is(equalTo("is")));
    }

    @Test
    void testDefaultIdTypeWhenNoCustomMethodCalled() {
        Application application = Application.builder().id("99999").build();

        assertThat(application, is(notNullValue()));
        assertThat(application.getId(), is(equalTo("99999")));
        assertThat(application.getIdType(), is(nullValue()));
    }

    @Test
    void testWhenBothIdAndIdTypeSpecified() {
        Application application =
                Application.builder().id("12345").idType("SEAL").build();

        assertThat(application, is(notNullValue()));
        assertThat(application.getId(), is(equalTo("12345")));
        assertThat(application.getIdType(), is(equalTo("SEAL")));
    }

    @Test
    void testToMapWithValidIdAndIdType() {
        // Arrange
        Application application =
                Application.builder().id("12345").idType("SEAL").build();

        // Act
        Map<String, String> result = application.toMap();

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(2));
        assertThat(result, hasEntry("id", "12345"));
        assertThat(result, hasEntry("idType", "SEAL"));
    }

    @Test
    void testToMapWithNullIdAndIdType() {
        // Arrange
        Application application = Application.builder().id(null).idType(null).build();

        // Act
        Map<String, String> result = application.toMap();

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(2));
        assertThat(result, hasEntry("id", null));
        assertThat(result, hasEntry("idType", null));
    }

    @Test
    void testToMapWithEmptyIdAndIdType() {
        // Arrange
        Application application = Application.builder().id("").idType("").build();

        // Act
        Map<String, String> result = application.toMap();

        // Assert
        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(2));
        assertThat(result, hasEntry("id", ""));
        assertThat(result, hasEntry("idType", ""));
    }
}
