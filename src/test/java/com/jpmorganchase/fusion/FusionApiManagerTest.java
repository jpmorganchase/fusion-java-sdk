package com.jpmorganchase.fusion;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.jpmorganchase.fusion.credential.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FusionApiManagerTest {

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
            .options(wireMockConfig().port(8080).notifier(new ConsoleNotifier(true))) //TODO: Remove this fixed port and use the default of a random port
            .configureStaticDsl(true)
            .build();

    private FusionCredentials credentials;
    private FusionAPIManager fusionAPIManager;

    private static final String tokenJson = "{\"access_token\":\"my-oauth-generated-token\",\"token_type\":\"bearer\",\"expires_in\":3600}";

    @BeforeEach
    void setUp() {
        credentials = new BearerTokenCredentials("my-token");
        fusionAPIManager = FusionAPIManager.getAPIManager(credentials);
    }

    @Test
    void successfulGetCall() throws Exception {
        stubFor(get("/test").willReturn(aResponse().withBody("sample response")));

        String response = fusionAPIManager.callAPI("http://localhost:8080/test");

        verify(getRequestedFor(urlEqualTo("/test"))
                .withHeader("Authorization", WireMock.equalTo("Bearer my-token")));
        assertThat(response, is(equalTo("sample response")));
    }

    @Test
    void failureForResourceNotFound() throws Exception {
        stubFor(get("/test").willReturn(aResponse().withStatus(HttpURLConnection.HTTP_NOT_FOUND)));

        APICallException thrown = assertThrows(APICallException.class,
                () -> fusionAPIManager.callAPI("http://localhost:8080/test"),
                "Expected APICallException but none thrown"
        );

        verify(getRequestedFor(urlEqualTo("/test"))
                .withHeader("Authorization", WireMock.equalTo("Bearer my-token")));
        assertThat(thrown.getMessage(), is(equalTo("The requested resource does not exist.")));
    }

    //TODO: This can be moved once we finish refactoring
    //TODO: Need tests around the expiry time logic
    @Test
    void successWithOAuthTokenRetrieval() throws Exception {
        credentials = new OAuthSecretBasedCredentials("aClientID", "aClientSecret", "aResource", "http://localhost:8080/oAuth");
        fusionAPIManager = FusionAPIManager.getAPIManager(credentials);

        stubFor(post("/oAuth").willReturn(aResponse()
                .withStatus(HttpURLConnection.HTTP_OK)
                .withBody(tokenJson)));
        stubFor(get("/test").willReturn(aResponse().withBody("sample response")));

        String response = fusionAPIManager.callAPI("http://localhost:8080/test");

        verify(postRequestedFor(urlEqualTo("/oAuth"))
                .withHeader("Authorization", WireMock.equalTo("Basic YUNsaWVudElEOmFDbGllbnRTZWNyZXQ="))
                .withHeader("Accept", WireMock.equalTo("application/json"))
                .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(WireMock.equalTo("grant_type=client_credentials&aud=aResource")));

        verify(getRequestedFor(urlEqualTo("/test"))
                .withHeader("Authorization", WireMock.equalTo("Bearer my-oauth-generated-token")));

        assertThat(response, is(equalTo("sample response")));
    }

    //TODO: This can be moved once we finish refactoring
    //String aClientID, String username, String password, String aResource, String anAuthServerURL
    @Test
    void successWithOAuthPasswordBasedTokenRetrieval() throws Exception {
        credentials = new OAuthPasswordBasedCredentials("aClientID", "aUsername", "aPassword", "aResource", "http://localhost:8080/oAuth");
        fusionAPIManager = FusionAPIManager.getAPIManager(credentials);

        stubFor(post("/oAuth").willReturn(aResponse()
                .withStatus(HttpURLConnection.HTTP_OK)
                .withBody(tokenJson)));
        stubFor(get("/test").willReturn(aResponse().withBody("sample response")));

        String response = fusionAPIManager.callAPI("http://localhost:8080/test");

        verify(postRequestedFor(urlEqualTo("/oAuth"))
                .withHeader("Accept", WireMock.equalTo("application/json"))
                .withHeader("Content-Type", WireMock.equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(WireMock.equalTo("grant_type=password&resource=aResource&client_id=aClientID&username=aUsername&password=aPassword")));

        verify(getRequestedFor(urlEqualTo("/test"))
                .withHeader("Authorization", WireMock.equalTo("Bearer my-oauth-generated-token")));

        assertThat(response, is(equalTo("sample response")));
    }

    @Test
    void successfulFileDownload() throws Exception {
        fusionAPIManager = FusionAPIManager.getAPIManager(credentials);

        File tempOutputFile = File.createTempFile("fusion-test-", ".csv");

        byte[] serverResponse = new String("A,B,C\n1,2,3").getBytes();
        stubFor(get("/test").willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/csv")
                .withHeader("Content-Disposition", "attachment; filename=test-testFile.csv")
                .withBody(serverResponse)));

        fusionAPIManager.callAPIFileDownload("http://localhost:8080/test", tempOutputFile.getAbsolutePath());

        verify(getRequestedFor(urlEqualTo("/test"))
                .withHeader("Authorization", WireMock.equalTo("Bearer my-token")));

        byte[] outputBytes = new byte[(int) tempOutputFile.length()];
        try(FileInputStream fis = new FileInputStream(tempOutputFile)) {
            fis.read(outputBytes);
        }

        assertThat(outputBytes, is(equalTo(serverResponse)));
        tempOutputFile.deleteOnExit();
    }

    @Test
    void successfulFileUpload() throws Exception {
        fusionAPIManager = FusionAPIManager.getAPIManager(credentials);
        stubFor(put("/test").willReturn(aResponse().withStatus(200)));

        fusionAPIManager.callAPIFileUpload("http://localhost:8080/test",
                getPathFromResource("upload-test.csv"),
                "2023-03-01", "2023-03-02", "2023-03-03");

        verify(putRequestedFor(urlEqualTo("/test"))
                .withHeader("accept", WireMock.equalTo("*/*"))
                .withHeader("Authorization", WireMock.equalTo("Bearer my-token"))
                .withHeader("Content-Type", WireMock.equalTo("application/octet-stream"))
                .withHeader("x-jpmc-distribution-from-date", WireMock.equalTo("2023-03-01"))
                .withHeader("x-jpmc-distribution-to-date", WireMock.equalTo("2023-03-02"))
                .withHeader("x-jpmc-distribution-created-date", WireMock.equalTo("2023-03-03"))
                .withHeader("Digest", WireMock.equalTo("md5=SpmtmsY7xMV5dSZdQaLnpA=="))
                .withHeader("Content-Length", WireMock.equalTo("23"))
                .withRequestBody(WireMock.equalTo("A,B,C\n1,2,3\n4,5,6\n7,8,9")));
    }

    private static String getPathFromResource(String resourceName){
        URL url = FusionApiManagerTest.class.getResource(resourceName);
        try {
            Path path = Paths.get(url.toURI());
            return path.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to locate test data", e);
        }
    }

}
