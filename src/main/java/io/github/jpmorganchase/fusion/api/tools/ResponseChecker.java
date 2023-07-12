package io.github.jpmorganchase.fusion.api.tools;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.http.HttpResponse;

public class ResponseChecker {

    /**
     * @param response to be verified
     * @param <T> type expected in the body of the response
     * @throws APICallException indicating a failure to communicate with Fusion API
     */
    public static <T> void checkResponseStatus(HttpResponse<T> response) throws APICallException {
        if (response.isError()) {
            throw new APICallException(response.getStatusCode());
        }
    }
}
