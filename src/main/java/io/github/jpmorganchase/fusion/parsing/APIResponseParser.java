package io.github.jpmorganchase.fusion.parsing;

import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.api.response.UploadedPart;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.serializing.mutation.ResourceMutationFactory;
import java.util.Map;

public interface APIResponseParser {
    Map<String, Catalog> parseCatalogResponse(String json);

    Map<String, Dataset> parseDatasetResponse(Fusion fusion, String json);

    Map<String, Attribute> parseAttributeResponse(Fusion fusion, String json, String dataset);

    Map<String, DataDictionaryAttribute> parseDataDictionaryAttributeResponse(Fusion fusion, String json);

    Map<String, DataProduct> parseDataProductResponse(String json);

    Map<String, DatasetSeries> parseDatasetSeriesResponse(String json);

    Map<String, Distribution> parseDistributionResponse(String json);

    <T extends CatalogResource> T parseResourceFromResponse(
            String json, Class<T> resourceClass, ResourceMutationFactory<T> mutator);

    <T extends CatalogResource> Map<String, T> parseResourcesFromResponse(String json, Class<T> resourceClass);

    <T extends CatalogResource> Map<String, T> parseResourcesWithVarArgsFromResponse(
            String json, Class<T> resourceClass, ResourceMutationFactory<T> mutator);

    Map<String, Map<String, Object>> parseResourcesUntyped(String json);

    Operation parseOperationResponse(String json);

    UploadedPart parseUploadPartResponse(String json);
}
