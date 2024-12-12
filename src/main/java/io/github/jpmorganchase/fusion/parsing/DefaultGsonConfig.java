package io.github.jpmorganchase.fusion.parsing;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides a default configuration for creating a {@link Gson} instance.
 * <p>
 * This class initializes a {@link Gson} object with custom settings, such as
 * a type adapter for {@link LocalDate} deserialization.
 * It implements the {@link GsonConfig} interface.
 * </p>
 */
@Slf4j
@Getter
public class DefaultGsonConfig implements GsonConfig {

    private static final Gson SHARED_GSON = new DefaultGsonConfig().getGson();

    private final Gson gson;

    /**
     * Constructs a {@code DefaultGsonConfig} instance and initializes
     * the {@link Gson} object with the default configuration.
     */
    public DefaultGsonConfig() {
        gson = initialiseGson();
    }

    /**
     * Initializes and configures the {@link Gson} object.
     * <p>
     * Registers a custom type adapter for {@link LocalDate} to handle its
     * deserialization.
     * </p>
     *
     * @return a configured {@link Gson} instance.
     */
    private Gson initialiseGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new DefaultGsonConfig.LocalDateDeserializer());
        return gsonBuilder.create();
    }

    /**
     * Returns a default {@link Gson} instance configured with this class's settings.
     * <p>
     * This method provides a shared, thread-safe {@link Gson} instance.
     * </p>
     *
     * @return a shared {@link Gson} instance with the default configuration.
     */
    public static Gson gson() {
        return SHARED_GSON;
    }

    private static final class LocalDateDeserializer implements JsonDeserializer<LocalDate> {

        private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public LocalDate deserialize(
                JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {
            try {
                return LocalDate.parse(jsonElement.getAsString(), dateTimeFormatter);
            } catch (DateTimeParseException e) {
                String message = "Failed to deserialize date field with value " + jsonElement.getAsString();
                log.warn(message);
                return null;
            }
        }
    }
}
