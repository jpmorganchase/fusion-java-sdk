package io.github.jpmorganchase.fusion.serializing.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.jpmorganchase.fusion.model.DataFlow;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

public class DataFlowSerializer implements JsonSerializer<DataFlow> {

    @Override
    public JsonElement serialize(DataFlow src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject jsonObject = new JsonObject();

        jsonObject.add("description", context.serialize(src.getDescription()));
        jsonObject.add("@id", context.serialize(src.getLinkedEntity()));
        jsonObject.add("title", context.serialize(src.getTitle()));
        jsonObject.add("frequency", context.serialize(src.getFrequency()));
        jsonObject.add("identifier", context.serialize(src.getIdentifier()));
        jsonObject.add("type", context.serialize(src.getType()));
        jsonObject.add("applicationId", context.serialize(src.getApplicationId()));

        if (isProducerApplicationIdPopulated(src)) {
            jsonObject.add("producerApplicationId", context.serialize(src.getProducerApplicationId()));
        }

        if (isConsumerApplicationIdPopulated(src)) {
            jsonObject.add("consumerApplicationId", context.serialize(src.getConsumerApplicationId()));
        }

        jsonObject.add("flowDetails", context.serialize(src.getFlowDetails()));
        jsonObject.add("publisher", context.serialize(src.getPublisher()));

        Map<String, Object> varArgs = src.getVarArgs();
        if (varArgs != null) {
            varArgs.forEach((key, value) -> jsonObject.add(key, context.serialize(value)));
        }

        return jsonObject;
    }

    private boolean isConsumerApplicationIdPopulated(DataFlow src) {
        return src.getConsumerApplicationId() != null
                && !src.getConsumerApplicationId().isEmpty()
                && src.getConsumerApplicationId().stream()
                        .allMatch(appId -> isValuePopulated(appId.getType()) && isValuePopulated(appId.getId()));
    }

    private boolean isValuePopulated(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean isProducerApplicationIdPopulated(DataFlow src) {
        return Objects.nonNull(src.getProducerApplicationId())
                && Objects.nonNull(src.getProducerApplicationId().getId())
                && Objects.nonNull(src.getProducerApplicationId().getType());
    }
}
