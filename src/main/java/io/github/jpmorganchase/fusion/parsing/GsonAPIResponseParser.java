package io.github.jpmorganchase.fusion.parsing;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.api.response.UploadedPart;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.serializing.mutation.MutationContext;
import io.github.jpmorganchase.fusion.serializing.mutation.ResourceMutationFactory;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for parsing JSON responses with various resources.
 *
 * @see GsonAPIResponseParserBuilder
 */
@Builder
public class GsonAPIResponseParser implements APIResponseParser {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Gson gson;
    private final Fusion fusion;

    @Override
    public Map<String, Catalog> parseCatalogResponse(String json) {
        return parseResourcesFromResponse(json, Catalog.class);
    }

    /**
     * Parses a JSON response to extract a map of datasets.
     * <p>
     * This method deserializes the given JSON string into a map of {@link Dataset} objects.
     * Each dataset is enriched with additional context, such as variable arguments and the
     * {@code fusion} configuration, using the builder pattern.
     * </p>
     *
     * @param json the JSON response containing dataset information to be parsed.
     * @return a map where the keys represent dataset identifiers (or relevant keys from the JSON structure),
     *         and the values are {@link Dataset} objects enriched with context.
     */
    @Override
    public Map<String, Dataset> parseDatasetResponse(String json, String catalog) {
        return parseResourcesWithVarArgsFromResponse(json, Dataset.class, (resource, mc) -> resource.toBuilder()
                .varArgs(mc.getVarArgs())
                .fusion(fusion)
                .catalogIdentifier(catalog)
                .build());
    }

    /**
     * Parses a JSON response to extract a map of reports.
     * <p>
     * This method deserializes the given JSON string into a map of {@link Report} objects.
     * Each report is enriched with additional context, such as variable arguments and the
     * {@code fusion} configuration, using the builder pattern.
     * </p>
     *
     * @param json the JSON response containing dataset information to be parsed.
     * @return a map where the keys represent dataset identifiers (or relevant keys from the JSON structure),
     *         and the values are {@link Report} objects enriched with context.
     */
    @Override
    public Map<String, Report> parseReportResponse(String json, String catalog) {
        return parseResourcesWithVarArgsFromResponse(json, Report.class, (resource, mc) -> resource.toBuilder()
                .varArgs(mc.getVarArgs())
                .fusion(fusion)
                .catalogIdentifier(catalog)
                .build());
    }

    /**
     * Parses a JSON response to extract a map of data dictionary attributes.
     * <p>
     * This method deserializes the given JSON string into a map of {@link DataDictionaryAttribute} objects.
     * Each attribute is enriched with additional context, including variable arguments and the {@code fusion} configuration,
     * using the builder pattern.
     * </p>
     *
     * @param json the JSON response containing data dictionary attribute information to be parsed.
     * @param catalog the catalog identifier to associate with each parsed attribute.
     * @return a map where the keys represent attribute identifiers (or relevant keys from the JSON structure),
     *         and the values are {@link DataDictionaryAttribute} objects enriched with context.
     */
    @Override
    public Map<String, DataDictionaryAttribute> parseDataDictionaryAttributesResponse(String json, String catalog) {
        return parseResourcesWithVarArgsFromResponse(
                json, DataDictionaryAttribute.class, (resource, mc) -> resource.toBuilder()
                        .varArgs(mc.getVarArgs())
                        .fusion(fusion)
                        .catalogIdentifier(catalog)
                        .build());
    }

