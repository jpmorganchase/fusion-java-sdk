package io.github.jpmorganchase.fusion.serializing.adapters;

import com.google.gson.*;
import io.github.jpmorganchase.fusion.model.Attribute;
import io.github.jpmorganchase.fusion.model.Attributes;
import java.lang.reflect.Type;
import java.util.List;

public class AttributesSerializer implements JsonSerializer<Attributes> {

    @Override
    public JsonElement serialize(Attributes src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        List<Attribute> attributes = src.getAttributes();
        if (null != attributes) {
            JsonArray jsonArray = new JsonArray();
            attributes.forEach(a -> jsonArray.add(context.serialize(a)));
            jsonObject.add("attributes", jsonArray);
        }

        return jsonObject;
    }
}
