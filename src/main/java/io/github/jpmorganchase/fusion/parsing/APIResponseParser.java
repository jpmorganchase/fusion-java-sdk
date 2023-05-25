package io.github.jpmorganchase.fusion.parsing;

import io.github.jpmorganchase.fusion.model.*;
import java.util.Map;

public interface APIResponseParser {
    Map<String, Catalog> parseCatalogResponse(String json);

    Map<String, Dataset> parseDatasetResponse(String json);

    Map<String, Attribute> parseAttributeResponse(String json);

    Map<String, DataProduct> parseDataProductResponse(String json);

    Map<String, DatasetSeries> parseDatasetSeriesResponse(String json);

    Map<String, Distribution> parseDistributionResponse(String json);

    <T extends CatalogResource> Map<String, T> parseResourcesFromResponse(String json, Class<T> resourceClass);

    Map<String, Map<String, Object>> parseResourcesUntyped(String json);

    Operation parseOperationResponse(String json);

    UploadedPart parseUploadPartResponse(String json);
}
