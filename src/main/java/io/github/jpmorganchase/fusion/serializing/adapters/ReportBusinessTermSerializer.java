package io.github.jpmorganchase.fusion.serializing.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.jpmorganchase.fusion.model.ReportBusinessTerm;
import java.lang.reflect.Type;
import java.util.Map;

public class ReportBusinessTermSerializer implements JsonSerializer<ReportBusinessTerm> {

    @Override
    public JsonElement serialize(ReportBusinessTerm src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        String attributeId = src.getAttributeId();

        if (attributeId != null) {
            JsonObject attribute = new JsonObject();
            attribute.addProperty("id", attributeId);
            jsonObject.add("attribute", attribute);
        }

        String termId = src.getTermId();

        if (termId != null) {
            JsonObject businessTerm = new JsonObject();
            businessTerm.addProperty("id", termId);
            jsonObject.add("term", businessTerm);
        }

        Map<String, Object> varArgs = src.getVarArgs();
        if (varArgs != null) {
            varArgs.forEach((key, value) -> jsonObject.add(key, context.serialize(value)));
        }

        return jsonObject;
    }
}
