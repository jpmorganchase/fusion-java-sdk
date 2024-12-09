package io.github.jpmorganchase.fusion.serializing.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import java.lang.reflect.Type;
import java.util.Map;

public class DataDictionaryAttributeSerializer implements JsonSerializer<DataDictionaryAttribute> {

    @Override
    public JsonElement serialize(DataDictionaryAttribute src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("description", context.serialize(src.getDescription()));
        jsonObject.add("title", context.serialize(src.getTitle()));
        jsonObject.add("identifier", context.serialize(src.getIdentifier()));
        jsonObject.add("applicationId", context.serialize(src.getApplicationId()));

        Map<String, Object> varArgs = src.getVarArgs();
        if (varArgs != null) {
            varArgs.forEach((key, value) -> jsonObject.add(key, context.serialize(value)));
        }

        return jsonObject;
    }
}
