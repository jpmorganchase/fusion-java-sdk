package com.jpmorganchase.fusion.http;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.emptyString;
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
    void correctHandlingOfResponseHeaderOnGetRequest() throws Exception {

        stubFor(get("/test").willReturn(aResponse()
                .withBody("sample response")
                .withHeader("test-header-1", "header-1-value")));

        HttpResponse<String> response = httpClient.get("http://localhost:8080/test", Collections.emptyMap());

        verify(getRequestedFor(urlEqualTo("/test")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
        assertThat(response.getHeaders().get("test-header-1").get(0), is(equalTo("header-1-value")));
    }

    @Test
    void getRequestRespectsConfiguredProxySettings() throws Exception {

        wiremockProxy.stubFor(get("/test").willReturn(aResponse().proxiedFrom("http://localhost:8080")));
        stubFor(get("/test").willReturn(aResponse().withBody("sample response")));

        HttpResponse<String> response = httpClientWithProxy.get("http://localhost:8080/test", Collections.emptyMap());

        wiremockProxy.verify(getRequestedFor(urlEqualTo("/test")));
        verify(getRequestedFor(urlEqualTo("/test")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
    }

    @Test
    void successfulPostCall() throws Exception {

        stubFor(post("/test").willReturn(aResponse().withBody("sample response")));

        HttpResponse<String> response = httpClient.post("http://localhost:8080/test", Collections.emptyMap(), "sample post body");

        verify(postRequestedFor(urlEqualTo("/test")).withRequestBody(WireMock.equalTo("sample post body")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
    }

    @Test
    void successfulPostCallWithOneHeader() throws Exception {

        stubFor(post("/test").willReturn(aResponse().withBody("sample response")));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("header1", "value1");
        HttpResponse<String> response = httpClient.post("http://localhost:8080/test", requestHeaders, "sample post body");

        verify(postRequestedFor(urlEqualTo("/test"))
                .withRequestBody(WireMock.equalTo("sample post body"))
                .withHeader("header1", WireMock.equalTo("value1")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
    }

    @Test
    void successfulPostCallWithManyHeaders() throws Exception {

        stubFor(post("/test").willReturn(aResponse().withBody("sample response")));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("header1", "value1");
        requestHeaders.put("header2", "value2");
        requestHeaders.put("header3", "value3");
        HttpResponse<String> response = httpClient.post("http://localhost:8080/test", requestHeaders, "sample post body");

        verify(postRequestedFor(urlEqualTo("/test"))
                .withRequestBody(WireMock.equalTo("sample post body"))
                .withHeader("header1", WireMock.equalTo("value1"))
                .withHeader("header2", WireMock.equalTo("value2"))
                .withHeader("header3", WireMock.equalTo("value3")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
    }

    @Test
    void postCallWith404Response() throws Exception {
        stubFor(post("/test").willReturn(aResponse().withStatus(HttpURLConnection.HTTP_NOT_FOUND)));

        HttpResponse<String> response = httpClient.post("http://localhost:8080/test", Collections.emptyMap(), "sample post body");

        verify(postRequestedFor(urlEqualTo("/test")));
        assertThat(response.getStatusCode(), is(equalTo(404)));
        assertThat(response.getBody(), is(emptyString()));
    }

    @Test
    void postCallWith500Response() throws Exception {
        stubFor(post("/test").willReturn(aResponse().withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)));

        HttpResponse<String> response = httpClient.post("http://localhost:8080/test", Collections.emptyMap(), "sample post body");

        verify(postRequestedFor(urlEqualTo("/test")));
        assertThat(response.getStatusCode(), is(equalTo(500)));
        assertThat(response.getBody(), is(emptyString()));
    }

    @Test
    void correctHandlingOfResponseHeaderOnPostRequest() throws Exception {

        stubFor(post("/test").willReturn(aResponse()
                .withBody("sample response")
                .withHeader("test-header-1", "header-1-value")));

        HttpResponse<String> response = httpClient.post("http://localhost:8080/test", Collections.emptyMap(), "sample post body");

        verify(postRequestedFor(urlEqualTo("/test")).withRequestBody(WireMock.equalTo("sample post body")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
        assertThat(response.getHeaders().get("test-header-1").get(0), is(equalTo("header-1-value")));
    }

    @Test
    void postRequestRespectsConfiguredProxySettings() throws Exception {

        wiremockProxy.stubFor(post("/test").willReturn(aResponse().proxiedFrom("http://localhost:8080")));
        stubFor(post("/test").willReturn(aResponse().withBody("sample response")));

        HttpResponse<String> response = httpClientWithProxy.post("http://localhost:8080/test", Collections.emptyMap(), "sample post body");

        wiremockProxy.verify(postRequestedFor(urlEqualTo("/test")));
        verify(postRequestedFor(urlEqualTo("/test")).withRequestBody(WireMock.equalTo("sample post body")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
    }

    @Test
    void successfulPutCall() throws Exception {

        stubFor(put("/test").willReturn(aResponse().withBody("sample response")));

        HttpResponse<String> response = httpClient.put("http://localhost:8080/test", Collections.emptyMap(), new ByteArrayInputStream("sample post body".getBytes()));

        verify(putRequestedFor(urlEqualTo("/test")).withRequestBody(WireMock.equalTo("sample post body")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
    }

    @Test
    void successfulPutCallWithOneHeader() throws Exception {

        stubFor(put("/test").willReturn(aResponse().withBody("sample response")));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("header1", "value1");
        HttpResponse<String> response = httpClient.put("http://localhost:8080/test", requestHeaders, new ByteArrayInputStream("sample post body".getBytes()));

        verify(putRequestedFor(urlEqualTo("/test"))
                .withRequestBody(WireMock.equalTo("sample post body"))
                .withHeader("header1", WireMock.equalTo("value1")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
    }

    @Test
    void successfulPutCallWithManyHeaders() throws Exception {

        stubFor(put("/test").willReturn(aResponse().withBody("sample response")));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("header1", "value1");
        requestHeaders.put("header2", "value2");
        requestHeaders.put("header3", "value3");
        HttpResponse<String> response = httpClient.put("http://localhost:8080/test", requestHeaders, new ByteArrayInputStream("sample post body".getBytes()));

        verify(putRequestedFor(urlEqualTo("/test"))
                .withRequestBody(WireMock.equalTo("sample post body"))
                .withHeader("header1", WireMock.equalTo("value1"))
                .withHeader("header2", WireMock.equalTo("value2"))
                .withHeader("header3", WireMock.equalTo("value3")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
    }

    @Test
    void putCallWith404Response() throws Exception {
        stubFor(put("/test").willReturn(aResponse().withStatus(HttpURLConnection.HTTP_NOT_FOUND)));

        HttpResponse<String> response = httpClient.put("http://localhost:8080/test", Collections.emptyMap(), new ByteArrayInputStream("sample post body".getBytes()));

        verify(putRequestedFor(urlEqualTo("/test")));
        assertThat(response.getStatusCode(), is(equalTo(404)));
        assertThat(response.getBody(), is(emptyString()));
    }

    @Test
    void putCallWith500Response() throws Exception {
        stubFor(put("/test").willReturn(aResponse().withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)));

        HttpResponse<String> response = httpClient.put("http://localhost:8080/test", Collections.emptyMap(), new ByteArrayInputStream("sample post body".getBytes()));

        verify(putRequestedFor(urlEqualTo("/test")));
        assertThat(response.getStatusCode(), is(equalTo(500)));
        assertThat(response.getBody(), is(emptyString()));
    }

    @Test
    void correctHandlingOfResponseHeaderOnPutRequest() throws Exception {

        stubFor(put("/test").willReturn(aResponse()
                .withBody("sample response")
                .withHeader("test-header-1", "header-1-value")));

        HttpResponse<String> response = httpClient.put("http://localhost:8080/test", Collections.emptyMap(), new ByteArrayInputStream("sample post body".getBytes()));

        verify(putRequestedFor(urlEqualTo("/test")).withRequestBody(WireMock.equalTo("sample post body")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
        assertThat(response.getHeaders().get("test-header-1").get(0), is(equalTo("header-1-value")));
    }

    @Test
    void putRequestRespectsConfiguredProxySettings() throws Exception {

        wiremockProxy.stubFor(put("/test").willReturn(aResponse().proxiedFrom("http://localhost:8080")));
        stubFor(put("/test").willReturn(aResponse().withBody("sample response")));

        HttpResponse<String> response = httpClientWithProxy.put("http://localhost:8080/test", Collections.emptyMap(), new ByteArrayInputStream("sample post body".getBytes()));

        wiremockProxy.verify(putRequestedFor(urlEqualTo("/test")));
        verify(putRequestedFor(urlEqualTo("/test")).withRequestBody(WireMock.equalTo("sample post body")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo("sample response")));
    }

    @Test
    void successfulGetStreamCallWithNoHeaders() throws Exception {

        stubFor(get("/test").willReturn(aResponse().withBody("sample response")));

        HttpResponse<InputStream> response = httpClient.getInputStream("http://localhost:8080/test", Collections.emptyMap());

        verify(getRequestedFor(urlEqualTo("/test")));
        assertThat(response.getStatusCode(), is(equalTo(200)));

        String responseText = new BufferedReader(
                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        assertThat(responseText, is(equalTo("sample response")));
    }

    @Test
    @Disabled
    void implementAllOtherTests(){
        //leaving this here so that I dont forget what I was doing next week
        /*
                DONE

                1. Finish tests for get requests (handling of response headers, requests through a proxy)
                2. Add tests for PUT and POST in a similar fashion

                TODO

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
