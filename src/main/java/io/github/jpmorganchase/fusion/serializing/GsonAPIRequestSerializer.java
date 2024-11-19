package io.github.jpmorganchase.fusion.serializing;

import com.google.gson.*;
import io.github.jpmorganchase.fusion.model.Dataset;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GsonAPIRequestSerializer implements APIRequestSerializer {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Gson gson;

    public GsonAPIRequestSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Dataset.class, new DatasetSerializer());
        gson = gsonBuilder.create();
    }

    @Override
    public String serializeDatasetRequest(Dataset dataset) {
        logger.debug("Attempting to serialize dataset {}", dataset);
        return gson.toJson(dataset);
    }

    private static final class DatasetSerializer implements JsonSerializer<Dataset> {

        @Override
        public JsonElement serialize(Dataset src, Type typeOfSrc, JsonSerializationContext context) {

            JsonObject jsonObject = new JsonObject();

            jsonObject.add("description", context.serialize(src.getDescription()));
            jsonObject.add("@id", context.serialize(src.getLinkedEntity()));
            jsonObject.add("title", context.serialize(src.getTitle()));
            jsonObject.add("frequency", context.serialize(src.getFrequency()));
            jsonObject.add("identifier", context.serialize(src.getIdentifier()));

            Map<String, Object> varArgs = src.getVarArgs();
            if (varArgs != null) {
                varArgs.forEach((key, value) -> jsonObject.add(key, context.serialize(value)));
            }

            if (jsonObject.has("varArgs")) {
                jsonObject.remove("varArgs");
            }

            return jsonObject;
        }
    }
}
