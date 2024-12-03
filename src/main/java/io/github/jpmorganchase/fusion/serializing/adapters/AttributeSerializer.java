package io.github.jpmorganchase.fusion.serializing.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.jpmorganchase.fusion.model.Attribute;
import java.lang.reflect.Type;
import java.util.Map;

public class AttributeSerializer implements JsonSerializer<Attribute> {

    @Override
    public JsonElement serialize(Attribute src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("isDatasetKey", context.serialize(src.isKey()));
        jsonObject.add("dataType", context.serialize(src.getDataType()));
        jsonObject.add("index", context.serialize(src.getIndex()));
        jsonObject.add("description", context.serialize(src.getDescription()));
        jsonObject.add("title", context.serialize(src.getTitle()));
        jsonObject.add("identifier", context.serialize(src.getIdentifier()));
        jsonObject.add("isCriticalDataElement", context.serialize(src.isCriticalDataElement()));

        Map<String, Object> varArgs = src.getVarArgs();
        if (varArgs != null) {
            varArgs.forEach((key, value) -> jsonObject.add(key, context.serialize(value)));
        }

        return jsonObject;
    }
}
