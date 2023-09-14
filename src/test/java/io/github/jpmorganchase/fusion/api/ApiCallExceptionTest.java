package io.github.jpmorganchase.fusion.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import org.junit.jupiter.api.Test;

public class ApiCallExceptionTest {

    @Test
    void undefinedCodeReturnsUnknownMessage() {
        APICallException e = new APICallException(999, "Unknown");
        assertThat(e.getResponseCode(), is(999));
        assertThat(e.getMessage(), is(equalTo("Unknown")));
    }

    @Test
    void bearerTokenMessage() {
        APICallException e = new APICallException(401, "Unknown");
        assertThat(e.getResponseCode(), is(401));
        assertThat(e.getMessage(), is(equalTo("The bearer token is missing or an invalid bearer token was provided")));
    }

    @Test
    void notPermittedMessage() {
        APICallException e = new APICallException(403, "Unknown");
        assertThat(e.getResponseCode(), is(403));
        assertThat(
                e.getMessage(),
                is(equalTo("Not permitted. Check credentials are correct or you are subscribed to the dataset")));
    }

    @Test
    void notFoundMessage() {
        APICallException e = new APICallException(404, "Unknown");
        assertThat(e.getResponseCode(), is(404));
        assertThat(e.getMessage(), is(equalTo("The requested resource does not exist.")));
    }

    @Test
    void unsupportedMediaTypeMessage() {
        APICallException e = new APICallException(415, "Unknown");
        assertThat(e.getResponseCode(), is(415));
        assertThat(
                e.getMessage(),
                is(equalTo("Unsupported media type. Confirm the correct method is being invoked for the operation.")));
    }

    @Test
    void internalServerErrorMessage() {
        APICallException e = new APICallException(500, "Unknown");
        assertThat(e.getResponseCode(), is(500));
        assertThat(e.getMessage(), is(equalTo("Internal API error. There was an error processing the request.")));
    }

    @Test
    void badRequestMessage() {
        APICallException e = new APICallException(400, "Headers do not match the expected order");
        assertThat(e.getResponseCode(), is(400));
        assertThat(e.getMessage(), is(equalTo("Headers do not match the expected order")));
    }

    @Test
    void badRequestMessageWhenDetailIsUnknown() {
        APICallException e = new APICallException(400, "Unknown");
        assertThat(e.getResponseCode(), is(400));
        assertThat(e.getMessage(), is(equalTo("Bad Request.  Please verify the correct data has been provided.")));
    }

    @Test
    void badRequestMessageWhenDetailIsUnknownLowercase() {
        APICallException e = new APICallException(400, "unknown");
        assertThat(e.getResponseCode(), is(400));
        assertThat(e.getMessage(), is(equalTo("Bad Request.  Please verify the correct data has been provided.")));
    }
}
