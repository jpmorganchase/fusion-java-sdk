package io.github.jpmorganchase.fusion.pact.util;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.core.model.RequestResponsePact;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestResponseHelper {

    private static final String BEARER_TOKEN = "my-bearer-token";
    private static final String AUTH_VAL = "Bearer " + BEARER_TOKEN;

    private RequestResponseHelper() {}
    ;

    public static RequestResponsePact getExpectation(
            PactDslWithProvider builder, String given, String upon, String path, DslPart body) {
        return builder.given(given)
                .uponReceiving(upon)
                .path(path)
                .matchHeader("Authorization", AUTH_VAL)
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(body)
                .toPact();
    }

    public static RequestResponsePact downloadExpectation(
            PactDslWithProvider builder, String given, String upon, String path, String body) {
        return builder.given(given)
                .uponReceiving(upon)
                .path(path)
                .matchHeader("Authorization", AUTH_VAL)
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(body)
                .toPact();
    }

    public static RequestResponsePact uploadExpectation(
            PactDslWithProvider builder,
            String given,
            String upon,
            String authPath,
            String path,
            Map<String, String> headers,
            String body) {

        log.atInfo().log("Initialising Expectation for upload");
        return builder.given(given)
                .uponReceiving(upon)
                .path(path)
                .matchHeader("accept", "\\*\\/\\*")
                .matchHeader("Authorization", AUTH_VAL)
                .matchHeader("Fusion-Authorization", "Bearer my-fusion-bearer")
                .matchHeader("Content-Type", "application/octet-stream")
                .headers(headers)
                .method("PUT")
                .body(body)
                .willRespondWith()
                .status(200)
                .toPact();
    }
}
