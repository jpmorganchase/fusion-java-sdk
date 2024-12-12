package io.github.jpmorganchase.fusion.serializing.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.jpmorganchase.fusion.model.Flow;
import java.lang.reflect.Type;

public class FlowSerializer implements JsonSerializer<Flow> {

    @Override
    public JsonElement serialize(Flow src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("flowDirection", context.serialize(src.getFlowDirection()));
        return jsonObject;
    }
}
