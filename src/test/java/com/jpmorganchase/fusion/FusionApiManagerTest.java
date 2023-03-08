package com.jpmorganchase.fusion;

import com.google.common.collect.Lists;
import com.jpmorganchase.fusion.credential.*;
import com.jpmorganchase.fusion.http.Client;
import com.jpmorganchase.fusion.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FusionApiManagerTest {


    private FusionCredentials credentials;
    private FusionAPIManager fusionAPIManager;
    @Mock
    private Client client;

    private static final String tokenJson = "{\"access_token\":\"my-oauth-generated-token\",\"token_type\":\"bearer\",\"expires_in\":3600}";

    @BeforeEach
    void setUp() {
        credentials = new BearerTokenCredentials("my-token");
        fusionAPIManager = new FusionAPIManager(credentials, client);
    }

    @Test
    void successfulGetCall() throws Exception {
        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("Authorization", "Bearer my-token");
        HttpResponse<String> expectedHttpResponse = HttpResponse.<String>builder()
                        .statusCode(200)
                        .body("sample response")
                        .build();
        when(client.get("http://localhost:8080/test", expectedHeaders)).thenReturn(expectedHttpResponse);

        String response = fusionAPIManager.callAPI("http://localhost:8080/test");

        assertThat(response, is(equalTo("sample response")));
    }

    @Test
    void failureForResourceNotFound() throws Exception {
        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("Authorization", "Bearer my-token");
        HttpResponse<String> expectedHttpResponse = HttpResponse.<String>builder()
                .statusCode(HttpURLConnection.HTTP_NOT_FOUND)
                .build();
        when(client.get("http://localhost:8080/test", expectedHeaders)).thenReturn(expectedHttpResponse);

        APICallException thrown = assertThrows(APICallException.class,
                () -> fusionAPIManager.callAPI("http://localhost:8080/test"),
                "Expected APICallException but none thrown"
        );

        assertThat(thrown.getMessage(), is(equalTo("The requested resource does not exist.")));
    }

    //TODO: This can be moved once we finish refactoring
    //TODO: Need tests around the expiry time logic
    @Test
    void successWithOAuthTokenRetrieval() throws Exception {
        credentials = new OAuthSecretBasedCredentials("aClientID", "aClientSecret", "aResource", "http://localhost:8080/oAuth", client);
        fusionAPIManager = new FusionAPIManager(credentials, client);

        Map<String, String> expectedOAuthHeaders = new HashMap<>();
        expectedOAuthHeaders.put("Authorization", "Basic YUNsaWVudElEOmFDbGllbnRTZWNyZXQ=");
        expectedOAuthHeaders.put("Accept", "application/json");
        expectedOAuthHeaders.put("Content-Type", "application/x-www-form-urlencoded");

        HttpResponse<String> oAuthResponse = HttpResponse.<String>builder()
                .body(tokenJson)
                .build();

        Map<String, String> expectedRequestHeaders = new HashMap<>();
        expectedRequestHeaders.put("Authorization", "Bearer my-oauth-generated-token");

        HttpResponse<String> expectedHttpResponse = HttpResponse.<String>builder()
                .statusCode(200)
                .body("sample response")
                .build();

        when(client.post("http://localhost:8080/oAuth", expectedOAuthHeaders, "grant_type=client_credentials&aud=aResource")).thenReturn(oAuthResponse);
        when(client.get("http://localhost:8080/test", expectedRequestHeaders)).thenReturn(expectedHttpResponse);


        String response = fusionAPIManager.callAPI("http://localhost:8080/test");

        assertThat(response, is(equalTo("sample response")));
    }

    //TODO: This can be moved once we finish refactoring (Can it? I wrote this comment a week ago and am now unconvinced)
    //TODO: This method and the one above are duplicating code - refactor
    @Test
    void successWithOAuthPasswordBasedTokenRetrieval() throws Exception {
        credentials = new OAuthPasswordBasedCredentials("aClientID", "aUsername", "aPassword", "aResource", "http://localhost:8080/oAuth", client);
        fusionAPIManager = new FusionAPIManager(credentials, client);

        Map<String, String> expectedOAuthHeaders = new HashMap<>();
        expectedOAuthHeaders.put("Accept", "application/json");
        expectedOAuthHeaders.put("Content-Type", "application/x-www-form-urlencoded");

        HttpResponse<String> oAuthResponse = HttpResponse.<String>builder()
                .body(tokenJson)
                .build();

        Map<String, String> expectedRequestHeaders = new HashMap<>();
        expectedRequestHeaders.put("Authorization", "Bearer my-oauth-generated-token");

        HttpResponse<String> expectedHttpResponse = HttpResponse.<String>builder()
                .statusCode(200)
                .body("sample response")
                .build();

        when(client.post("http://localhost:8080/oAuth", expectedOAuthHeaders,
                "grant_type=password&resource=aResource&client_id=aClientID&username=aUsername&password=aPassword")).thenReturn(oAuthResponse);
        when(client.get("http://localhost:8080/test", expectedRequestHeaders)).thenReturn(expectedHttpResponse);


        String response = fusionAPIManager.callAPI("http://localhost:8080/test");

        assertThat(response, is(equalTo("sample response")));
    }

    @Test
    void successfulFileDownload() throws Exception {
        fusionAPIManager = new FusionAPIManager(credentials, client);

        File tempOutputFile = File.createTempFile("fusion-test-", ".csv");

        Map<String, String> expectedRequestHeaders = new HashMap<>();
        expectedRequestHeaders.put("Authorization", "Bearer my-token");

        byte[] serverResponse = new String("A,B,C\n1,2,3").getBytes();
        Map<String, List<String>> responseHeaders = new HashMap<>();
        responseHeaders.put("Content-Type", Lists.newArrayList("text/csv"));
        responseHeaders.put("Content-Disposition", Lists.newArrayList("attachment; filename=test-testFile.csv"));
        HttpResponse<InputStream> expectedHttpResponse = HttpResponse.<InputStream>builder()
                .statusCode(200)
                .body(new ByteArrayInputStream(serverResponse))
                .headers(responseHeaders)
                .build();
        when(client.getInputStream("http://localhost:8080/test", expectedRequestHeaders)).thenReturn(expectedHttpResponse);


        fusionAPIManager.callAPIFileDownload("http://localhost:8080/test", tempOutputFile.getAbsolutePath());

        byte[] outputBytes = new byte[(int) tempOutputFile.length()];
        try(FileInputStream fis = new FileInputStream(tempOutputFile)) {
            fis.read(outputBytes);
        }

        assertThat(outputBytes, is(equalTo(serverResponse)));
        tempOutputFile.deleteOnExit();
    }

    @Captor
    ArgumentCaptor<InputStream> fileUploadInputStream;

    @Test
    void successfulFileUpload() throws Exception {
        fusionAPIManager = new FusionAPIManager(credentials, client);

        Map<String, String> expectedRequestHeaders = new HashMap<>();
        expectedRequestHeaders.put("accept", "*/*");
        expectedRequestHeaders.put("Authorization", "Bearer my-token");
        expectedRequestHeaders.put("Content-Type", "application/octet-stream");
        expectedRequestHeaders.put("x-jpmc-distribution-from-date", "2023-03-01");
        expectedRequestHeaders.put("x-jpmc-distribution-to-date", "2023-03-02");
        expectedRequestHeaders.put("x-jpmc-distribution-created-date", "2023-03-03");
        expectedRequestHeaders.put("Digest", "md5=SpmtmsY7xMV5dSZdQaLnpA==");
        expectedRequestHeaders.put("Content-Length", "23");

        when(client.put(any(), any(),any()))
                .thenReturn(HttpResponse.<String>builder().statusCode(200).build());
        //TODO: Now we need to validate the stub properly
                //new ByteArrayInputStream("A,B,C\n1,2,3\n4,5,6\n7,8,9".getBytes())

        fusionAPIManager.callAPIFileUpload("http://localhost:8080/test",
                getPathFromResource("upload-test.csv"),
                "2023-03-01", "2023-03-02", "2023-03-03");
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
