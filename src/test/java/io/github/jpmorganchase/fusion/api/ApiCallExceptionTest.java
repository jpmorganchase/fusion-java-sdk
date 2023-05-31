package io.github.jpmorganchase.fusion.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import org.junit.jupiter.api.Test;

public class ApiCallExceptionTest {

    @Test
    void undefinedCodeReturnsUnknownMessage() {
        APICallException e = new APICallException(999);
        assertThat(e.getResponseCode(), is(999));
        assertThat(e.getMessage(), is(equalTo("Unknown")));
    }

    @Test
    void bearerTokenMessage() {
        APICallException e = new APICallException(401);
        assertThat(e.getResponseCode(), is(401));
        assertThat(e.getMessage(), is(equalTo("The bearer token is missing or an invalid bearer token was provided")));
    }

    @Test
    void notPermittedMessage() {
        APICallException e = new APICallException(403);
        assertThat(e.getResponseCode(), is(403));
        assertThat(
                e.getMessage(),
                is(equalTo("Not permitted. Check credentials are correct or you are subscribed to the dataset")));
    }

    @Test
    void notFoundMessage() {
        APICallException e = new APICallException(404);
        assertThat(e.getResponseCode(), is(404));
        assertThat(e.getMessage(), is(equalTo("The requested resource does not exist.")));
    }

    @Test
    void unsupportedMediaTypeMessage() {
        APICallException e = new APICallException(415);
        assertThat(e.getResponseCode(), is(415));
        assertThat(
                e.getMessage(),
                is(equalTo("Unsupported media type. Confirm the correct method is being invoked for the operation.")));
    }

    @Test
    void internalServerErrorMessage() {
        APICallException e = new APICallException(500);
        assertThat(e.getResponseCode(), is(500));
        assertThat(e.getMessage(), is(equalTo("Internal API error. There was an error processing the request.")));
    }
}
