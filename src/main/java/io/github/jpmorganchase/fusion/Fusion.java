package io.github.jpmorganchase.fusion;

import static io.github.jpmorganchase.fusion.filter.DatasetFilter.filterDatasets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.api.FusionAPIManager;
import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.exception.ApiInputValidationException;
import io.github.jpmorganchase.fusion.api.exception.FileDownloadException;
import io.github.jpmorganchase.fusion.api.exception.FileUploadException;
import io.github.jpmorganchase.fusion.builders.APIConfiguredBuilders;
import io.github.jpmorganchase.fusion.builders.Builders;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.http.JdkClient;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.oauth.credential.BearerTokenCredentials;
import io.github.jpmorganchase.fusion.oauth.credential.Credentials;
import io.github.jpmorganchase.fusion.oauth.credential.OAuthPasswordBasedCredentials;
import io.github.jpmorganchase.fusion.oauth.credential.OAuthSecretBasedCredentials;
import io.github.jpmorganchase.fusion.oauth.exception.OAuthException;
import io.github.jpmorganchase.fusion.oauth.provider.DefaultFusionTokenProvider;
import io.github.jpmorganchase.fusion.oauth.provider.FusionTokenProvider;
import io.github.jpmorganchase.fusion.parsing.APIResponseParser;
import io.github.jpmorganchase.fusion.parsing.DefaultGsonConfig;
import io.github.jpmorganchase.fusion.parsing.GsonAPIResponseParser;
import io.github.jpmorganchase.fusion.parsing.ParsingException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * Class representing the Fusion API, providing methods that correspond to available API endpoints
 */
@Slf4j
@SuppressWarnings({"LombokSetterMayBeUsed", "LombokGetterMayBeUsed"})
public class Fusion {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
    private static int defaultPageSize = -1;

    private final APIManager api;
    private String defaultCatalog;
    private final String defaultPath;
    private final String rootURL;
    private final APIResponseParser responseParser;
    private final Builders builders;

    @Builder
    public Fusion(
            APIManager api,
            String defaultCatalog,
            String defaultPath,
            String rootURL,
            APIResponseParser responseParser,
            Builders builders) {
        this.api = api;
        this.defaultCatalog = defaultCatalog;
        this.defaultPath = defaultPath;
        this.rootURL = rootURL;
        this.responseParser = initApiResponseParser(responseParser);
        this.builders = initApiResourceBuilders(builders);
    }

    /**
     * Initializes the API resource builders if they have not already been set.
     * <p>
     * If the provided {@code builders} parameter is {@code null}, a new instance of
     * {@link APIConfiguredBuilders} is created and initialized with the current object.
     * Otherwise, the provided {@code builders} instance is returned unchanged.
     * </p>
     *
     * @param builders the {@link Builders} instance to initialize, or {@code null}
     *                 to create a new default instance.
     * @return an initialized {@link Builders} instance.
     */
    private Builders initApiResourceBuilders(Builders builders) {
        return Objects.isNull(builders) ? new APIConfiguredBuilders(this) : builders;
    }

    /**
     * Initializes the API response parser if it has not already been set.
     * <p>
     * If the provided {@code responseParser} is {@code null}, a default instance of
     * {@link GsonAPIResponseParser} is created using the {@link DefaultGsonConfig}.
     * Otherwise, the provided {@code responseParser} is returned unchanged.
     * </p>
     *
     * @param responseParser the {@link APIResponseParser} instance to initialize, or {@code null}
     *                       to use the default parser.
     * @return an initialized {@link APIResponseParser} instance.
     */
    private APIResponseParser initApiResponseParser(APIResponseParser responseParser) {
        return Objects.isNull(responseParser)
                ? GsonAPIResponseParser.builder()
                        .gson(DefaultGsonConfig.gson())
                        .fusion(this)
                        .build()
                : responseParser;
    }

    /**
     * Get the default download path - Please see default {@link FusionConfiguration}
     */
    public String getDefaultPath() {
        return this.defaultPath;
    }

    /**
     * Get the rootURL - Please see default {@link FusionConfiguration}
     */
    public String getRootURL() {
        return this.rootURL;
    }

    /**
     * Get the default catalog identifier - Please see default {@link FusionConfiguration}
     */
    public String getDefaultCatalog() {
        return this.defaultCatalog;
    }

    /**
     * Set the default catalog. This allows the default context to be a specified catalog.
     *
     * @param newDefaultCatalogName the identifier of the catalog to set as the default
     */
    public void setDefaultCatalog(String newDefaultCatalogName) {
        this.defaultCatalog = newDefaultCatalogName;
    }

    /**
     * Update the currently used bearer token, where this is supported by the underlying Credentials implementation
     *
     * @param token value of the new bearer token to be used
     * @throws ApiInputValidationException if the underlying Credentials instance does not support a token update
     */
    // TODO: The implementation of this needs some thought. This way makes it difficult to test in isolation
    public void updateBearerToken(String token) {
        if (this.api instanceof FusionAPIManager) {
            ((FusionAPIManager) this.api).updateBearerToken(token);
        } else {
            throw new FusionException("Bearer token update not supported");
        }
    }

