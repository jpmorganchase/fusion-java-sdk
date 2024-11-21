package io.github.jpmorganchase.fusion.parsing;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.github.jpmorganchase.fusion.api.response.UploadedPart;
import io.github.jpmorganchase.fusion.model.*;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GsonAPIResponseParser implements APIResponseParser {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Gson gson;

    public GsonAPIResponseParser() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        // TODO: need to add a serializer as well once we get to update operations
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        gson = gsonBuilder.create();
    }

    public GsonAPIResponseParser(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Map<String, Catalog> parseCatalogResponse(String json) {
        return parseResourcesFromResponse(json, Catalog.class);
    }

    @Override
    public Map<String, Dataset> parseDatasetResponse(String json) {
        return parseResourcesFromResponse(json, Dataset.class);
    }

    @Override
    public Map<String, Attribute> parseAttributeResponse(String json) {
        return parseResourcesFromResponse(json, Attribute.class);
    }

    @Override
    public Map<String, DataProduct> parseDataProductResponse(String json) {
        return parseResourcesFromResponse(json, DataProduct.class);
    }

    @Override
    public Map<String, DatasetSeries> parseDatasetSeriesResponse(String json) {
        return parseResourcesFromResponse(json, DatasetSeries.class);
    }

    @Override
    public Map<String, Distribution> parseDistributionResponse(String json) {
        return parseResourcesFromResponse(json, Distribution.class);
    }

    @Override
    public Operation parseOperationResponse(String json) {
        return gson.fromJson(json, Operation.class);
    }

    @Override
    public UploadedPart parseUploadPartResponse(String json) {
        return new GsonBuilder().create().fromJson(json, UploadedPart.class);
    }

    @Override
    public <T extends CatalogResource> Map<String, T> parseResourcesFromResponseWithVarArgs(
            String json, Class<T> resourceClass) {

        Map<String, Map<String, Object>> untypedResources = parseResourcesUntyped(json);
        JsonArray resources = getResources(json);

        Set<String> excludes = varArgsExclusions(resourceClass);
        List<T> resourceList = new ArrayList<>();
        for (JsonElement element : resources) {
            resourceList.add(parseResourceWithVarArgs(resourceClass, excludes, element, untypedResources));
        }

        return collectMapOfUniqueResources(resourceList);
    }

    @Override
    public <T extends CatalogResource> Map<String, T> parseResourcesFromResponse(String json, Class<T> resourceClass) {
        // TODO: handle varArgs

        JsonArray resources = getResources(json);

        Type listType = TypeToken.getParameterized(List.class, resourceClass).getType();
        List<T> resourceList = gson.fromJson(resources, listType);

        return collectMapOfUniqueResources(resourceList);
    }

    @Override
    public Map<String, Map<String, Object>> parseResourcesUntyped(String json) {
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> responseMap = gson.fromJson(json, mapType);

        Object resources = responseMap.get("resources");
        if (resources instanceof List) {
            @SuppressWarnings("unchecked") // List<Object> is always safe, compiler disagrees
            List<Object> resourceList = (List<Object>) resources;

            if (resourceList.size() == 0) throw generateNoResourceException();
            Map<String, Map<String, Object>> resourcesMap = new HashMap<>();
            resourceList.forEach((o -> {
                @SuppressWarnings("unchecked") // Output of GSON parsing will always be in this format
                Map<String, Object> resource = (Map<String, Object>) o;

                String identifier = (String) resource.get("identifier");
                resourcesMap.put(identifier, resource);
            }));
            return resourcesMap;
        } else {
            throw generateNoResourceException();
        }
    }


    private JsonArray getResources(String json) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        JsonArray array = obj.getAsJsonArray("resources");
        if (array == null || array.size() == 0) {
            throw generateNoResourceException();
        }
        return array;
    }

    private ParsingException generateNoResourceException() {
        String message = "Failed to parse resources from JSON, none found";
        logger.error(message);
        return new ParsingException(message);
    }

    private <T extends CatalogResource> T parseResourceWithVarArgs(
            Class<T> resourceClass, Set<String> excludes, JsonElement element, Map<String, Map<String, Object>> untypedResources) {
        T obj = gson.fromJson(element, resourceClass);

        Map<String, Object> varArgs = getVarArgsToInclude(untypedResources.get(obj.getIdentifier()), excludes);

        obj.setVarArgs(varArgs);
        return obj;
    }

    private Map<String, Object> getVarArgsToInclude(Map<String, Object> untypedResource, Set<String> exclusionList) {
        HashMap<String, Object> modified = new HashMap<>(untypedResource);
        modified.keySet().removeIf(exclusionList::contains);
        return modified;
    }

    private static <T extends CatalogResource> Set<String> varArgsExclusions(Class<T> resourceClass) {
        // TODO :: Should this be returned by the Model Object ? It Should
        Set<String> excludes = new HashSet<>();
        Set<String> excludeFromType = Arrays.stream(resourceClass.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
        Set<String> excludeFromCatalogResource = Arrays.stream(CatalogResource.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
        excludes.addAll(excludeFromType);
        excludes.addAll(excludeFromCatalogResource);
        excludes.add("@id");
        return excludes;
    }

    private static <T extends CatalogResource> Map<String, T> collectMapOfUniqueResources(List<T> resourceList) {
        return resourceList.stream()
                .collect(Collectors.toMap(
                        T::getIdentifier,
                        Function.identity(),
                        // resolve any duplicate keys, for now just skip the duplicates
                        (r1, r2) -> {
                            logger.warn("Duplicate key '{}' found, will be ignored", r2.getIdentifier());
                            return r1;
                        }));
    }

    private static final class LocalDateDeserializer implements JsonDeserializer<LocalDate> {

        private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public LocalDate deserialize(
                JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
                throws JsonParseException {
            try {
                return LocalDate.parse(jsonElement.getAsString(), dateTimeFormatter);
            } catch (DateTimeParseException e) {
                String message = "Failed to deserialize date field with value " + jsonElement.getAsString();
                logger.warn(message);
                return null;
            }
        }
    }
}
