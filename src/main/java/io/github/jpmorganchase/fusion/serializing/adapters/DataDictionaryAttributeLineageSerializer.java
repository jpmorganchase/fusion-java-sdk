package io.github.jpmorganchase.fusion.serializing.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttributeLineage;
import java.lang.reflect.Type;

public class DataDictionaryAttributeLineageSerializer implements JsonSerializer<DataDictionaryAttributeLineage> {

    @Override
    public JsonElement serialize(DataDictionaryAttributeLineage src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("catalog", context.serialize(src.getCatalogIdentifier()));
        jsonObject.add("attribute", context.serialize(src.getIdentifier()));

        return jsonObject;
    }
}
