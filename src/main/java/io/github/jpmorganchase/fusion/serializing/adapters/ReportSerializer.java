package io.github.jpmorganchase.fusion.serializing.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.jpmorganchase.fusion.model.AlternativeId;
import io.github.jpmorganchase.fusion.model.DataNodeId;
import io.github.jpmorganchase.fusion.model.Domain;
import io.github.jpmorganchase.fusion.model.Report;
import java.lang.reflect.Type;
import java.util.Map;

public class ReportSerializer implements JsonSerializer<Report> {

    @Override
    public JsonElement serialize(Report src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.add("name", context.serialize(src.getName()));
        jsonObject.add("tierType", context.serialize(src.getTierType()));
        jsonObject.add("lob", context.serialize(src.getLob()));

        DataNodeId dataNodeId = src.getDataNodeId();
        if (dataNodeId != null) {
            JsonObject dataNodeJson = new JsonObject();
            dataNodeJson.add("id", context.serialize(dataNodeId.getId()));
            dataNodeJson.add("name", context.serialize(dataNodeId.getName()));
            dataNodeJson.add("dataNodeType", context.serialize(dataNodeId.getDataNodeType()));
            jsonObject.add("dataNodeId", dataNodeJson);
        }

        AlternativeId alternativeId = src.getAlternativeId();
        if (alternativeId != null) {
            JsonObject alternativeIdJson = new JsonObject();

            Domain domain = alternativeId.getDomain();
            if (domain != null) {
                JsonObject domainJson = new JsonObject();
                domainJson.add("id", context.serialize(domain.getId()));
                domainJson.add("name", context.serialize(domain.getName()));
                alternativeIdJson.add("domain", domainJson);
            }

            alternativeIdJson.add("value", context.serialize(alternativeId.getValue()));
            jsonObject.add("alternativeId", alternativeIdJson);
        }

        Map<String, Object> varArgs = src.getVarArgs();
        if (varArgs != null) {
            varArgs.forEach((key, value) -> jsonObject.add(key, context.serialize(value)));
        }

        return jsonObject;
    }
}
