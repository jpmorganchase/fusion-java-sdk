package io.github.jpmorganchase.fusion.serializing.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.jpmorganchase.fusion.model.Attribute;
import io.github.jpmorganchase.fusion.model.Attributes;
import java.lang.reflect.Type;
import java.util.List;

public class AttributesSerializer implements JsonSerializer<Attributes> {

    @Override
    public JsonElement serialize(Attributes src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray jsonObject = new JsonArray();

        List<Attribute> attributes = src.getAttributes();
        if (null != attributes) {
            attributes.forEach(a -> jsonObject.add(context.serialize(a)));
        }

        return jsonObject;
    }
}
