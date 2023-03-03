package com.jpmorganchase.fusion.http;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.jpmorganchase.fusion.APICallException;
import com.jpmorganchase.fusion.credential.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.emptyString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class JdkClientTest {

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
            .options(wireMockConfig().port(8080).notifier(new ConsoleNotifier(true))) //TODO: Remove this fixed port and use the default of a random port
            .configureStaticDsl(true)
            .build();

    @RegisterExtension
    static WireMockExtension wiremockProxy = WireMockExtension.newInstance()
            .options(wireMockConfig().port(8081).notifier(new ConsoleNotifier(true))) //TODO: Remove this fixed port and use the default of a random port
            .build();

    @BeforeAll
    static void initialiseProxy(){
        wiremockProxy.stubFor(get("/test").willReturn(aResponse().proxiedFrom("http://localhost:8080")));
    }

    private static final Client httpClient = new JdkClient();
    private static final Client httpClientWithProxy = new JdkClient(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8081)));

    @Test
    void successfulGetCallWithNoHeaders() throws Exception {

        stubFor(get("/test").willReturn(aResponse().withBody("sample response")));

        HttpResponse<String> response = httpClient.get("http://localhost:8080/test", Collections.emptyMap());

        verify(getRequestedFor(urlEqualTo("/test")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
    }

    @Test
    void successfulGetCallWithOneHeader() throws Exception {

        stubFor(get("/test").willReturn(aResponse().withBody("sample response")));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("header1", "value1");
        HttpResponse<String> response = httpClient.get("http://localhost:8080/test", requestHeaders);

        verify(getRequestedFor(urlEqualTo("/test")).withHeader("header1", WireMock.equalTo("value1")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
    }

    @Test
    void successfulGetCallWithManyHeaders() throws Exception {

        stubFor(get("/test").willReturn(aResponse().withBody("sample response")));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("header1", "value1");
        requestHeaders.put("header2", "value2");
        requestHeaders.put("header3", "value3");
        HttpResponse<String> response = httpClient.get("http://localhost:8080/test", requestHeaders);

        verify(getRequestedFor(urlEqualTo("/test"))
                .withHeader("header1", WireMock.equalTo("value1"))
                .withHeader("header2", WireMock.equalTo("value2"))
                .withHeader("header3", WireMock.equalTo("value3")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
    }

    @Test
    void getCallWith404Response() throws Exception {
        stubFor(get("/test").willReturn(aResponse().withStatus(HttpURLConnection.HTTP_NOT_FOUND)));

        HttpResponse<String> response = httpClient.get("http://localhost:8080/test", Collections.emptyMap());

        verify(getRequestedFor(urlEqualTo("/test")));
        assertThat(response.getStatusCode(), is(equalTo(404)));
        assertThat(response.getBody(), is(emptyString()));
    }

    @Test
    void getCallWith500Response() throws Exception {
        stubFor(get("/test").willReturn(aResponse().withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)));

        HttpResponse<String> response = httpClient.get("http://localhost:8080/test", Collections.emptyMap());

        verify(getRequestedFor(urlEqualTo("/test")));
        assertThat(response.getStatusCode(), is(equalTo(500)));
        assertThat(response.getBody(), is(emptyString()));
    }

    @Test
    void implementAllOtherTests(){
        //leaving this here so that I dont forget what I was doing next week
        /*
                1. Finish tests for get requests (handling of response headers, requests through a proxy)
                2. Add tests for PUT and POST in a similar fashion
                3. Refactor all these HTTP tests to remove duplication
                4. Go back and refactor all the FusionApiManager tests now that we don't need to execute them against Wiremock
                5. Refactor of the JdkClient once all tests are in place - needs duplication removed and some clean-up
                6. Start working on the user facing code next - e.g. Fusion.java (Builder? to allow for customisation of Credential type, Http client type and Proxy information)
                7. Save and loading of credentials (use GSON), do we want an interface for this that the one Credential type that needs it can use?
                8. Finish the tests for ApiResponseParser - need a set for each model class
                9. Credential refactoring - concurrency etc
                10. Logging
                11. Mutation testing
                12. Documentation
         */
        fail();
    }

}