    /**
     * Creates a new catalog resource by sending a POST request to the specified API path.
     *
     * @param apiPath the API endpoint path where the resource should be created.
     * @param resource the {@link Object} to be created.
     * @return the response from the API as a {@link String}.
     */
    public String create(String apiPath, Object resource) {
        return this.api.callAPIToPost(apiPath, resource);
    }

    /**
     * Updates an existing catalog resource by sending a PUT request to the specified API path.
     *
     * @param apiPath the API endpoint path where the resource exists.
     * @param resource the {@link Object} containing the updated data.
     * @return the response from the API as a {@link String}.
     */
    public String update(String apiPath, Object resource) {
        return this.api.callAPIToPut(apiPath, resource);
    }

    /**
     * Deletes a catalog resource by sending a DELETE request to the specified API path.
     *
     * @param apiPath the API endpoint path where the resource to be deleted exists.
     * @return the response from the API as a {@link String}.
     */
    public String delete(String apiPath) {
        return this.api.callAPIToDelete(apiPath);
    }

    /**
     * Call the API and returns a data table structure containing the results of the API call in tabular form
     *
     * @param url the API url to call
     * @return a map from the returned json object
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    private Map<String, Map<String, Object>> callForMap(String url) {
        String json = this.api.callAPI(url);
        return responseParser.parseResourcesUntyped(json);
    }

    /**
     * Makes paginated API calls and aggregates all results transparently.
     * This method handles the pagination logic internally, making multiple API calls
     * as needed and combining the results into a single response string.
     *
     * @param url the API endpoint URL
     * @return aggregated JSON response containing all pages of data
     */
    private String callAPIWithPagination(String url) {
        log.debug("Starting paginated request to URL: {}", url);

        Map<String, String> headers = new HashMap<>();
        headers.put("x-jpmc-paginate", "true");
        if (defaultPageSize > 0) {
            log.debug("Using page size: {}", defaultPageSize);
            headers.put("x-jpmc-page-size", String.valueOf(defaultPageSize));
        }

        Gson gson = new Gson();
        JsonArray aggregatedResources = new JsonArray();
        String nextToken = null;
        int pageCount = 0;

        do {
            pageCount++;
            if (nextToken != null) {
                headers.put("x-jpmc-next-token", nextToken);
                log.debug("Fetching page {} with next token", pageCount);
            } else {
                log.debug("Fetching page {}", pageCount);
            }

            HttpResponse<String> response = this.api.callAPIWithResponse(url, headers);
            String pageJson = response.getBody();

            JsonObject pageObject = JsonParser.parseString(pageJson).getAsJsonObject();
            if (pageObject.has("resources") && pageObject.get("resources").isJsonArray()) {
                JsonArray pageResources = pageObject.getAsJsonArray("resources");
                int pageResourceCount = pageResources.size();
                pageResources.forEach(aggregatedResources::add);
                log.debug("Retrieved {} resources from page {}", pageResourceCount, pageCount);
            }

            nextToken = getHeaderValue(response.getHeaders(), "x-jpmc-next-token");

            if (nextToken != null && !nextToken.isEmpty()) {
                log.debug("Next token received, more pages available");
            }

        } while (nextToken != null && !nextToken.isEmpty());

        log.debug(
                "Pagination complete. Total pages fetched: {}, Total resources: {}",
                pageCount,
                aggregatedResources.size());

        JsonObject result = new JsonObject();
        result.add("resources", aggregatedResources);
        return gson.toJson(result);
    }

