package io.github.jpmorganchase.fusion.model;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
}
