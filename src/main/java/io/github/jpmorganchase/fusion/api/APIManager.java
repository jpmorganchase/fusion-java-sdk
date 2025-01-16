package io.github.jpmorganchase.fusion.api;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.operations.APIDownloadOperations;
import io.github.jpmorganchase.fusion.api.operations.APIUploadOperations;
import io.github.jpmorganchase.fusion.model.CatalogResource;

public interface APIManager extends APIDownloadOperations, APIUploadOperations {

    /**
     * Sends a GET request to the specified API endpoint.
     *
     * @param apiPath the API endpoint path to which the GET request will be sent
     * @return the response body as a {@code String} if the request is successful
     * @throws APICallException if the response status indicates an error or the request fails
     */
    String callAPI(String apiPath) throws APICallException;

    /**
     * Sends a POST request to the specified API endpoint with the provided catalog resource.
     *
     * @param apiPath         the API endpoint path to which the POST request will be sent
     * @param catalogResource the resource object to be serialized and sent as the request body
     * @return the response body as a {@code String} if the request is successful
     * @throws APICallException if the response status indicates an error or the request fails
     */
    String callAPIToPost(String apiPath, CatalogResource catalogResource);

    /**
     * Sends a POST request to the specified API endpoint with the provided catalog resource.
     *
     * @param apiPath         the API endpoint path to which the POST request will be sent
     * @param resource     the resource object to be serialized and sent as the request body
     * @return the response body as a {@code String} if the request is successful
     * @throws APICallException if the response status indicates an error or the request fails
     */
    String callAPIToPost(String apiPath, Object resource);

    /**
     * Sends a PUT request to the specified API endpoint with the provided catalog resource.
     *
     * @param apiPath         the API endpoint path to which the PUT request will be sent
     * @param catalogResource the resource object to be serialized and sent as the request body
     * @return the response body as a {@code String} if the request is successful
     * @throws APICallException if the response status indicates an error or the request fails
     */
    String callAPIToPut(String apiPath, CatalogResource catalogResource);

    /**
     * Sends a DELETE request to the specified API endpoint with the provided catalog resource.
     *
     * @param apiPath         the API endpoint path to which the PUT request will be sent
     * @throws APICallException if the response status indicates an error or the request fails
     */
    String callAPIToDelete(String apiPath);
}
