package io.github.jpmorganchase.fusion.serializing.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.jpmorganchase.fusion.model.AttributeLineage;
import io.github.jpmorganchase.fusion.model.AttributeLineages;
import java.lang.reflect.Type;
import java.util.Set;

public class AttributeLineagesSerializer implements JsonSerializer<AttributeLineages> {

    @Override
    public JsonElement serialize(AttributeLineages src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray jsonObject = new JsonArray();

        Set<AttributeLineage> attributes = src.getAttributeLineages();
        if (null != attributes) {
            attributes.forEach(a -> jsonObject.add(context.serialize(a)));
        }

        return jsonObject;
    }
}
