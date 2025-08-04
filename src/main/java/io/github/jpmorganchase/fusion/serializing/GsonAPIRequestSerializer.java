package io.github.jpmorganchase.fusion.serializing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.model.Report;
import io.github.jpmorganchase.fusion.serializing.adapters.*;
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
        gsonBuilder.registerTypeAdapter(Report.class, new ReportSerializer());
        gsonBuilder.registerTypeAdapter(ReportAttribute.class, new ReportAttributeSerializer());
        gsonBuilder.registerTypeAdapter(ReportBusinessTerm.class, new ReportBusinessTermSerializer());
        gsonBuilder.registerTypeAdapter(DataFlow.class, new DataFlowSerializer());
        gsonBuilder.registerTypeAdapter(DataDictionaryAttribute.class, new DataDictionaryAttributeSerializer());
        gsonBuilder.registerTypeAdapter(DataDictionaryAttributes.class, new DataDictionaryAttributesSerializer());
        gsonBuilder.registerTypeAdapter(Attribute.class, new AttributeSerializer());
        gsonBuilder.registerTypeAdapter(Attributes.class, new AttributesSerializer());
        gsonBuilder.registerTypeAdapter(Flow.class, new FlowSerializer());
        gsonBuilder.registerTypeAdapter(AttributeLineages.class, new AttributeLineagesSerializer());
        gson = gsonBuilder.create();
    }

    @Override
    public String serialize(Object obj) {
        logger.debug("Attempting to serialize object {}", obj);
        return gson.toJson(obj);
    }
}
