package io.github.jpmorganchase.fusion.api.tools;

import static io.github.jpmorganchase.fusion.api.tools.RegexBasedErrorParser.UNKNOWN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResponseCheckerTest {

    @Test
    public void testWhenResponseIsError() {
        HttpResponse<String> response = HttpResponse.<String>builder()
                .statusCode(500)
                .body("Error Occurred")
                .build();

        APICallException ex = assertThrows(APICallException.class, () -> ResponseChecker.checkResponseStatus(response));
        assertThat(ex.getResponseCode(), equalTo(500));
        assertThat(ex.getMessage(), not(UNKNOWN));
    }

    @Test
    public void testWhenResponseIsErrorAndContainsErrorDetailAndIs400() {
        HttpResponse<String> response = HttpResponse.<String>builder()
                .statusCode(400)
                .body("{\"error\": \"Issue with headers\"}")
                .build();

        APICallException ex = assertThrows(APICallException.class, () -> ResponseChecker.checkResponseStatus(response));
        assertThat(ex.getResponseCode(), equalTo(400));
        assertThat(ex.getMessage(), equalTo("Issue with headers"));
    }

    @Test
    public void testWhenResponseIsErrorAndContainsErrorDetailAndIsNot500() {
        HttpResponse<String> response = HttpResponse.<String>builder()
                .statusCode(500)
                .body("{\"error\": \"Server is broken\"}")
                .build();

        APICallException ex = assertThrows(APICallException.class, () -> ResponseChecker.checkResponseStatus(response));
        assertThat(ex.getResponseCode(), equalTo(500));
        assertThat(ex.getMessage(), equalTo(new APICallException(500, "Unknown").getMessage()));
    }

    @Test
    public void testWhenResponseIsErrorAndBodyIsNull() {
        HttpResponse<String> response =
                HttpResponse.<String>builder().statusCode(400).build();

        APICallException ex = assertThrows(APICallException.class, () -> ResponseChecker.checkResponseStatus(response));
        assertThat(ex.getResponseCode(), equalTo(400));
        assertThat(ex.getMessage(), equalTo("Bad Request. Please verify the correct data has been provided."));
    }

    @Test
    public void testWhenResponseIsOk() {
        HttpResponse<String> response =
                HttpResponse.<String>builder().statusCode(200).build();
        assertDoesNotThrow(() -> ResponseChecker.checkResponseStatus(response));
    }
}
