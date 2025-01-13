package io.github.jpmorganchase.fusion.serializing.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.jpmorganchase.fusion.model.Report;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

public class ReportSerializer implements JsonSerializer<Report> {

    @Override
    public JsonElement serialize(Report src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.add("description", context.serialize(src.getDescription()));
        jsonObject.add("@id", context.serialize(src.getLinkedEntity()));
        jsonObject.add("title", context.serialize(src.getTitle()));
        jsonObject.add("frequency", context.serialize(src.getFrequency()));
        jsonObject.add("identifier", context.serialize(src.getIdentifier()));
        jsonObject.add("type", context.serialize(src.getType()));

        if (isReportPopulated(src)) {
            jsonObject.add("report", context.serialize(src.getReport()));
        }

        jsonObject.add("applicationId", context.serialize(src.getApplicationId()));
        jsonObject.add("publisher", context.serialize(src.getPublisher()));

        Map<String, Object> varArgs = src.getVarArgs();
        if (varArgs != null) {
            varArgs.forEach((key, value) -> jsonObject.add(key, context.serialize(value)));
        }

        return jsonObject;
    }

    private boolean isReportPopulated(Report src) {
        return Objects.nonNull(src.getReport())
                && Objects.nonNull(src.getReport().getTier());
    }
}
