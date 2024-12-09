package io.github.jpmorganchase.fusion.serializing.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttributes;
import java.lang.reflect.Type;
import java.util.List;

public class DataDictionaryAttributesSerializer implements JsonSerializer<DataDictionaryAttributes> {

    @Override
    public JsonElement serialize(DataDictionaryAttributes src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray jsonObject = new JsonArray();

        List<DataDictionaryAttribute> attributes = src.getDataDictionaryAttributes();
        if (null != attributes) {
            attributes.forEach(a -> jsonObject.add(context.serialize(a)));
        }

        return jsonObject;
    }
}
