package io.github.jpmorganchase.fusion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.jpmorganchase.fusion.api.APICallException;
import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.api.ApiInputValidationException;
import io.github.jpmorganchase.fusion.api.FusionAPIManager;
import io.github.jpmorganchase.fusion.digest.AlgoSpecificDigestProducer;
import io.github.jpmorganchase.fusion.digest.DigestProducer;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.JdkClient;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.oauth.credential.*;
import io.github.jpmorganchase.fusion.oauth.exception.OAuthException;
import io.github.jpmorganchase.fusion.oauth.provider.OAuthDatasetTokenProvider;
import io.github.jpmorganchase.fusion.oauth.provider.OAuthSessionTokenProvider;
import io.github.jpmorganchase.fusion.parsing.APIResponseParser;
import io.github.jpmorganchase.fusion.parsing.GsonAPIResponseParser;
import io.github.jpmorganchase.fusion.parsing.ParsingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

/**
 * Class representing the Fusion API, providing methods that correspond to available API endpoints
 */
@Builder
public class Fusion {

    /*
       Public facing constant default values
    */
    public static final String DEFAULT_ROOT_URL = "https://fusion-api.jpmorgan.com/fusion/v1/";
    public static final String DEFAULT_CATALOG = "common";

    private static final String DEFAULT_CREDENTIALS_FILE = "config/client_credentials.json";
    private static final String DEFAULT_PATH = "downloads";
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private APIManager api;

    @Builder.Default
    private String defaultCatalog = DEFAULT_CATALOG;

    @Builder.Default
    @Getter(value = AccessLevel.PACKAGE)
    private String rootURL = DEFAULT_ROOT_URL;

    @Getter(value = AccessLevel.PACKAGE)
    private Credentials credentials;

    @Builder.Default
    private APIResponseParser responseParser = new GsonAPIResponseParser();

    /**
     * Get the default catalog identifier - this will be common unless overridden.
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
     * Get a list of the catalogs available to the API account.
     *
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public Map<String, Catalog> listCatalogs() {
        String json = this.api.callAPI(rootURL.concat("catalogs"));
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
     * Note that as of current version this search capability is not yet implemented
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
        String json = this.api.callAPI(url);
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
     * Note that as of current version this search capability is not yet implemented
     *
     * @param catalogName identifier of the catalog to be queried
     * @param contains    a search keyword.
     * @param idContains  is true if only apply the filter to the identifier
     * @throws APICallException if the call to the Fusion API fails
     * @throws ParsingException if the response from Fusion could not be parsed successfully
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    // TODO: Search parameters
    public Map<String, Dataset> listDatasets(String catalogName, String contains, boolean idContains) {
        String url = String.format("%1scatalogs/%2s/datasets", this.rootURL, catalogName);
        String json = this.api.callAPI(url);
        return responseParser.parseDatasetResponse(json);
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
     * Currently this will always return a datasetseries.
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
     * Get the available resources for a dataset, using the default catalog
     * Currently this will always return a datasetseries.
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
        String json = this.api.callAPI(url);
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
        String json = this.api.callAPI(url);
        return responseParser.parseAttributeResponse(json);
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
        String json = this.api.callAPI(url);
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
     * @throws FusionException  if the downloaded file cannot be saved to the specified target path
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public void download(String catalogName, String dataset, String seriesMember, String distribution, String path) {

        String url = String.format(
                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                this.rootURL, catalogName, dataset, seriesMember, distribution);
        try {
            Files.createDirectories(Paths.get(path));
        } catch (InvalidPathException | IOException e) {
            throw new FusionException(String.format("Unable to save to target path %s", path), e);
        }
        String filepath = String.format("%s/%s_%s_%s.%s", path, catalogName, dataset, seriesMember, distribution);
        this.api.callAPIFileDownload(url, filepath);
    }

    /**
     * Download a single distribution to the local filesystem. By default, this will write to downloads folder.
     *
     * @param catalogName  identifier of the catalog to be queried
     * @param dataset      a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @param distribution a String representing the distribution identifier, this is the file extension.
     * @throws APICallException if the call to the Fusion API fails
     * @throws FusionException  if the downloaded file cannot be saved to the default path
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public void download(String catalogName, String dataset, String seriesMember, String distribution) {
        this.download(catalogName, dataset, seriesMember, distribution, Fusion.DEFAULT_PATH);
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
     * @throws FusionException  if the downloaded file cannot be saved to the default path
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
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    public InputStream downloadStream(String catalogName, String dataset, String seriesMember, String distribution) {
        String url = String.format(
                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                this.rootURL, catalogName, dataset, seriesMember, distribution);
        return this.api.callAPIFileDownload(url);
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

        String url = String.format(
                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                this.rootURL, catalogName, dataset, seriesMember, distribution);
        String strFromDate = fromDate.format(dateTimeFormatter);
        String strToDate = toDate.format(dateTimeFormatter);
        String strCreatedDate = createdDate.format(dateTimeFormatter);
        this.api.callAPIFileUpload(url, filename, catalogName, dataset, strFromDate, strToDate, strCreatedDate);
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
     * @throws OAuthException if a token could not be retrieved for authentication
     **/
    public void upload(
            String catalogName,
            String dataset,
            String seriesMember,
            String distribution,
            String filename,
            LocalDate dataDate) {
        this.upload(catalogName, dataset, seriesMember, distribution, filename, dataDate, dataDate, dataDate);
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
     * @throws ApiInputValidationException if the specified file cannot be read
     * @throws APICallException if the call to the Fusion API fails
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

        String url = String.format(
                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                this.rootURL, catalogName, dataset, seriesMember, distribution);
        String strFromDate = fromDate.format(dateTimeFormatter);
        String strToDate = toDate.format(dateTimeFormatter);
        String strCreatedDate = createdDate.format(dateTimeFormatter);
        this.api.callAPIFileUpload(url, data, catalogName, dataset, strFromDate, strToDate, strCreatedDate);
    }

