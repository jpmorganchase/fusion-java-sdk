package io.github.jpmorganchase.fusion.api;

import static io.github.jpmorganchase.fusion.api.tools.ResponseChecker.checkResponseStatus;

import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.FusionInitialisationException;
import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.exception.FileDownloadException;
import io.github.jpmorganchase.fusion.api.operations.APIDownloadOperations;
import io.github.jpmorganchase.fusion.api.operations.APIUploadOperations;
import io.github.jpmorganchase.fusion.api.operations.FusionAPIDownloadOperations;
import io.github.jpmorganchase.fusion.api.operations.FusionAPIUploadOperations;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.http.JdkClient;
import io.github.jpmorganchase.fusion.oauth.credential.BearerTokenCredentials;
import io.github.jpmorganchase.fusion.oauth.provider.FusionTokenProvider;
import io.github.jpmorganchase.fusion.serializing.APIRequestSerializer;
import io.github.jpmorganchase.fusion.serializing.GsonAPIRequestSerializer;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Builder;

/**
 * Class that manages calls to the API. Intended to be called from multithreaded code.
 */
@Builder
public class FusionAPIManager implements APIManager {

    private final Client httpClient;
    private final FusionTokenProvider tokenProvider;
    private final APIDownloadOperations downloader;
    private APIUploadOperations uploader;

    @Builder.Default
    private APIRequestSerializer serializer = new GsonAPIRequestSerializer();

    public void updateBearerToken(String token) {
        tokenProvider.updateCredentials(new BearerTokenCredentials(token));
    }

    /**
     * Sends a GET request to the specified API endpoint.
     *
     * <p>This method constructs the necessary authorization headers using a bearer token from
     * the {@code tokenProvider} and sends a GET request to the specified {@code apiPath} using
     * the {@code httpClient}. It then checks the HTTP response status for errors and returns the
     * response body if the request is successful.
     *
     * @param apiPath the API endpoint path to which the GET request will be sent
     * @return the response body as a {@code String} if the request is successful
     * @throws APICallException if the response status indicates an error or the request fails
     */
    @Override
    public String callAPI(String apiPath) throws APICallException {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + tokenProvider.getSessionBearerToken());

        HttpResponse<String> response = httpClient.get(apiPath, requestHeaders);
        checkResponseStatus(response);
        return response.getBody();
    }

    /**
     * Sends a POST request to the specified API endpoint with the provided catalog resource.
     *
     * <p>This method constructs the necessary authorization headers using a bearer token from
     * the {@code tokenProvider}, serializes the given {@code catalogResource} into JSON,
     * and sends a POST request to the specified {@code apiPath} using the {@code httpClient}.
     * It then checks the HTTP response status for errors and returns the response body if successful.
     *
     * @param apiPath         the API endpoint path to which the POST request will be sent
     * @param resource the resource object to be serialized and sent as the request body
     * @return the response body as a {@code String} if the request is successful
     * @throws APICallException if the response status indicates an error or the request fails
     */
    @Override
    public String callAPIToPost(String apiPath, Object resource) throws APICallException {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + tokenProvider.getSessionBearerToken());
        requestHeaders.put("Content-Type", "application/json");

        HttpResponse<String> response = httpClient.post(apiPath, requestHeaders, serializer.serialize(resource));
        checkResponseStatus(response);
        return response.getBody();
    }

    /**
     * Sends a PUT request to the specified API endpoint with the provided catalog resource.
     *
     * @param apiPath         the API endpoint path to which the PUT request will be sent
     * @param resource the resource object to be serialized and sent as the request body
     * @return the response body as a {@code String} if the request is successful
     * @throws APICallException if the response status indicates an error or the request fails
     */
    @Override
    public String callAPIToPut(String apiPath, Object resource) {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + tokenProvider.getSessionBearerToken());
        requestHeaders.put("Content-Type", "application/json");

        HttpResponse<String> response = httpClient.put(apiPath, serializer.serialize(resource), requestHeaders);
        checkResponseStatus(response);
        return response.getBody();
    }

    /**
     * Sends a DELETE request to the specified API endpoint with the provided catalog resource.
     *
     * @param apiPath         the API endpoint path to which the PUT request will be sent
     * @throws APICallException if the response status indicates an error or the request fails
     */
    @Override
    public String callAPIToDelete(String apiPath) {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + tokenProvider.getSessionBearerToken());

        HttpResponse<String> response = httpClient.delete(apiPath, requestHeaders, null);
        checkResponseStatus(response);
        return response.getBody();
    }

    @Override
    public void callAPIFileDownload(
            String apiPath, String fileName, String catalog, String dataset, Map<String, String> headers)
            throws APICallException, FileDownloadException {
        downloader.callAPIFileDownload(apiPath, fileName, catalog, dataset, headers);
    }

    @Override
    public InputStream callAPIFileDownload(String apiPath, String catalog, String dataset, Map<String, String> headers)
            throws APICallException, FileDownloadException {
        return downloader.callAPIFileDownload(apiPath, catalog, dataset, headers);
    }

    @Override
    public void callAPIFileUpload(
            String apiPath,
            String fileName,
            String catalogName,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate,
            Map<String, String> headers)
            throws APICallException {
        uploader.callAPIFileUpload(apiPath, fileName, catalogName, dataset, fromDate, toDate, createdDate, headers);
    }

    @Override
    public void callAPIFileUpload(
            String apiPath,
            InputStream data,
            String catalogName,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate,
            Map<String, String> headers)
            throws APICallException {
        uploader.callAPIFileUpload(apiPath, data, catalogName, dataset, fromDate, toDate, createdDate, headers);
    }

    public static FusionAPIManagerBuilder builder() {
        return new CustomFusionAPIManagerBuilder();
    }

    public static class FusionAPIManagerBuilder {

        protected Client httpClient;
        protected FusionTokenProvider tokenProvider;
        protected APIDownloadOperations downloader;
        protected APIUploadOperations uploader;

        protected FusionConfiguration configuration =
                FusionConfiguration.builder().build();

        public FusionAPIManagerBuilder configuration(FusionConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }
    }

    public static class CustomFusionAPIManagerBuilder extends FusionAPIManagerBuilder {
        @Override
        public FusionAPIManager build() {

            if (Objects.isNull(tokenProvider)) {
                throw new FusionInitialisationException("No Fusion credentials provided, cannot build Fusion instance");
            }

            if (Objects.isNull(httpClient)) {
                this.httpClient = JdkClient.builder().noProxy().build();
            }

            if (Objects.isNull(downloader)) {
                this.downloader = FusionAPIDownloadOperations.builder()
                        .configuration(configuration)
                        .httpClient(httpClient)
                        .fusionTokenProvider(tokenProvider)
                        .build();
            }

            if (Objects.isNull(uploader)) {
                this.uploader = FusionAPIUploadOperations.builder()
                        .configuration(configuration)
                        .httpClient(httpClient)
                        .fusionTokenProvider(tokenProvider)
                        .build();
            }
            return super.build();
        }
    }
}
