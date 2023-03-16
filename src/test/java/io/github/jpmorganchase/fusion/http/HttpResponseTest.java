package io.github.jpmorganchase.fusion.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class HttpResponseTest {

    @Test
    void successfulCodeIsNotAnErrorResponse() {
        HttpResponse<String> response = new HttpResponse<>(200, null, null);
        assertThat(response.isError(), is(false));
    }

    @Test
    void failureCodeIsAnErrorResponse() {
        HttpResponse<String> response = new HttpResponse<>(404, null, null);
        assertThat(response.isError(), is(true));
    }

    @Test
    void statusCodeBoundaryChecks() {
        HttpResponse<String> success = new HttpResponse<>(399, null, null);
        HttpResponse<String> failure = new HttpResponse<>(400, null, null);
        assertThat(success.isError(), is(false));
        assertThat(failure.isError(), is(true));
    }
}