    public static FusionBuilder builder() {
        return new CustomFusionBuilder();
    }

    public static class FusionBuilder {
        // Implementation of helper methods to allow for simpler instantiation. Note that Lombok will fill in the
        // missing, standard builder methods
        protected Credentials credentials;
        protected OAuthCredentials oAuthCredentials;
        protected Client client;
        protected String credentialFile;
        protected String rootUrl;
        protected APIManager api;

        public FusionBuilder bearerToken(String token) {
            this.credentials = new BearerTokenCredentials(token);
            return this;
        }

        public FusionBuilder secretBasedCredentials(
                String clientId, String clientSecret, String resource, String authServerUrl) {
            this.oAuthCredentials = new OAuthSecretBasedCredentials(clientId, resource, authServerUrl, clientSecret);
            return this;
        }

        public FusionBuilder passwordBasedCredentials(
                String clientId, String username, String password, String resource, String authServerUrl) {
            this.oAuthCredentials =
                    new OAuthPasswordBasedCredentials(clientId, resource, authServerUrl, username, password);
            return this;
        }

        public FusionBuilder proxy(String url, int port) {
            client = new JdkClient(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(url, port)));
            return this;
        }

        public FusionBuilder credentialFile() {
            return this.credentialFile(DEFAULT_CREDENTIALS_FILE);
        }

        public FusionBuilder credentialFile(String credentialFile) {
            this.credentialFile = credentialFile;
            return this;
        }

        public FusionBuilder rootUrl(String rootUrl) {
            this.rootUrl = rootUrl;
            return this;
        }
    }

    private static class CustomFusionBuilder extends FusionBuilder {

        @Override
        public Fusion build() {

            if (client == null) {
                client = new JdkClient();
            }

            if (credentialFile != null) {
                Gson gson = new GsonBuilder().create();
                try {
                    // Java 8 doesn't allow specification of the charset if we use a FileReader
                    InputStreamReader fileReader = new InputStreamReader(
                            Files.newInputStream(Paths.get(credentialFile)), StandardCharsets.UTF_8);
                    oAuthCredentials = gson.fromJson(fileReader, OAuthSecretBasedCredentials.class);
                    fileReader.close();
                } catch (IOException e) {
                    throw new FusionInitialisationException(
                            String.format("Failed to load credential file from path: %s", credentialFile), e);
                }
            }

            if (oAuthCredentials != null) {
                if (oAuthCredentials instanceof OAuthSecretBasedCredentials) {
                    credentials = oAuthCredentials;
                } else if (oAuthCredentials instanceof OAuthPasswordBasedCredentials) {
                    credentials = oAuthCredentials;
                }
            }

            if (credentials == null) {
                throw new FusionInitialisationException("No Fusion credentials provided, cannot build Fusion instance");
            }

            // TODO : Make this part of the builder journey
            OAuthSessionTokenProvider sessionTokenProvider = new OAuthSessionTokenProvider(credentials, client);
            OAuthDatasetTokenProvider datasetTokenProvider =
                    new OAuthDatasetTokenProvider(rootUrl, sessionTokenProvider, client);
            DigestProducer digestProducer =
                    AlgoSpecificDigestProducer.builder().sha256().build();

            if (api == null) {
                api = new FusionAPIManager(client, sessionTokenProvider, datasetTokenProvider, digestProducer);
            }

            return super.build();
        }
    }
}