    /**
     * Parses a JSON response to extract a map of attributes associated with a specific catalog and dataset.
     * <p>
     * This method deserializes the given JSON string into a map of attribute objects, using the provided
     * {@code catalog} and {@code dataset} to enrich the parsed attributes with additional context.
     * Each attribute is further customized via a builder pattern.
     * </p>
     *
     * @param json    the JSON response containing attribute data to be parsed.
     * @param catalog the catalog identifier to associate with each parsed attribute.
     * @param dataset the dataset identifier to associate with each parsed attribute.
     * @return a map where the keys are attribute identifiers (or relevant keys from the JSON structure),
     *         and the values are {@link Attribute} objects enriched with the provided context.
     */
    @Override
    public Map<String, Attribute> parseAttributeResponse(String json, String catalog, String dataset) {
        return parseResourcesWithVarArgsFromResponse(json, Attribute.class, (resource, mc) -> resource.toBuilder()
                .datasetIdentifier(dataset)
                .varArgs(mc.getVarArgs())
                .fusion(fusion)
                .catalogIdentifier(catalog)
                .build());
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
    public <T extends CatalogResource> T parseResourceFromResponse(
            String json, Class<T> resourceClass, ResourceMutationFactory<T> mutator) {

        Map<String, Object> responseMap = getMapFromJsonResponse(json);
        T obj = gson.fromJson(json, resourceClass);

        return parseResourceWithVarArgs(obj.getRegisteredAttributes(), obj, responseMap, mutator);
    }

    @Override
    public <T extends CatalogResource> Map<String, T> parseResourcesWithVarArgsFromResponse(
            String json, Class<T> resourceClass, ResourceMutationFactory<T> mutator) {

        Map<String, Map<String, Object>> untypedResources = parseResourcesUntyped(json);
        JsonArray resources = getResources(json);

        List<T> resourceList = new ArrayList<>();
        for (JsonElement element : resources) {
            T obj = gson.fromJson(element, resourceClass);
            Map<String, Object> untypedResource = untypedResources.get(obj.getIdentifier());
            resourceList.add(parseResourceWithVarArgs(obj.getRegisteredAttributes(), obj, untypedResource, mutator));
        }

        return collectMapOfUniqueResources(resourceList);
    }

    @Override
    public <T extends CatalogResource> Map<String, T> parseResourcesFromResponse(String json, Class<T> resourceClass) {

        JsonArray resources = getResources(json);

        Type listType = TypeToken.getParameterized(List.class, resourceClass).getType();
        List<T> resourceList = gson.fromJson(resources, listType);

        return collectMapOfUniqueResources(resourceList);
    }

    @Override
    public Map<String, Map<String, Object>> parseResourcesUntyped(String json) {
        Map<String, Object> responseMap = getMapFromJsonResponse(json);

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
            Set<String> excludes, T obj, Map<String, Object> untypedResource, ResourceMutationFactory<T> mutator) {

        Map<String, Object> varArgs = getVarArgsToInclude(untypedResource, excludes);
        return mutator.mutate(obj, MutationContext.builder().varArgs(varArgs).build());
    }

    private Map<String, Object> getVarArgsToInclude(Map<String, Object> untypedResource, Set<String> exclusionList) {
        HashMap<String, Object> modified = new HashMap<>(untypedResource);
        modified.keySet().removeIf(exclusionList::contains);
        return modified;
    }

    private Map<String, Object> getMapFromJsonResponse(String json) {
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        return gson.fromJson(json, mapType);
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

    public static class GsonAPIResponseParserBuilder {}

    /**
     * A custom builder class for {@link GsonAPIResponseParser} that extends the default builder
     * provided by Lombok's {@code @Builder} annotation. This builder allows for the customization
     * of the {@link Gson} instance used during the construction of the {@link GsonAPIResponseParser}.
     *
     * <p>In the default behavior, if no {@link Gson} instance is provided, the builder will
     * automatically use a default {@link Gson} configuration through the {@link DefaultGsonConfig} class.</p>
     *
     * <p>This class ensures that the {@link Gson} instance is set, either by the user or using
     * the default configuration, before building the {@link GsonAPIResponseParser} object.</p>
     *
     * <p>This builder is particularly useful when you need to customize the behavior of the
     * {@link GsonAPIResponseParser} by ensuring that a valid {@link Gson} object is always available.</p>
     *
     * @see GsonAPIResponseParser
     * @see GsonAPIResponseParserBuilder
     * @see Gson
     * @see DefaultGsonConfig
     */
    public static class CustomGsonAPIResponseParserBuilder extends GsonAPIResponseParser.GsonAPIResponseParserBuilder {

        private Gson gson;

        @Override
        public GsonAPIResponseParser build() {

            if (Objects.isNull(gson)) {
                this.gson = new DefaultGsonConfig().getGson();
            }

            return super.build();
        }
    }
}
