package io.github.jpmorganchase.fusion.serializing;

import com.google.gson.*;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import io.github.jpmorganchase.fusion.model.Dataset;
import io.github.jpmorganchase.fusion.serializing.adapters.DataDictionaryAttributeSerializer;
import io.github.jpmorganchase.fusion.serializing.adapters.DatasetSerializer;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GsonAPIRequestSerializer implements APIRequestSerializer {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Gson gson;

    public GsonAPIRequestSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Dataset.class, new DatasetSerializer());
        gsonBuilder.registerTypeAdapter(DataDictionaryAttribute.class, new DataDictionaryAttributeSerializer());
        gson = gsonBuilder.create();
    }

    @Override
    public String serialize(Object obj) {
        logger.debug("Attempting to serialize object {}", obj);
        return gson.toJson(obj);
    }
}
