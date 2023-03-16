package io.github.jpmorganchase.fusion.http;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class JdkClientTest {

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().notifier(new Slf4jNotifier(true)))
            .configureStaticDsl(true)
            .build();

    @RegisterExtension
    static WireMockExtension wiremockProxy = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().notifier(new Slf4jNotifier(true)))
            .build();

    private static final Client httpClient = new JdkClient();
    private static Client httpClientWithProxy;

    private static final String SAMPLE_RESPONSE_BODY = "sample response";
    private static final String BASE_PATH = "/test";
    private static String BASE_URL;
    private static String API_URL;
    private static Map<String, String> SINGLE_REQUEST_HEADER;
    private static Map<String, String> MULTIPLE_REQUEST_HEADERS;
    private static final Map<String, String> NO_REQUEST_HEADERS = Collections.emptyMap();
    private static Map<String, String> SINGLE_RESPONSE_HEADER;

    @BeforeAll
    public static void setUp() throws MalformedURLException {
        BASE_URL = wiremock.getRuntimeInfo().getHttpBaseUrl();
        API_URL = String.format("%s%s", BASE_URL, BASE_PATH);

        URL proxyUrl = new URL(wiremockProxy.getRuntimeInfo().getHttpBaseUrl());
        httpClientWithProxy = new JdkClient(
                new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl.getHost(), wiremockProxy.getPort())));

        SINGLE_REQUEST_HEADER = new HashMap<>();
        SINGLE_REQUEST_HEADER.put("header1", "value1");

        MULTIPLE_REQUEST_HEADERS = new HashMap<>();
        MULTIPLE_REQUEST_HEADERS.put("header1", "value1");
        MULTIPLE_REQUEST_HEADERS.put("header2", "value2");
        MULTIPLE_REQUEST_HEADERS.put("header3", "value3");

        SINGLE_RESPONSE_HEADER = new HashMap<>();
        SINGLE_RESPONSE_HEADER.put("test-header-1", "header-value-1");
    }

    @Test
    void successfulGetCallWithNoHeaders() {
        getMethodStub();
        HttpResponse<String> response = executeGetRequest(NO_REQUEST_HEADERS);
        validateGetRequest(response);
    }

    private static void getMethodStub() {
        stubFor(get(BASE_PATH).willReturn(aResponse().withBody(SAMPLE_RESPONSE_BODY)));
    }

    private static void getMethodStub(Map<String, String> responseHeaders) {
        ResponseDefinitionBuilder responseDefinition = aResponse().withBody(SAMPLE_RESPONSE_BODY);
        for (Map.Entry<String, String> header : responseHeaders.entrySet()) {
            responseDefinition.withHeader(header.getKey(), header.getValue());
        }
        stubFor(get(BASE_PATH).willReturn(responseDefinition));
    }

    private static void getMethodStub(int responseCode) {
        stubFor(get(BASE_PATH).willReturn(aResponse().withStatus(responseCode)));
    }

    private HttpResponse<String> executeGetRequest(Map<String, String> headers) {
        return httpClient.get(API_URL, headers);
    }

    private void validateGetRequest(HttpResponse<String> response) {
        validateGetRequest(
                response, NO_REQUEST_HEADERS, SAMPLE_RESPONSE_BODY, HttpURLConnection.HTTP_OK, Collections.emptyMap());
    }

    private void validateGetRequest(HttpResponse<String> response, Map<String, String> expectedRequestHeaders) {
        validateGetRequest(
                response,
                expectedRequestHeaders,
                SAMPLE_RESPONSE_BODY,
                HttpURLConnection.HTTP_OK,
                Collections.emptyMap());
    }

    private void validateGetRequest(
            HttpResponse<String> response,
            Map<String, String> expectedRequestHeaders,
            String expectedResponseBody,
            int expectedResponseCode) {
        validateGetRequest(
                response, expectedRequestHeaders, expectedResponseBody, expectedResponseCode, Collections.emptyMap());
    }

    private void validateGetRequest(
            HttpResponse<String> response,
            Map<String, String> expectedRequestHeaders,
            String expectedResponseBody,
            int expectedResponseCode,
            Map<String, String> expectedResponseHeaders) {
        if (expectedRequestHeaders.size() > 0) {
            // Add headers to the stub expectation if we had any on the request
            RequestPatternBuilder requestPatternBuilder = getRequestedFor(urlEqualTo(BASE_PATH));
            for (Map.Entry<String, String> header : expectedRequestHeaders.entrySet()) {
                requestPatternBuilder.withHeader(header.getKey(), WireMock.equalTo(header.getValue()));
            }
            verify(requestPatternBuilder);
        } else {
            // otherwise just verify that the URL was invoked
            verify(getRequestedFor(urlEqualTo(BASE_PATH)));
        }
        assertThat(response.getStatusCode(), is(equalTo(expectedResponseCode)));
        assertThat(response.getBody(), is(equalTo(expectedResponseBody)));

        expectedResponseHeaders.forEach(
                (key, value) -> assertThat(response.getHeaders().get(key).get(0), is(equalTo(value))));
    }

    @Test
    void successfulGetCallWithOneHeader() {
        getMethodStub();

        HttpResponse<String> response = executeGetRequest(SINGLE_REQUEST_HEADER);

        validateGetRequest(response, SINGLE_REQUEST_HEADER);
    }

    @Test
    void successfulGetCallWithManyHeaders() {
        getMethodStub();

        HttpResponse<String> response = executeGetRequest(MULTIPLE_REQUEST_HEADERS);

        validateGetRequest(response, MULTIPLE_REQUEST_HEADERS);
    }

    @Test
    void getCallWith404Response() {
        getMethodStub(HttpURLConnection.HTTP_NOT_FOUND);

        HttpResponse<String> response = executeGetRequest(Collections.emptyMap());

        validateGetRequest(response, NO_REQUEST_HEADERS, "", HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void getCallWith500Response() throws Exception {
        getMethodStub(HttpURLConnection.HTTP_INTERNAL_ERROR);

        HttpResponse<String> response = executeGetRequest(Collections.emptyMap());

        validateGetRequest(response, NO_REQUEST_HEADERS, "", HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    @Test
    void correctHandlingOfResponseHeaderOnGetRequest() throws Exception {

        getMethodStub(SINGLE_RESPONSE_HEADER);

        HttpResponse<String> response = executeGetRequest(NO_REQUEST_HEADERS);

        validateGetRequest(
                response, NO_REQUEST_HEADERS, SAMPLE_RESPONSE_BODY, HttpURLConnection.HTTP_OK, SINGLE_RESPONSE_HEADER);
    }

    @Test
    void getRequestRespectsConfiguredProxySettings() throws Exception {

        wiremockProxy.stubFor(get(BASE_PATH).willReturn(aResponse().proxiedFrom(BASE_URL)));
        getMethodStub();

        HttpResponse<String> response = httpClientWithProxy.get(API_URL, Collections.emptyMap());

        wiremockProxy.verify(getRequestedFor(urlEqualTo(BASE_PATH)));
        validateGetRequest(response);
    }

    @Test
    void successfulPostCall() throws Exception {

        stubFor(post(BASE_PATH).willReturn(aResponse().withBody(SAMPLE_RESPONSE_BODY)));

        HttpResponse<String> response = httpClient.post(API_URL, Collections.emptyMap(), "sample post body");

        verify(postRequestedFor(urlEqualTo(BASE_PATH)).withRequestBody(WireMock.equalTo("sample post body")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo(SAMPLE_RESPONSE_BODY)));
    }

    @Test
    void successfulPostCallWithOneHeader() throws Exception {

        stubFor(post(BASE_PATH).willReturn(aResponse().withBody(SAMPLE_RESPONSE_BODY)));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("header1", "value1");
        HttpResponse<String> response = httpClient.post(API_URL, requestHeaders, "sample post body");

        verify(postRequestedFor(urlEqualTo(BASE_PATH))
                .withRequestBody(WireMock.equalTo("sample post body"))
                .withHeader("header1", WireMock.equalTo("value1")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo(SAMPLE_RESPONSE_BODY)));
    }

    @Test
    void successfulPostCallWithManyHeaders() throws Exception {

        stubFor(post(BASE_PATH).willReturn(aResponse().withBody(SAMPLE_RESPONSE_BODY)));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("header1", "value1");
        requestHeaders.put("header2", "value2");
        requestHeaders.put("header3", "value3");
        HttpResponse<String> response = httpClient.post(API_URL, requestHeaders, "sample post body");

        verify(postRequestedFor(urlEqualTo(BASE_PATH))
                .withRequestBody(WireMock.equalTo("sample post body"))
                .withHeader("header1", WireMock.equalTo("value1"))
                .withHeader("header2", WireMock.equalTo("value2"))
                .withHeader("header3", WireMock.equalTo("value3")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo(SAMPLE_RESPONSE_BODY)));
    }

    @Test
    void postCallWith404Response() throws Exception {
        stubFor(post(BASE_PATH).willReturn(aResponse().withStatus(HttpURLConnection.HTTP_NOT_FOUND)));

        HttpResponse<String> response = httpClient.post(API_URL, Collections.emptyMap(), "sample post body");

        verify(postRequestedFor(urlEqualTo(BASE_PATH)));
        assertThat(response.getStatusCode(), is(equalTo(404)));
        assertThat(response.getBody(), is(emptyString()));
    }

    @Test
    void postCallWith500Response() throws Exception {
        stubFor(post(BASE_PATH).willReturn(aResponse().withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)));

        HttpResponse<String> response = httpClient.post(API_URL, Collections.emptyMap(), "sample post body");

        verify(postRequestedFor(urlEqualTo(BASE_PATH)));
        assertThat(response.getStatusCode(), is(equalTo(500)));
        assertThat(response.getBody(), is(emptyString()));
    }

    @Test
    void correctHandlingOfResponseHeaderOnPostRequest() throws Exception {

        stubFor(post(BASE_PATH)
                .willReturn(aResponse().withBody(SAMPLE_RESPONSE_BODY).withHeader("test-header-1", "header-1-value")));

        HttpResponse<String> response = httpClient.post(API_URL, Collections.emptyMap(), "sample post body");

        verify(postRequestedFor(urlEqualTo(BASE_PATH)).withRequestBody(WireMock.equalTo("sample post body")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo(SAMPLE_RESPONSE_BODY)));
        assertThat(response.getHeaders().get("test-header-1").get(0), is(equalTo("header-1-value")));
    }

    @Test
    void postRequestRespectsConfiguredProxySettings() throws Exception {

        wiremockProxy.stubFor(post(BASE_PATH).willReturn(aResponse().proxiedFrom(BASE_URL)));
        stubFor(post(BASE_PATH).willReturn(aResponse().withBody(SAMPLE_RESPONSE_BODY)));

        HttpResponse<String> response = httpClientWithProxy.post(API_URL, Collections.emptyMap(), "sample post body");

        wiremockProxy.verify(postRequestedFor(urlEqualTo(BASE_PATH)));
        verify(postRequestedFor(urlEqualTo(BASE_PATH)).withRequestBody(WireMock.equalTo("sample post body")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo(SAMPLE_RESPONSE_BODY)));
    }

    @Test
    void successfulPutCall() throws Exception {

        stubFor(put(BASE_PATH).willReturn(aResponse().withBody(SAMPLE_RESPONSE_BODY)));

        HttpResponse<String> response = httpClient.put(
                API_URL, Collections.emptyMap(), new ByteArrayInputStream("sample post body".getBytes()));

        verify(putRequestedFor(urlEqualTo(BASE_PATH)).withRequestBody(WireMock.equalTo("sample post body")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo(SAMPLE_RESPONSE_BODY)));
    }

    @Test
    void successfulPutCallFromStream() throws Exception {

        stubFor(put(BASE_PATH).willReturn(aResponse().withBody(SAMPLE_RESPONSE_BODY)));

        HttpResponse<String> response = httpClient.put(
                API_URL, Collections.emptyMap(), new ByteArrayInputStream("sample post body".getBytes()));

        verify(putRequestedFor(urlEqualTo(BASE_PATH)).withRequestBody(WireMock.equalTo("sample post body")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo(SAMPLE_RESPONSE_BODY)));
    }

    @Test
    void successfulPutCallWithOneHeader() throws Exception {

        stubFor(put(BASE_PATH).willReturn(aResponse().withBody(SAMPLE_RESPONSE_BODY)));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("header1", "value1");
        HttpResponse<String> response =
                httpClient.put(API_URL, requestHeaders, new ByteArrayInputStream("sample post body".getBytes()));

        verify(putRequestedFor(urlEqualTo(BASE_PATH))
                .withRequestBody(WireMock.equalTo("sample post body"))
                .withHeader("header1", WireMock.equalTo("value1")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo(SAMPLE_RESPONSE_BODY)));
    }

    @Test
    void successfulPutCallWithManyHeaders() throws Exception {

        stubFor(put(BASE_PATH).willReturn(aResponse().withBody(SAMPLE_RESPONSE_BODY)));

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("header1", "value1");
        requestHeaders.put("header2", "value2");
        requestHeaders.put("header3", "value3");
        HttpResponse<String> response =
                httpClient.put(API_URL, requestHeaders, new ByteArrayInputStream("sample post body".getBytes()));

        verify(putRequestedFor(urlEqualTo(BASE_PATH))
                .withRequestBody(WireMock.equalTo("sample post body"))
                .withHeader("header1", WireMock.equalTo("value1"))
                .withHeader("header2", WireMock.equalTo("value2"))
                .withHeader("header3", WireMock.equalTo("value3")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo(SAMPLE_RESPONSE_BODY)));
    }

    @Test
    void putCallWith404Response() throws Exception {
        stubFor(put(BASE_PATH).willReturn(aResponse().withStatus(HttpURLConnection.HTTP_NOT_FOUND)));

        HttpResponse<String> response = httpClient.put(
                API_URL, Collections.emptyMap(), new ByteArrayInputStream("sample post body".getBytes()));

        verify(putRequestedFor(urlEqualTo(BASE_PATH)));
        assertThat(response.getStatusCode(), is(equalTo(404)));
        assertThat(response.getBody(), is(emptyString()));
    }

    @Test
    void putCallWith500Response() throws Exception {
        stubFor(put(BASE_PATH).willReturn(aResponse().withStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)));

        HttpResponse<String> response = httpClient.put(
                API_URL, Collections.emptyMap(), new ByteArrayInputStream("sample post body".getBytes()));

        verify(putRequestedFor(urlEqualTo(BASE_PATH)));
        assertThat(response.getStatusCode(), is(equalTo(500)));
        assertThat(response.getBody(), is(emptyString()));
    }

    @Test
    void correctHandlingOfResponseHeaderOnPutRequest() throws Exception {

        stubFor(put(BASE_PATH)
                .willReturn(aResponse().withBody(SAMPLE_RESPONSE_BODY).withHeader("test-header-1", "header-1-value")));

        HttpResponse<String> response = httpClient.put(
                API_URL, Collections.emptyMap(), new ByteArrayInputStream("sample post body".getBytes()));

        verify(putRequestedFor(urlEqualTo(BASE_PATH)).withRequestBody(WireMock.equalTo("sample post body")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo(SAMPLE_RESPONSE_BODY)));
        assertThat(response.getHeaders().get("test-header-1").get(0), is(equalTo("header-1-value")));
    }

    @Test
    void putRequestRespectsConfiguredProxySettings() throws Exception {

        wiremockProxy.stubFor(put(BASE_PATH).willReturn(aResponse().proxiedFrom(BASE_URL)));
        stubFor(put(BASE_PATH).willReturn(aResponse().withBody(SAMPLE_RESPONSE_BODY)));

        HttpResponse<String> response = httpClientWithProxy.put(
                API_URL, Collections.emptyMap(), new ByteArrayInputStream("sample post body".getBytes()));

        wiremockProxy.verify(putRequestedFor(urlEqualTo(BASE_PATH)));
        verify(putRequestedFor(urlEqualTo(BASE_PATH)).withRequestBody(WireMock.equalTo("sample post body")));
        assertThat(response.getStatusCode(), is(equalTo(200)));
        assertThat(response.getBody(), is(equalTo(SAMPLE_RESPONSE_BODY)));
    }

    @Test
    void noRequestBodyForPutRequestResultsInException() {
        ClientException thrown = assertThrows(
                ClientException.class,
                () -> httpClient.put(API_URL, Collections.emptyMap(), null),
                "Expected ClientException but none thrown");
        assertThat(thrown.getMessage(), is(equalTo("No request body specified for PUT operation")));
    }

    @Test
    void noRequestBodyForPostRequestResultsInException() {
        ClientException thrown = assertThrows(
                ClientException.class,
                () -> httpClient.post(API_URL, Collections.emptyMap(), null),
                "Expected ClientException but none thrown");
        assertThat(thrown.getMessage(), is(equalTo("No request body specified for POST operation")));
    }

    @Test
    void invalidPathResultsInException() {
        ClientException thrown = assertThrows(
                ClientException.class,
                () -> httpClient.post("not/a/valid/url", Collections.emptyMap(), "test"),
                "Expected ClientException but none thrown");
        assertThat(thrown.getMessage(), is(equalTo("Malformed URL path received")));
    }

    @Test
    void successfulGetStreamCallWithNoHeaders() throws Exception {

        getMethodStub();

        HttpResponse<InputStream> response = httpClient.getInputStream(API_URL, Collections.emptyMap());

        verify(getRequestedFor(urlEqualTo(BASE_PATH)));
        assertThat(response.getStatusCode(), is(equalTo(200)));

        InputStream responseStream = response.getBody();
        String responseText = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        assertThat(responseText, is(equalTo(SAMPLE_RESPONSE_BODY)));
    }

    @Test
    void successfulGetStreamCallWithHeaders() throws Exception {

        getMethodStub();

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("header1", "value1");
        requestHeaders.put("header2", "value2");
        requestHeaders.put("header3", "value3");

        HttpResponse<InputStream> response = httpClient.getInputStream(API_URL, requestHeaders);

        verify(getRequestedFor(urlEqualTo(BASE_PATH))
                .withHeader("header1", WireMock.equalTo("value1"))
                .withHeader("header2", WireMock.equalTo("value2"))
                .withHeader("header3", WireMock.equalTo("value3")));
        assertThat(response.getStatusCode(), is(equalTo(200)));

        InputStream responseStream = response.getBody();
        String responseText = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        assertThat(responseText, is(equalTo(SAMPLE_RESPONSE_BODY)));
    }
}
