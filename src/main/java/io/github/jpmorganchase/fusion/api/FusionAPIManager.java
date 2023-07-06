package io.github.jpmorganchase.fusion.api;

import static io.github.jpmorganchase.fusion.api.tools.ResponseChecker.checkResponseStatus;

import io.github.jpmorganchase.fusion.FusionConfiguration;
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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.jpmorganchase.fusion.oauth.provider.FusionTokenProvider;
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

    public void updateBearerToken(String token) {
        tokenProvider.updateCredentials(new BearerTokenCredentials(token));
    }

    /**
     * Call the API with the path provided and return the JSON response.
     *
     * @param apiPath appended to the base URL
     */
    @Override
    public String callAPI(String apiPath) throws APICallException {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", "Bearer " + tokenProvider.getSessionBearerToken());

        HttpResponse<String> response = httpClient.get(apiPath, requestHeaders);
        checkResponseStatus(response);
        return response.getBody();
    }

    @Override
    public void callAPIFileDownload(String apiPath, String fileName, String catalog, String dataset) throws APICallException, FileDownloadException {
        downloader.callAPIFileDownload(apiPath, fileName, catalog, dataset);
    }

    @Override
    public InputStream callAPIFileDownload(String apiPath, String catalog, String dataset) throws APICallException, FileDownloadException {
        return downloader.callAPIFileDownload(apiPath, catalog, dataset);
    }

    @Override
    public void callAPIFileUpload(String apiPath, String fileName, String catalogName, String dataset, String fromDate, String toDate, String createdDate) throws APICallException {
        uploader.callAPIFileUpload(apiPath, fileName, catalogName, dataset, fromDate, toDate, createdDate);
    }

    @Override
    public void callAPIFileUpload(String apiPath, InputStream data, String catalogName, String dataset, String fromDate, String toDate, String createdDate) throws APICallException {
        uploader.callAPIFileUpload(apiPath, data, catalogName, dataset, fromDate, toDate, createdDate);
    }

    public static FusionAPIManagerBuilder builder() {
        return new CustomFusionAPIManagerBuilder();
    }

    public static class FusionAPIManagerBuilder {

        protected Client httpClient;
        protected FusionTokenProvider tokenProvider;
        protected APIDownloadOperations downloader;
        protected APIUploadOperations uploader;

        protected FusionConfiguration configuration = FusionConfiguration.builder().build();

        public FusionAPIManagerBuilder configuration(FusionConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

    }

    public static class CustomFusionAPIManagerBuilder extends FusionAPIManagerBuilder {
        @Override
        public FusionAPIManager build() {

            if (Objects.isNull(httpClient)){
                this.httpClient = JdkClient.builder().noProxy().build();
            }

            if (Objects.isNull(downloader)){
                this.downloader = FusionAPIDownloadOperations.builder()
                        .configuration(configuration)
                        .httpClient(httpClient)
                        .fusionTokenProvider(tokenProvider)
                        .build();
            }

            if (Objects.isNull(uploader)){
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
