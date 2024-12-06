package io.github.jpmorganchase.fusion.parsing;

import com.google.gson.Gson;
import java.time.LocalDate;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultGsonConfigTest {

    private DefaultGsonConfig defaultGsonConfig;

    @BeforeEach
    void setUp() {
        // Given
        defaultGsonConfig = new DefaultGsonConfig();
    }

    @Test
    void testGsonInitialization() {
        // When
        Gson gson = defaultGsonConfig.getGson();

        // Then
        MatcherAssert.assertThat(gson, Matchers.notNullValue());
    }

    @Test
    void testDeserializeValidDate() {
        // Given
        String jsonDate = "\"2024-12-05\"";
        Gson gson = defaultGsonConfig.getGson();

        // When
        LocalDate result = gson.fromJson(jsonDate, LocalDate.class);

        // Then
        MatcherAssert.assertThat(result, Matchers.is(Matchers.equalTo(LocalDate.of(2024, 12, 5))));
    }

    @Test
    void testDeserializeInvalidDate() {
        // Given
        String invalidJsonDate = "\"2024-12-32\""; // Invalid date (32nd December)
        Gson gson = defaultGsonConfig.getGson();

        // When
        LocalDate result = gson.fromJson(invalidJsonDate, LocalDate.class);

        // Then
        MatcherAssert.assertThat(result, Matchers.nullValue());
    }

    @Test
    void testSharedGsonInstance() {
        // Given
        Gson gson1 = DefaultGsonConfig.gson();
        Gson gson2 = DefaultGsonConfig.gson();

        // When & Then
        MatcherAssert.assertThat(gson1, Matchers.sameInstance(gson2)); // Verifies singleton instance
    }
}