    /**
     * Gets a header value from the response headers map (case-insensitive).
     *
     * @param headers the response headers map
     * @param headerName the header name to look for
     * @return the header value, or null if not found
     */
    @SuppressWarnings("SameParameterValue")
    private String getHeaderValue(Map<String, List<String>> headers, String headerName) {
        if (headers == null || headerName == null) {
            return null;
        }

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(headerName)) {
                List<String> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    return values.get(0);
                }
            }
        }
        return null;
    }

    /**
     * Get a list of the catalogs available to the API account.
     *
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Catalog> listCatalogs() {
        String url = rootURL.concat("catalogs");
        String json = callAPIWithPagination(url);
        return responseParser.parseCatalogResponse(json);
    }

    /**
     * Return the resources available for a specified catalog, currently this will return only products and datasets
     *
     * @param catalogName identifier of the catalog to be queried
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Map<String, Object>> catalogResources(String catalogName) {
        String url = String.format("%1scatalogs/%2s", this.rootURL, catalogName);
        return this.callForMap(url);
    }

    /**
     * Get a filtered list of the data products in the specified catalog
     * <p>
     * Note that as of the current version, this search capability is not yet implemented
     *
     * @param catalogName identifier of the catalog to be queried
     * @param contains    a search keyword.
     * @param idContains  is true if only apply the filter to the identifier
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, DataProduct> listProducts(String catalogName, String contains, boolean idContains) {
        // TODO: unimplemented logic implied by the method parameters
        String url = String.format("%1scatalogs/%2s/products", this.rootURL, catalogName);
        String json = callAPIWithPagination(url);
        return responseParser.parseDataProductResponse(json);
    }

    /**
     * Get a list of the data products in the specified catalog
     *
     * @param catalogName identifier of the catalog to be queried
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, DataProduct> listProducts(String catalogName) {

        return listProducts(catalogName, null, false);
    }

    /**
     * Get a list of the data products in the default catalog
     *
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, DataProduct> listProducts() {
        return listProducts(this.getDefaultCatalog(), null, false);
    }

    /**
     * Get a filtered list of the datasets in the specified catalog
     * <p>
     * Note that as of the current version, this search capability is not yet implemented
     *
     * @param catalogName identifier of the catalog to be queried
     * @param contains    a search keyword.
     * @param idContains  is true if only apply the filter to the identifier
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Dataset> listDatasets(String catalogName, String contains, boolean idContains) {
        String url = String.format("%1scatalogs/%2s/datasets", this.rootURL, catalogName);
        String json = callAPIWithPagination(url);
        return filterDatasets(responseParser.parseDatasetResponse(json, catalogName), contains, idContains);
    }

    /**
     * List distribution files for a given dataset series and format.
     *
     * @param catalogName   identifier of the catalog to be queried
     * @param dataset       dataset identifier
     * @param seriesMember  series member identifier
     * @param distribution  file format / distribution identifier
     * @param maxResults    maximum number of files to return; if = 0, all are returned
     * @return map of distribution files keyed by identifier
     * @throws APICallException   if the call to the Fusion API fails
     * @throws ParsingException   if the response cannot be parsed
     * @throws OAuthException     if a token could not be retrieved for authentication
     */
    public Map<String, DistributionFile> listDistributionFiles(
            String catalogName, String dataset, String seriesMember, String distribution, int maxResults) {

        String url = String.format(
                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                this.rootURL, catalogName, dataset, seriesMember, distribution);

        String json = this.api.callAPI(url);
        Map<String, DistributionFile> allFiles = responseParser.parseDistributionFilesResponse(json);

        if (maxResults > 0 && allFiles.size() > maxResults) {
            Map<String, DistributionFile> limitedFiles = new LinkedHashMap<>();
            int count = 0;
            for (Map.Entry<String, DistributionFile> entry : allFiles.entrySet()) {
                if (count >= maxResults) {
                    break;
                }
                limitedFiles.put(entry.getKey(), entry.getValue());
                count++;
            }
            return limitedFiles;
        }

        return allFiles;
    }

    /**
     * Get a list of the datasets in the specified catalog
     *
     * @param catalogName identifier of the catalog to be queried
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Dataset> listDatasets(String catalogName) {
        return listDatasets(catalogName, null, false);
    }

    /**
     * Get a list of the datasets in the default catalog
     *
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Dataset> listDatasets() {
        return listDatasets(this.getDefaultCatalog(), null, false);
    }

    /**
     * Get the available resources for a dataset, in the specified catalog
     * Currently this will always return a dataset.
     *
     * @param catalogName identifier of the catalog to be queried
     * @param dataset     a String representing the dataset identifier to query.
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Map<String, Object>> datasetResources(String catalogName, String dataset) {

        String url = String.format("%1scatalogs/%2s/datasets/%3s", this.rootURL, catalogName, dataset);
        return this.callForMap(url);
    }

    /**
     * Get the lineage for a dataset, in the specified catalog
     * Currently this will always return a lineage.
     *
     * @param catalogName identifier of the catalog to be queried
     * @param dataset     a String representing the dataset identifier to query.
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public DatasetLineage getLineage(String catalogName, String dataset) {

        String url = String.format("%1scatalogs/%2s/datasets/%3s/lineage", this.rootURL, catalogName, dataset);
        return responseParser.parseDatasetLineage(this.api.callAPI(url), catalogName);
    }

    /**
     * Get the available resources for a dataset, using the default catalog
     * currently this will always return a datasetseries.
     *
     * @param dataset a String representing the dataset identifier to query.
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Map<String, Object>> datasetResources(String dataset) {

        return this.datasetResources(this.getDefaultCatalog(), dataset);
    }

    /**
     * List the series members for a dataset in a specified catalog
     *
     * @param catalogName identifier of the catalog to be queried
     * @param dataset     a String representing the dataset identifier to query.
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, DatasetSeries> listDatasetMembers(String catalogName, String dataset) {
        String url = String.format("%1scatalogs/%2s/datasets/%3s/datasetseries", this.rootURL, catalogName, dataset);
        String json = callAPIWithPagination(url);
        return responseParser.parseDatasetSeriesResponse(json);
    }

    /**
     * List the series members for a dataset, using the default catalog
     *
     * @param dataset a String representing the dataset identifier to query.
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, DatasetSeries> listDatasetMembers(String dataset) {

        return this.listDatasetMembers(this.getDefaultCatalog(), dataset);
    }

    /**
     * Get the metadata for a dataset series member
     *
     * @param catalogName  identifier of the catalog to be queried
     * @param dataset      a String representing the dataset identifier to query.
     * @param seriesMember a String representing the series member identifier.
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Map<String, Object>> datasetMemberResources(
            String catalogName, String dataset, String seriesMember) {

        String url = String.format(
                "%1scatalogs/%2s/datasets/%3s/datasetseries/%4s", this.rootURL, catalogName, dataset, seriesMember);
        return this.callForMap(url);
    }

    /**
     * Get the metadata for a dataset series member, using the default catalog
     *
     * @param dataset      identifier of the catalog to be queried
     * @param seriesMember a String representing the series member identifier.
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Map<String, Object>> datasetMemberResources(String dataset, String seriesMember) {

        return this.datasetMemberResources(this.getDefaultCatalog(), dataset, seriesMember);
    }

    /**
     * List the attributes for a specified dataset in a defined catalog
     *
     * @param catalogName a String representing the identifier of the catalog to query.
     * @param dataset     a String representing the dataset identifier to query.
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Attribute> listAttributes(String catalogName, String dataset) {
        String url = String.format("%1scatalogs/%2s/datasets/%3s/attributes", this.rootURL, catalogName, dataset);
        String json = callAPIWithPagination(url);
        return responseParser.parseAttributeResponse(json, catalogName, dataset);
    }

    /**
     * List the attributes for a specified dataset, uses the default catalog.
     *
     * @param dataset a String representing the dataset identifier to query.
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Attribute> listAttributes(String dataset) {

        return this.listAttributes(this.getDefaultCatalog(), dataset);
    }

    /**
     * Get the metadata for a dataset, using the specified catalog
     *
     * @param catalogName identifier of the catalog to be queried
     * @param dataset     a String representing the dataset identifier to query.
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Map<String, Object>> attributeResources(String catalogName, String dataset) {
        String url = String.format("%1scatalogs/%2s/datasets/%3s/attributes", this.rootURL, catalogName, dataset);
        return this.callForMap(url);
    }

    /**
     * List the distributions available for a series member, uses the default catalog.
     *
     * @param catalogName  identifier of the catalog to be queried
     * @param dataset      a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Distribution> listDistributions(String catalogName, String dataset, String seriesMember) {
        String url = String.format(
                "%1scatalogs/%2s/datasets/%3s/datasetseries/%4s/distributions",
                this.rootURL, catalogName, dataset, seriesMember);
        String json = callAPIWithPagination(url);
        return responseParser.parseDistributionResponse(json);
    }

    /**
     * List the distributions available for a series member, uses the default catalog.
     *
     * @param dataset      a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Distribution> listDistributions(String dataset, String seriesMember) {

        return this.listDistributions(this.getDefaultCatalog(), dataset, seriesMember);
    }

    /**
     * Download a single distribution to the local filesystem
     *
     * @param catalogName  identifier of the catalog to be queried
     * @param dataset      a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @param distribution a String representing the distribution identifier, this is the file extension.
     * @param path         the absolute file path where the file should be written.
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public void download(String catalogName, String dataset, String seriesMember, String distribution, String path) {
        download(catalogName, dataset, seriesMember, distribution, path, (List<String>) null);
    }

    /**
     * Download a single distribution to the local filesystem
     *
     * @param catalogName  identifier of the catalog to be queried
     * @param dataset      a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @param distribution a String representing the distribution identifier, this is the file extension.
     * @param path         the absolute file path where the file should be written.
     * @param fileNames    a list of file names to download.
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public void download(
            String catalogName,
            String dataset,
            String seriesMember,
            String distribution,
            String path,
            List<String> fileNames) {
        download(catalogName, dataset, seriesMember, distribution, path, new HashMap<>(), fileNames);
    }

    /**
     * Download a single distribution to the local filesystem
     *
     * @param catalogName  identifier of the catalog to be queried
     * @param dataset      a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @param distribution a String representing the distribution identifier, this is the file extension.
     * @param path         the absolute file path where the file should be written.
     * @param headers       http headers to be provided in the request.  For headers with multiple instances, the value should be a csv list
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public void download(
            String catalogName,
            String dataset,
            String seriesMember,
            String distribution,
            String path,
            Map<String, String> headers) {
        download(catalogName, dataset, seriesMember, distribution, path, headers, null);
    }

    /**
     * Download a single distribution to the local filesystem
     *
     * @param catalogName  identifier of the catalog to be queried
     * @param dataset      a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @param distribution a String representing the distribution identifier, this is the file extension.
     * @param path         the absolute file path where the file should be written.
     * @param headers       http headers to be provided in the request.  For headers with multiple instances, the value should be a csv list
     * @param fileNames    a list of file names to download.
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public void download(
            String catalogName,
            String dataset,
            String seriesMember,
            String distribution,
            String path,
            Map<String, String> headers,
            List<String> fileNames) {

        Map<String, DistributionFile> distributionFiles =
                getDistributionFilesForDownload(catalogName, dataset, seriesMember, distribution, fileNames);

        boolean skipChecksumValidationIfMissing = shouldSkipChecksumValidationForDataset(catalogName, dataset);

        try {
            Files.createDirectories(Paths.get(path));
        } catch (Exception e) {
            throw new FusionException("Unable to save to " + path, e);
        }

        for (Map.Entry<String, DistributionFile> entry : distributionFiles.entrySet()) {
            DistributionFile file = entry.getValue();
            String identifier = file.getIdentifier();

            // Use fileExtension from the DistributionFile object, fallback to the distribution parameter if null
            String extension = file.getFileExtension() != null ? file.getFileExtension() : "." + distribution;
            if (!extension.startsWith(".")) {
                extension = "." + extension;
            }

            String safeFileName = (identifier.equals(distribution)
                            ? String.format("%s_%s_%s", catalogName, dataset, seriesMember)
                            : identifier)
                    .replaceAll("[^a-zA-Z0-9_.\\-]", "_");

            String fullPath = path + "/" + safeFileName + extension;

            String url = String.format(
                    "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files/operationType/download",
                    this.rootURL, catalogName, dataset, seriesMember, distribution);

            this.api.callAPIFileDownload(
                    url, fullPath, catalogName, dataset, headers, identifier, skipChecksumValidationIfMissing);
        }
    }

    /**
     * Download a single distribution to the local filesystem. By default, this will write to downloads folder.
     *
     * @param catalogName  identifier of the catalog to be queried
     * @param dataset      a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @param distribution a String representing the distribution identifier, this is the file extension.
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException  if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public void download(String catalogName, String dataset, String seriesMember, String distribution) {
        this.download(catalogName, dataset, seriesMember, distribution, getDefaultPath(), new HashMap<>(), null);
    }

    /**
     * Download a single distribution to the local filesystem. By default, this will write to downloads folder.
     *
     * @param catalogName  identifier of the catalog to be queried
     * @param dataset      a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @param distribution a String representing the distribution identifier, this is the file extension.
     * @param fileNames    a list of file names to download.
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException  if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public void download(
            String catalogName, String dataset, String seriesMember, String distribution, List<String> fileNames) {
        this.download(catalogName, dataset, seriesMember, distribution, getDefaultPath(), new HashMap<>(), fileNames);
    }

    /**
     * Download a single distribution to the local filesystem. By default, this will write to downloads folder.
     *
     * @param catalogName  identifier of the catalog to be queried
     * @param dataset      a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @param distribution a String representing the distribution identifier, this is the file extension.
     * @param headers       http headers to be provided in the request.  For headers with multiple instances, the value should be a csv list
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException  if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public void download(
            String catalogName, String dataset, String seriesMember, String distribution, Map<String, String> headers) {
        download(catalogName, dataset, seriesMember, distribution, headers, null);
    }

    /**
     * Download a single distribution to the local filesystem. By default, this will write to downloads folder.
     *
     * @param catalogName  identifier of the catalog to be queried
     * @param dataset      a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @param distribution a String representing the distribution identifier, this is the file extension.
     * @param headers       http headers to be provided in the request.  For headers with multiple instances, the value should be a csv list
     * @param fileNames    a list of file names to download.
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException  if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public void download(
            String catalogName,
            String dataset,
            String seriesMember,
            String distribution,
            Map<String, String> headers,
            List<String> fileNames) {
        this.download(catalogName, dataset, seriesMember, distribution, getDefaultPath(), fileNames);
    }

    /**
     * Download multiple distribution to the local filesystem. By default, this will write to downloads folder.
     * Not implemented.
     *
     * @param catalogName   identifier of the catalog to be queried
     * @param dataset       a String representing the dataset identifier to download.
     * @param seriesMembers a List of Strings representing the series member identifiers.
     * @param distribution  a String representing the distribution identifier, this is the file extension.
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException  if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public void download(String catalogName, String dataset, List<String> seriesMembers, String distribution) {
        throw new FusionException("Functionality not yet implemented");
    }

    /**
     * Download a single distribution and return the data as an InputStream
     * Note that users of this method are required to close the returned InputStream. Failure to do so will
     * result in resource leaks, including the Http connection used to communicate with Fusion
     *
     * @param catalogName  identifier of the catalog to be queried
     * @param dataset      a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @param distribution a String representing the distribution identifier, this is the file extension.
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, InputStream> downloadStream(
            String catalogName, String dataset, String seriesMember, String distribution) {
        return downloadStream(catalogName, dataset, seriesMember, distribution, (List<String>) null);
    }

    /**
     * Download a single distribution and return the data as an InputStream
     * Note that users of this method are required to close the returned InputStream. Failure to do so will
     * result in resource leaks, including the Http connection used to communicate with Fusion
     *
     * @param catalogName  identifier of the catalog to be queried
     * @param dataset      a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @param distribution a String representing the distribution identifier, this is the file extension.
     * @param fileNames    a list of file names to download.
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, InputStream> downloadStream(
            String catalogName, String dataset, String seriesMember, String distribution, List<String> fileNames) {
        return downloadStream(catalogName, dataset, seriesMember, distribution, new HashMap<>(), fileNames);
    }

    public Map<String, InputStream> downloadStream(
            String catalogName, String dataset, String seriesMember, String distribution, Map<String, String> headers) {
        return downloadStream(catalogName, dataset, seriesMember, distribution, headers, null);
    }

    public Map<String, InputStream> downloadStream(
            String catalogName,
            String dataset,
            String seriesMember,
            String distribution,
            Map<String, String> headers,
            List<String> fileNames) {

        Map<String, DistributionFile> distributionFiles =
                getDistributionFilesForDownload(catalogName, dataset, seriesMember, distribution, fileNames);

        boolean skipChecksumValidationIfMissing = shouldSkipChecksumValidationForDataset(catalogName, dataset);

        Map<String, InputStream> result = new HashMap<>();

        for (Map.Entry<String, DistributionFile> entry : distributionFiles.entrySet()) {
            DistributionFile file = entry.getValue();
            String identifier = file.getIdentifier();

            if (identifier == null || identifier.trim().isEmpty()) {
                continue;
            }

            String url = String.format(
                    "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files/operationType/download",
                    this.rootURL, catalogName, dataset, seriesMember, distribution);

            InputStream stream = this.api.callAPIFileDownload(
                    url, catalogName, dataset, headers, identifier, skipChecksumValidationIfMissing);
            result.put(identifier, stream);
        }

        return result;
    }

    /**
     * Private helper method to fetch and filter distribution files for download operations.
     * This validates that the requested files exist and returns the DistributionFile objects.
     *
     * @param catalogName  identifier of the catalog
     * @param dataset      the dataset identifier
     * @param seriesMember the series member identifier
     * @param distribution the distribution identifier
     * @param fileNames    optional list of specific file names to filter for (null or empty means all files)
     * @return Map of DistributionFile objects keyed by identifier
     * @throws FusionException if no files are found or if requested files don't exist
     */
    private Map<String, DistributionFile> getDistributionFilesForDownload(
            String catalogName, String dataset, String seriesMember, String distribution, List<String> fileNames) {

        Map<String, DistributionFile> distributionFiles =
                listDistributionFiles(catalogName, dataset, seriesMember, distribution, 0);

        if (distributionFiles == null || distributionFiles.isEmpty()) {
            throw new FusionException(String.format(
                    "No files found to download for catalog=%s, dataset=%s, series=%s, distribution=%s",
                    catalogName, dataset, seriesMember, distribution));
        }

        // Filter to only include requested files if fileNames is provided
        if (fileNames != null && !fileNames.isEmpty()) {
            // First, check if all requested files exist
            List<String> missingFiles = new ArrayList<>();
            for (String fileName : fileNames) {
                if (!distributionFiles.containsKey(fileName)) {
                    missingFiles.add(fileName);
                }
            }

            // If any requested files don't exist, throw an exception before starting download
            if (!missingFiles.isEmpty()) {
                throw new FusionException(String.format(
                        "The following requested files do not exist in catalog=%s, dataset=%s, series=%s, distribution=%s: %s",
                        catalogName, dataset, seriesMember, distribution, String.join(", ", missingFiles)));
            }

            // All requested files exist, so filter to only include them
            Map<String, DistributionFile> filtered = new LinkedHashMap<>();
            for (String fileName : fileNames) {
                filtered.put(fileName, distributionFiles.get(fileName));
            }
            distributionFiles = filtered;
        }

        return distributionFiles;
    }

    private boolean shouldSkipChecksumValidationForDataset(String catalogName, String dataset) {
        try {
            Dataset datasetMetadata = getDataset(catalogName, dataset);
            Object deliveryChannel = Objects.nonNull(datasetMetadata)
                    ? datasetMetadata.getVarArgs().get("deliveryChannel")
                    : null;

            if (deliveryChannel instanceof Iterable<?>) {
                for (Object channel : (Iterable<?>) deliveryChannel) {
                    if (Objects.nonNull(channel) && "glue".equalsIgnoreCase(channel.toString())) return true;
                }
            }

            return false;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private Dataset getDataset(String catalogName, String dataset) {
        String url = String.format("%scatalogs/%s/datasets/%s", this.rootURL, catalogName, dataset);
        String json = this.api.callAPI(url);
        if (Objects.isNull(json)) {
            return null;
        }

        Map<String, Dataset> datasets =
                responseParser.parseDatasetResponse(String.format("{\"resources\":[%s]}", json), catalogName);
        return datasets != null && datasets.size() == 1
                ? datasets.values().iterator().next()
                : null;
    }

    /**
     * Upload a new dataset series member to a catalog.
     *
     * @param catalogName  identifier of the catalog to upload data into
     * @param dataset      the dataset identifier to upload against.
     * @param seriesMember the series member identifier to add or replace
     * @param distribution the distribution identifier, this is the file extension.
     * @param filename     a path to the file containing the data to upload
     * @param fromDate     the earliest date for which there is data in the distribution
     * @param toDate       the latest date for which there is data in the distribution
     * @param createdDate  the creation date for the distribution
     * @param headers      http headers to be provided in the request.  For the headers with multiple instances, the value should be a comm-separated list
     * @throws ApiInputValidationException if the specified file cannot be read
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileUploadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     **/
    public void upload(
            String catalogName,
            String dataset,
            String seriesMember,
            String distribution,
            String filename,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate createdDate,
            Map<String, String> headers) {

        String url = String.format(
                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                this.rootURL, catalogName, dataset, seriesMember, distribution);
        String strFromDate = fromDate.format(dateTimeFormatter);
        String strToDate = toDate.format(dateTimeFormatter);
        String strCreatedDate = createdDate.format(dateTimeFormatter);
        this.api.callAPIFileUpload(
                url, filename, catalogName, dataset, strFromDate, strToDate, strCreatedDate, headers);
    }

    /**
     * Upload a new dataset series member to a catalog.
     *
     * @param catalogName  identifier of the catalog to upload data into
     * @param dataset      the dataset identifier to upload against.
     * @param seriesMember the series member identifier to add or replace
     * @param distribution the distribution identifier, this is the file extension.
     * @param filename     a path to the file containing the data to upload
     * @param fromDate     the earliest date for which there is data in the distribution
     * @param toDate       the latest date for which there is data in the distribution
     * @param createdDate  the creation date for the distribution
     * @throws ApiInputValidationException if the specified file cannot be read
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileUploadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     **/
    public void upload(
            String catalogName,
            String dataset,
            String seriesMember,
            String distribution,
            String filename,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate createdDate) {
        this.upload(
                catalogName,
                dataset,
                seriesMember,
                distribution,
                filename,
                fromDate,
                toDate,
                createdDate,
                new HashMap<>());
    }

    /**
     * Upload a new dataset series member to a catalog.
     *
     * @param catalogName  identifier of the catalog to upload data into
     * @param dataset      the dataset identifier to upload against.
     * @param seriesMember the series member identifier to add or replace
     * @param distribution the distribution identifier, this is the file extension.
     * @param filename     a path to the file containing the data to upload
     * @param dataDate     the earliest, latest, and created date are all the same.
     * @throws ApiInputValidationException if the specified file cannot be read
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileUploadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     **/
    public void upload(
            String catalogName,
            String dataset,
            String seriesMember,
            String distribution,
            String filename,
            LocalDate dataDate) {
        this.upload(
                catalogName,
                dataset,
                seriesMember,
                distribution,
                filename,
                dataDate,
                dataDate,
                dataDate,
                new HashMap<>());
    }

    /**
     * Upload a new dataset series member to a catalog.
     *
     * @param catalogName  identifier of the catalog to upload data into
     * @param dataset      the dataset identifier to upload against.
     * @param seriesMember the series member identifier to add or replace
     * @param distribution the distribution identifier, this is the file extension.
     * @param filename     a path to the file containing the data to upload
     * @param dataDate     the earliest, latest, and created date are all the same.
     * @param headers      http headers to be provided in the request.  For the headers with multiple instances, the value should be a comm-separated list
     * @throws ApiInputValidationException if the specified file cannot be read
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileUploadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     **/
    public void upload(
            String catalogName,
            String dataset,
            String seriesMember,
            String distribution,
            String filename,
            LocalDate dataDate,
            Map<String, String> headers) {
        this.upload(catalogName, dataset, seriesMember, distribution, filename, dataDate, dataDate, dataDate, headers);
    }

    /**
     * Upload a new dataset series member to a catalog.
     *
     * @param catalogName  identifier of the catalog to upload data into
     * @param dataset      the dataset identifier to upload against.
     * @param seriesMember the series member identifier to add or replace
     * @param distribution the distribution identifier, this is the file extension.
     * @param data         am InputStream of the data to be uploaded
     * @param fromDate     the earliest date for which there is data in the distribution
     * @param toDate       the latest date for which there is data in the distribution
     * @param createdDate  the creation date for the distribution
     * @param headers      http headers to be provided in the request.  For the headers with multiple instances, the value should be a comm-separated list
     * @throws ApiInputValidationException if the specified stream cannot be read
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileUploadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     **/
    public void upload(
            String catalogName,
            String dataset,
            String seriesMember,
            String distribution,
            InputStream data,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate createdDate,
            Map<String, String> headers) {

        String url = String.format(
                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                this.rootURL, catalogName, dataset, seriesMember, distribution);
        String strFromDate = fromDate.format(dateTimeFormatter);
        String strToDate = toDate.format(dateTimeFormatter);
        String strCreatedDate = createdDate.format(dateTimeFormatter);
        this.api.callAPIFileUpload(url, data, catalogName, dataset, strFromDate, strToDate, strCreatedDate, headers);
    }

    /**
     * Upload a new dataset series member to a catalog.
     *
     * @param catalogName  identifier of the catalog to upload data into
     * @param dataset      the dataset identifier to upload against.
     * @param seriesMember the series member identifier to add or replace
     * @param distribution the distribution identifier, this is the file extension.
     * @param data         am InputStream of the data to be uploaded
     * @param fromDate     the earliest date for which there is data in the distribution
     * @param toDate       the latest date for which there is data in the distribution
     * @param createdDate  the creation date for the distribution
     * @throws ApiInputValidationException if the specified stream cannot be read
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileUploadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     **/
    public void upload(
            String catalogName,
            String dataset,
            String seriesMember,
            String distribution,
            InputStream data,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate createdDate) {
        this.upload(
                catalogName, dataset, seriesMember, distribution, data, fromDate, toDate, createdDate, new HashMap<>());
    }

    /**
     * Upload a new dataset series member to a catalog.
     *
     * @param catalogName  identifier of the catalog to upload data into
     * @param dataset      the dataset identifier to upload against.
     * @param seriesMember the series member identifier to add or replace
     * @param distribution the distribution identifier, this is the file extension.
     * @param data         am InputStream of the data to be uploaded
     * @param dataDate     the earliest, latest, and created date are all the same.
     * @throws ApiInputValidationException if the specified stream cannot be read
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileUploadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     **/
    public void upload(
            String catalogName,
            String dataset,
            String seriesMember,
            String distribution,
            InputStream data,
            LocalDate dataDate) {
        this.upload(
                catalogName, dataset, seriesMember, distribution, data, dataDate, dataDate, dataDate, new HashMap<>());
    }

    /**
     * Upload a new dataset series member to a catalog.
     *
     * @param catalogName  identifier of the catalog to upload data into
     * @param dataset      the dataset identifier to upload against.
     * @param seriesMember the series member identifier to add or replace
     * @param distribution the distribution identifier, this is the file extension.
     * @param data         am InputStream of the data to be uploaded
     * @param dataDate     the earliest, latest, and created date are all the same.
     * @param headers      http headers to be provided in the request.  For the headers with multiple instances, the value should be a comm-separated list
     * @throws ApiInputValidationException if the specified stream cannot be read
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileUploadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     **/
    public void upload(
            String catalogName,
            String dataset,
            String seriesMember,
            String distribution,
            InputStream data,
            LocalDate dataDate,
            Map<String, String> headers) {
        this.upload(catalogName, dataset, seriesMember, distribution, data, dataDate, dataDate, dataDate, headers);
    }

    /**
     * Returns a builder for creating a {@link io.github.jpmorganchase.fusion.model.Dataset} object.
     * The builder can be used to set properties and then create or update an instance of {@link io.github.jpmorganchase.fusion.model.Dataset}.
     *
     * @return {@link Builders} The object that provides access to specific model builders for different types of datasets.
     */
    public Builders builders() {
        return this.builders;
    }

    public static FusionBuilder builder() {
        return new CustomFusionBuilder();
    }

    public static class FusionBuilder {

        protected Client client;
        protected APIManager api;
        protected Builders builders;
        protected String rootURL;
        protected String defaultCatalog;
        protected String defaultPath;
        protected FusionTokenProvider fusionTokenProvider;
        protected Credentials credentials;
        protected FusionConfiguration configuration =
                FusionConfiguration.builder().build();

        protected APIResponseParser responseParser;

        public FusionBuilder configuration(FusionConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public FusionBuilder fusionTokenProvider(FusionTokenProvider fusionTokenProvider) {
            this.fusionTokenProvider = fusionTokenProvider;
            return this;
        }

        public FusionBuilder secretBasedCredentials(
                String clientId, String clientSecret, String resource, String authServerUrl) {
            this.credentials = new OAuthSecretBasedCredentials(clientId, resource, authServerUrl, clientSecret);
            return this;
        }

        public FusionBuilder passwordBasedCredentials(
                String clientId, String username, String password, String resource, String authServerUrl) {
            this.credentials = new OAuthPasswordBasedCredentials(clientId, resource, authServerUrl, username, password);
            return this;
        }

        public FusionBuilder credentials(Credentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public FusionBuilder bearerToken(String token) {
            this.credentials = new BearerTokenCredentials(token);
            return this;
        }

        public FusionBuilder proxy(String url, int port) {
            client = JdkClient.builder().url(url).port(port).build();
            return this;
        }

        private FusionBuilder rootURL(String rootURL) {
            return this;
        }

        private FusionBuilder defaultCatalog(String defaultCatalog) {
            return this;
        }

        private FusionBuilder defaultPath(String defaultPath) {
            return this;
        }
    }

    private static class CustomFusionBuilder extends FusionBuilder {

        @Override
        public Fusion build() {

            this.rootURL = configuration.getRootURL();
            this.defaultCatalog = configuration.getDefaultCatalog();
            this.defaultPath = configuration.getDownloadPath();

            if (Objects.isNull(client)) {
                client = JdkClient.builder().noProxy().build();
            }

            if (Objects.isNull(fusionTokenProvider)) {
                fusionTokenProvider = DefaultFusionTokenProvider.builder()
                        .configuration(configuration)
                        .credentials(credentials)
                        .client(client)
                        .build();
            }

            if (Objects.isNull(api)) {
                api = FusionAPIManager.builder()
                        .httpClient(client)
                        .tokenProvider(fusionTokenProvider)
                        .configuration(configuration)
                        .build();
            }

            return super.build();
        }
    }
}
