package io.github.jpmorganchase.fusion.serializing.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.jpmorganchase.fusion.model.Dataset;
import java.lang.reflect.Type;
import java.util.Map;

public class DatasetSerializer implements JsonSerializer<Dataset> {

    @Override
    public JsonElement serialize(Dataset src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.add("description", context.serialize(src.getDescription()));
        jsonObject.add("@id", context.serialize(src.getLinkedEntity()));
        jsonObject.add("title", context.serialize(src.getTitle()));
        jsonObject.add("frequency", context.serialize(src.getFrequency()));
        jsonObject.add("identifier", context.serialize(src.getIdentifier()));
        jsonObject.add("type", context.serialize(src.getType()));
        jsonObject.add("report", context.serialize(src.getReport()));

        Map<String, Object> varArgs = src.getVarArgs();
        if (varArgs != null) {
            varArgs.forEach((key, value) -> jsonObject.add(key, context.serialize(value)));
        }

        return jsonObject;
    }
}
