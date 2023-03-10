package com.jpmorganchase.fusion.parsing;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.jpmorganchase.fusion.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GsonAPIResponseParser implements APIResponseParser {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Gson gson;

    public GsonAPIResponseParser() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        //TODO: need to add a serializer as well
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        gson = gsonBuilder.create();
    }

    public GsonAPIResponseParser(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Map<String, Catalog> parseCatalogResponse(String json){
        return parseResourcesFromResponse(json, Catalog.class);
    }

    @Override
    public Map<String, Dataset> parseDatasetResponse(String json){
        return parseResourcesFromResponse(json, Dataset.class);
    }

    @Override
    public Map<String, Attribute> parseAttributeResponse(String json){
        return parseResourcesFromResponse(json, Attribute.class);
    }

    @Override
    public Map<String, DataProduct> parseDataProductResponse(String json){
        return parseResourcesFromResponse(json, DataProduct.class);
    }

    @Override
    public Map<String, DatasetSeries> parseDatasetSeriesResponse(String json){
        return parseResourcesFromResponse(json, DatasetSeries.class);
    }

    @Override
    public Map<String, Distribution> parseDistributionResponse(String json){
        return parseResourcesFromResponse(json, Distribution.class);
    }

    @Override
    public <T extends CatalogResource> Map<String, T> parseResourcesFromResponse(String json, Class<T> resourceClass){
        //TODO: handle varArgs

        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        JsonArray resources = obj.getAsJsonArray("resources"); //TODO: what if this deosn't exist / is empty? write a test

        Type listType = TypeToken.getParameterized(List.class, resourceClass).getType();
        List<T> resourceList = gson.fromJson(resources, listType);
        return resourceList.stream()
                .collect(Collectors.toMap(T::getIdentifier, Function.identity(),
                        //resolve any duplicate keys, for now jsut skip the dups
                        (r1, r2) -> {
                            logger.atWarn().setMessage("Duplicate key '{}' found, will be ignored").addArgument(r2.getIdentifier()).log(); //TODO: Different error handling?
                            return r1;
                        }));
    }

    //TODO: Consider making this public to allow for reuse in the case where a user wants to specify their own instance of Gson
    private static final class LocalDateDeserializer implements JsonDeserializer<LocalDate> {

        private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public LocalDate deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try{
                return LocalDate.parse(jsonElement.getAsString(), dateTimeFormatter);
            }catch(DateTimeParseException e){
                //TODO: We need sensible default behaviour here (logging for sure, but also do we want to just empty the field instead of failing?)
                throw new JsonParseException("Failed to deserialize date field with value " + jsonElement.getAsString(), e);
            }
        }
    }
}
