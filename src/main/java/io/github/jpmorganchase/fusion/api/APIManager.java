package io.github.jpmorganchase.fusion.api;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.operations.APIDownloadOperations;
import io.github.jpmorganchase.fusion.api.operations.APIUploadOperations;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public interface APIManager extends APIDownloadOperations, APIUploadOperations {

    /**
     * Sends a GET request to the specified API endpoint.
     *
     * @param apiPath the API endpoint path to which the GET request will be sent
     * @return the response body as a {@code String} if the request is successful
     * @throws APICallException if the response status indicates an error or the request fails
     */
    String callAPI(String apiPath) throws APICallException;

    String callAPIToPost(String apiPath) throws APICallException;

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
     * @param resource     the resource object to be serialized and sent as the request body
     * @return the response body as a {@code String} if the request is successful
     * @throws APICallException if the response status indicates an error or the request fails
     */
    String callAPIToPut(String apiPath, Object resource);

    /**
     * Sends a DELETE request to the specified API endpoint with the provided catalog resource.
     *
     * @param apiPath         the API endpoint path to which the PUT request will be sent
     * @throws APICallException if the response status indicates an error or the request fails
     */
    String callAPIToDelete(String apiPath);

    static String encodeUrl(String rawUrl) {
        try {
            URL url = new URL(rawUrl);

            String protocol = url.getProtocol();
            String host = url.getHost();
            int port = url.getPort();
            String path = url.getPath();
            String query = url.getQuery();

            boolean endsWithSlash = path.endsWith("/");

            String[] segments = path.split("/");
            StringBuilder encodedPath = new StringBuilder();
            for (String segment : segments) {
                if (segment.isEmpty()) continue;

                String encodedSegment;
                if (segment.equals(".") || segment.equals("..")) {
                    // Leave the path unencoded
                    encodedSegment = segment;
                } else {
                    encodedSegment = URLEncoder.encode(segment, "UTF-8").replace("+", "%20");

                    // If the segment contains '.' not used as a special path
                    if (segment.contains(".")) {
                        encodedSegment = encodedSegment.replace(".", "%2E");
                    }
                }

                encodedPath.append("/").append(encodedSegment);
            }

            StringBuilder finalUrl = new StringBuilder();
            finalUrl.append(protocol).append("://").append(host);
            if (port != -1) {
                finalUrl.append(":").append(port);
            }
            finalUrl.append(encodedPath);

            if (endsWithSlash && !finalUrl.toString().endsWith("/")) {
                finalUrl.append("/");
            }

            if (query != null) {
                finalUrl.append("?").append(query);
            }

            return finalUrl.toString();
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
