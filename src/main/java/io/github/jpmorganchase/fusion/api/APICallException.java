package io.github.jpmorganchase.fusion.api;

import io.github.jpmorganchase.fusion.exception.FusionException;

/**
 * A custom exception to provide useful information on the response of an API call
 */
public class APICallException extends FusionException {

    private final int responseCode;

    public APICallException(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * Returns the HTTP response code
     * @return a response code
     */
    public int getResponseCode() {
        return this.responseCode;
    }

    /**
     * Get a meaningful response message
     * @return a description for the exception
     */
    public String getMessage() {

        String errorMsg;
        switch (this.responseCode) {
            case 401:
                errorMsg = "The bearer token is missing or an invalid bearer token was provided";
                break;
            case 403:
                errorMsg = "Not permitted. Check credentials are correct or you are subscribed to the dataset";
                break;
            case 404:
                errorMsg = "The requested resource does not exist.";
                break;
            case 415:
                errorMsg = "Unsupported media type. Confirm the correct method is being invoked for the operation.";
                break;
            case 500:
                errorMsg = "Internal API error. There was an error processing the request.";
                break;
            case 504:
                errorMsg = "Request timed out. Please try again.";
                break;
            default:
                errorMsg = "Unknown";
        }
        return errorMsg;
    }
}
