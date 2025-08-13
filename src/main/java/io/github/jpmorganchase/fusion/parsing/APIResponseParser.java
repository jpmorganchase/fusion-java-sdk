package io.github.jpmorganchase.fusion.parsing;

import io.github.jpmorganchase.fusion.api.response.UploadedPart;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.serializing.mutation.ResourceMutationFactory;
import java.util.Map;

public interface APIResponseParser {
    Map<String, Catalog> parseCatalogResponse(String json);

    Map<String, Dataset> parseDatasetResponse(String json, String catalog);

    DatasetLineage parseDatasetLineage(String json, String catalog);

    Map<String, Attribute> parseAttributeResponse(String json, String catalog, String dataset);

    Map<String, DataProduct> parseDataProductResponse(String json);

    Map<String, DatasetSeries> parseDatasetSeriesResponse(String json);

    Map<String, Distribution> parseDistributionResponse(String json);

    <T extends CatalogResource> Map<String, T> parseResourcesFromResponse(String json, Class<T> resourceClass);

    <T extends CatalogResource> Map<String, T> parseResourcesWithVarArgsFromResponse(
            String json, Class<T> resourceClass, ResourceMutationFactory<T> mutator);

    Map<String, Map<String, Object>> parseResourcesUntyped(String json);

    Map<String, Map<String, Object>> parseResourcesUntypedList(String json, String identifierAttribute);

    Operation parseOperationResponse(String json);

    UploadedPart parseUploadPartResponse(String json);
}
