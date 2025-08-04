package io.github.jpmorganchase.fusion.serializing.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.jpmorganchase.fusion.model.DataNodeId;
import io.github.jpmorganchase.fusion.model.Domain;
import io.github.jpmorganchase.fusion.model.Report;
import java.lang.reflect.Type;
import java.util.Map;

public class ReportSerializer implements JsonSerializer<Report> {

    @Override
    public JsonElement serialize(Report src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.add("title", context.serialize(src.getTitle()));
        jsonObject.add("description", context.serialize(src.getDescription()));
        jsonObject.add("frequency", context.serialize(src.getFrequency()));
        jsonObject.add("category", context.serialize(src.getCategory()));
        jsonObject.add("subCategory", context.serialize(src.getSubCategory()));
        jsonObject.add("regulatoryRelated", context.serialize(src.isRegulatoryRelated()));

        JsonObject domain = serializeDomain(src.getDomain(), context);
        if (domain != null) {
            jsonObject.add("domain", domain);
        }

        DataNodeId dataNodeId = src.getDataNodeId();
        if (dataNodeId != null) {
            JsonObject dataNodeJson = new JsonObject();
            dataNodeJson.add("id", context.serialize(dataNodeId.getId()));
            dataNodeJson.add("name", context.serialize(dataNodeId.getName()));
            dataNodeJson.add("dataNodeType", context.serialize(dataNodeId.getDataNodeType()));
            jsonObject.add("dataNodeId", dataNodeJson);
        }

        Map<String, Object> varArgs = src.getVarArgs();
        if (varArgs != null) {
            varArgs.forEach((key, value) -> jsonObject.add(key, context.serialize(value)));
        }

        return jsonObject;
    }

    private JsonObject serializeDomain(Domain domain, JsonSerializationContext context) {
        JsonObject domainJson = null;

        if (domain != null) {
            domainJson = new JsonObject();
            domainJson.add("id", context.serialize(domain.getId()));
            domainJson.add("name", context.serialize(domain.getName()));
        }

        return domainJson;
    }
}
