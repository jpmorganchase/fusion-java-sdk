package com.jpmorganchase.fusion;

import com.jpmorganchase.fusion.credential.BearerTokenCredentials;
import com.jpmorganchase.fusion.credential.FusionCredentials;
import com.jpmorganchase.fusion.credential.OAuthPasswordBasedCredentials;
import com.jpmorganchase.fusion.credential.OAuthSecretBasedCredentials;
import com.jpmorganchase.fusion.http.Client;
import com.jpmorganchase.fusion.http.JdkClient;
import com.jpmorganchase.fusion.model.*;
import com.jpmorganchase.fusion.parsing.GsonAPIResponseParser;
import com.jpmorganchase.fusion.parsing.APIResponseParser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
* Class representing the Fusion API, providing methods that correspond to available API endpoints
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fusion {

    /*
        Public facing constant default values
     */
    public static final String DEFAULT_ROOT_URL = "https://fusion-api.jpmorgan.com/fusion/v1/";
    public static final String DEFAULT_CATALOG = "common";

    private static final String DEFAULT_CREDENTIALS_FILE = "config/client_credentials.json";
    private static final String DEFAULT_PATH = "downloads";

    private IFusionAPIManager api;
    @Builder.Default
    private String defaultCatalog = DEFAULT_CATALOG;
    @Builder.Default
    private String rootURL = DEFAULT_ROOT_URL;
    @Builder.Default
    private Client httpClient = new JdkClient();

    /**
     * Object members
     */
    private FusionCredentials credentials;

    @Builder.Default
    private APIResponseParser responseParser = new GsonAPIResponseParser();

    /**
     * Constructor
     * @param myCredentials a populated credentials object
     * @param rootURL override the API URL root
     */
    /*public Fusion(FusionCredentials myCredentials, String rootURL){
        this.credentials = myCredentials;
        this.rootURL = rootURL;
        api = new FusionAPIManager(this.credentials);
    }*/

    /**
     * Constructor that uses the default base URL
     * @param myCredentials a populated credentials object
     */
   /* public Fusion(FusionCredentials myCredentials){
        this(myCredentials, DEFAULT_ROOT_URL);
    }*/

    /**
     * The constructor will read the API credentials from file and connect to the API
     * @param credentialsFile a path to a credentials file
     */
    //TODO: Reimplement this
    /*public Fusion(String credentialsFile) throws IOException {
        this(FusionCredentials.readCredentialsFile(credentialsFile), ROOT_URL);
        api = FusionAPIManager.getAPIManager(credentials);
    }*/

    /**
     * The constructor will read the API credentials from file and connect to the API
     * @param credentialsFile a path to a credentials file
     * @param rootURL override the API URL root
     */
    //TODO: Re-implement this
    /*public Fusion(String credentialsFile, String rootURL) throws IOException{
        this(FusionCredentials.readCredentialsFile(credentialsFile), rootURL);
        api = FusionAPIManager.getAPIManager(credentials);
    }*/

    /**
     * The constructor will read the API credentials from file and connect to the API
     */
    //TODO: Re-implement this
    /*public Fusion() throws IOException {
        this(FusionCredentials.readCredentialsFile(DEFAULT_CREDENTIALS_FILE), ROOT_URL);
        api = FusionAPIManager.getAPIManager(credentials);
    }*/


    /**
     * Get the default catalog identifier - this will be common unless overridden.
     */
    public String getDefaultCatalog(){
        return this.defaultCatalog;
    }

    /**
     * Set the default catalog. This allows the default context to be a specified catalog.
     * @param newDefaultCatalogName the identifier of the catalog to set as a the default
     */
    public void setDefaultCatalog(String newDefaultCatalogName){
        this.defaultCatalog = newDefaultCatalogName;
    }

    /**
     * Call the API and returns a data table structure containing the results of the API call in tabular form
     * @param url the API url to call
     * @return a map from the returned json object
     */
    private Map<String, Map> callForMap(String url) throws Exception{
        /*String json = this.api.callAPI(url);
        return CatalogResource.metadataAttributes(json);*/
        //TODO: implement this behaviour
        throw new RuntimeException("Not implemented yet");
    }

    /**
     * Get a list of the catalogs available to the API account.
     */
    public Map<String, Catalog> listCatalogs() throws Exception {
        String json = this.api.callAPI(rootURL.concat("catalogs/"));
        return responseParser.parseCatalogResponse(json);
    }

    /**
     * Return the resources available for a specified catalog
     * @param catalogName currently this will return only products and datasets
     */
    public Map catalogResources(String catalogName) throws Exception{

        String url = String.format("%1scatalogs/%2s",this.rootURL, catalogName);
        return this.callForMap(url);
    }


    /**
     * Get a list of the data products in the default catalog
     * @param catalogName a String representing the identifier of the catalog to query.
     * @param contains a search keyword.
     * @param idContains is true if only apply the filter to the identifier
     */
    public Map<String, DataProduct> listProducts(String catalogName, String contains, boolean idContains) throws Exception{
        //TODO: unimplemented logic implied by the method parameters
        String url = String.format("%1scatalogs/%2s/products",this.rootURL, catalogName);
        String json = this.api.callAPI(url);
        return responseParser.parseDataProductResponse(json);
    }

    /**
     * Get a list of the data products in the default catalog
     * @param catalogName a String representing the identifier of the catalog to query.
     */
    public Map<String, DataProduct> listProducts(String catalogName) throws Exception {

        return listProducts(catalogName, null, false);

    }

    /**
     * Get a list of the data products in the default catalog
     */
    public Map<String,DataProduct> listProducts() throws Exception{
        return listProducts(this.getDefaultCatalog(), null, false);
    }


    /**
     * Get a filtered list of the datasets in the specified catalog
     * @param catalogName a String representing the identifier of the catalog to query.
     * @param contains a search keyword.
     * @param idContains is true if only apply the filter to the identifier
     */
    //TODO: Search parameters
    public Map<String, Dataset> listDatasets(String catalogName, String contains, boolean idContains) throws Exception{
        String url = String.format("%1scatalogs/%2s/datasets",this.rootURL, catalogName);
        String json = this.api.callAPI(url);
        return responseParser.parseDatasetResponse(json);
    }

    /**
     * Get a list of the datasets in the specified catalog
     * @param catalogName a String representing the identifier of the catalog to query.
     */
    public Map<String, Dataset>  listDatasets(String catalogName) throws Exception{
        return listDatasets(catalogName, null, false);
    }

    /**
     * Get a list of the datasets in the default catalog
     */
    public Map<String, Dataset>  listDatasets() throws Exception{
        return listDatasets(this.getDefaultCatalog(), null, false);
    }


    /**
     * Get the available resources for a dataset, in the specified catalog
     * Currently this will always return a datasetseries.
     * @param catalogName a String representing the identifier of the catalog to query.
     * @param dataset a String representing the dataset identifier to query.
     */
    public Map datasetResources(String catalogName, String dataset) throws Exception{

        String url = String.format("%1scatalogs/%2s/datasets/%3s",this.rootURL, catalogName, dataset);
        return this.callForMap(url);
    }

    /**
     * Get the available resources for a dataset, using the default catalog
     * Currently this will always return a datasetseries.
     * @param dataset a String representing the dataset identifier to query.
     */
    public Map datasetResources(String dataset) throws Exception{

        return this.datasetResources(this.getDefaultCatalog(), dataset);
    }

    /**
     * List the series members for a dataset in a specified catalog
     * @param catalogName a String representing the identifier of the catalog to query.
     * @param dataset a String representing the dataset identifier to query.
     */
    public Map<String, DatasetSeries> listDatasetMembers(String catalogName, String dataset) throws Exception{
        String url = String.format("%1scatalogs/%2s/datasets/%3s/datasetseries",this.rootURL, catalogName, dataset);
        String json = this.api.callAPI(url);
        return responseParser.parseDatasetSeriesResponse(json);
    }

    /**
     * List the series members for a dataset, using the default catalog
     * @param dataset a String representing the dataset identifier to query.
     */
    public Map listDatasetMembers(String dataset) throws Exception{

        return this.listDatasetMembers(this.getDefaultCatalog(), dataset);
    }

    /**
     * Get the metadata for a dataset series member
     * @param catalogName a String representing the identifier of the catalog to query.
     * @param dataset a String representing the dataset identifier to query.
     * @param seriesMember a String representing the series member identifier.
     */
    public Map datasetMemberResources(String catalogName, String dataset, String seriesMember) throws Exception{

        String url = String.format("%1scatalogs/%2s/datasets/%3s/datasetseries/%4s",this.rootURL, catalogName, dataset, seriesMember);
        return this.callForMap(url);
    }

    /**
     * Get the metadata for a dataset series member, using the default catalog
     * @param dataset a String representing the dataset identifier to query.
     * @param seriesMember a String representing the series member identifier.
     */
    public Map datasetMemberResources(String dataset, String seriesMember) throws Exception{

        return this.datasetMemberResources(this.getDefaultCatalog(), dataset, seriesMember);
    }

    /**
     * List the attributes for a specified dataset in a defined catalog
     * @param catalogName a String representing the identifier of the catalog to query.
     * @param dataset a String representing the dataset identifier to query.
     */
    public Map<String, Attribute> listAttributes(String catalogName, String dataset) throws Exception{
        String url = String.format("%1scatalogs/%2s/datasets/%3s/attributes",this.rootURL, catalogName,dataset);
        String json = this.api.callAPI(url);
        return responseParser.parseAttributeResponse(json);
    }

    /**
     * List the attributes for a specified dataset, uses the default catalog.
     * @param dataset a String representing the dataset identifier to query.
     */
    public Map<String, Attribute> listAttributes(String dataset) throws Exception{

        return this.listAttributes(this.getDefaultCatalog(), dataset);

    }

    /**
     * List the distributions available for a series member, uses the default catalog.
     * @param catalogName a String representing the identifier of the catalog to query.
     * @param dataset a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     */
    public Map<String, Distribution> listDistributions(String catalogName, String dataset, String seriesMember) throws Exception{

        String url = String.format("%1scatalogs/%2s/datasets/%3s/datasetseries/%4s/distributions",
                this.rootURL, catalogName,dataset, seriesMember);
        String json = this.api.callAPI(url);
        return responseParser.parseDistributionResponse(json);
    }

    /**
     * List the distributions available for a series member, uses the default catalog.
     * @param dataset a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     */
    public Map<String, Distribution> listDistributions(String dataset, String seriesMember) throws Exception{

        return this.listDistributions(this.getDefaultCatalog(), dataset, seriesMember);
    }

    /**
     * Download a single distribution to the local filesystem
     * @param catalogName a String representing the identifier of the catalog to download from
     * @param dataset a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @param distribution a String representing the distribution identifier, this is the file extension.
     * @param path the absolute file path where the file should be written.
     */
    public int download(String catalogName, String dataset, String seriesMember, String distribution, String path) throws Exception{

        String url = String.format("%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",this.rootURL, catalogName,dataset, seriesMember,distribution);
        Files.createDirectories(Paths.get(path));
        String filepath = String.format("%s/%s_%s_%s.%s", path, catalogName,dataset,seriesMember,distribution);
        this.api.callAPIFileDownload(url,filepath);
        return 1; //TODO: Change this behaviour
    }

    /**
     * Download a single distribution to the local filesystem. By default will write to downloads folder.
     * @param catalogName a String representing the identifier of the catalog to download from
     * @param dataset a String representing the dataset identifier to download.
     * @param seriesMember a String representing the series member identifier.
     * @param distribution a String representing the distribution identifier, this is the file extension.
     */
    public int download(String catalogName, String dataset, String seriesMember, String distribution) throws Exception{

        return this.download(catalogName, dataset, seriesMember, distribution, Fusion.DEFAULT_PATH);
    }

    /**
     * Download multiple distribution to the local filesystem. By default will write to downloads folder.
     * Not implemented.
     * @param catalogName a String representing the identifier of the catalog to download from
     * @param dataset a String representing the dataset identifier to download.
     * @param seriesMembers a List of Strings representing the series member identifiers.
     * @param distribution a String representing the distribution identifier, this is the file extension.
     */
    public int download(String catalogName, String dataset, List<String> seriesMembers, String distribution){
        return 0;
    }


    //TODO: This is duplicated from LocalDateDeserializer, but might be ok?
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Upload a new dataset series member to a catalog.
     * @param catalogName a String representing the identifier of the catalog to upload into
     * @param dataset a String representing the dataset identifier to upload against.
     * @param seriesMember a String representing the series member identifier to add or replace
     * @param distribution a String representing the distribution identifier, this is the file extension.
     * @param filename a path to the file containing the data to upload
     * @param fromDate the earliest date for which there is data in the distribution
     * @param toDate the latest date for which there is data in the distribution
     * @param createdDate the creation date for the distribution
     **/
    public int upload(String catalogName, String dataset, String seriesMember, String distribution, String filename, LocalDate fromDate, LocalDate toDate, LocalDate createdDate) throws Exception {

        String url = String.format("%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",this.rootURL, catalogName,dataset, seriesMember,distribution);
        String strFromDate = fromDate.format(dateTimeFormatter);
        String strToDate = toDate.format(dateTimeFormatter);
        String strCreatedDate = createdDate.format(dateTimeFormatter);
        return this.api.callAPIFileUpload(url, filename, strFromDate, strToDate, strCreatedDate );
    }

    /**
     * Upload a new dataset series member to a catalog.
     * @param catalogName a String representing the identifier of the catalog to upload into
     * @param dataset a String representing the dataset identifier to upload against.
     * @param seriesMember a String representing the series member identifier to add or replace
     * @param distribution a String representing the distribution identifier, this is the file extension.
     * @param filename a path to the file containing the data to upload
     * @param dataDate the earliest, latest, and created date are all the same.
     **/
    public int upload(String catalogName, String dataset, String seriesMember, String distribution, String filename, LocalDate dataDate) throws Exception {
        return this.upload(catalogName, dataset, seriesMember, distribution, filename, dataDate, dataDate, dataDate);
    }


    public static class FusionBuilder {
        //Implementation of helper methods to allow for simpler instantiation. Note that Lombok will fill in the missing, standard builder methods
        private FusionCredentials credentials;
        private IFusionAPIManager api;
        private Client client;

        public FusionBuilder bearerToken(String token){
            this.credentials = new BearerTokenCredentials(token);
            return this;
        }

        public FusionBuilder secretBasedCredentials(String clientId, String clientSecret, String resource, String authServerUrl){
            try {
                this.credentials = new OAuthSecretBasedCredentials(clientId, clientSecret, resource, authServerUrl);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e); //TODO: Fix error handling
            }
            return this;
        }

        public FusionBuilder passwordBasedCredentials(String clientId, String username, String password, String resource, String authServerUrl){
            try {
                this.credentials = new OAuthPasswordBasedCredentials(clientId, username, password, resource, authServerUrl);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e); //TODO: Fix error handling
            }
            return this;
        }

        public FusionBuilder proxy(String url, int port){
            client = new JdkClient(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(url, port)));
            api = new FusionAPIManager(credentials, client);
            return this;
        }
    }
}
