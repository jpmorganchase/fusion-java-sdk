package io.github.jpmorganchase.fusion.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.google.common.collect.Lists;
import io.github.jpmorganchase.fusion.credential.BearerTokenCredentials;
import io.github.jpmorganchase.fusion.credential.Credentials;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FusionApiManagerTest {

    private Credentials credentials;
    private FusionAPIManager fusionAPIManager;

    @Mock
    private Client client;

    private static final String tokenJson =
            "{\"access_token\":\"my-oauth-generated-token\",\"token_type\":\"bearer\",\"expires_in\":3600}";

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

        APICallException thrown = assertThrows(
                APICallException.class,
                () -> fusionAPIManager.callAPI("http://localhost:8080/test"),
                "Expected APICallException but none thrown");

        assertThat(thrown.getMessage(), is(equalTo("The requested resource does not exist.")));
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
        when(client.getInputStream("http://localhost:8080/test", expectedRequestHeaders))
                .thenReturn(expectedHttpResponse);

        fusionAPIManager.callAPIFileDownload("http://localhost:8080/test", tempOutputFile.getAbsolutePath());

        byte[] outputBytes = new byte[(int) tempOutputFile.length()];
        try (FileInputStream fis = new FileInputStream(tempOutputFile)) {
            fis.read(outputBytes);
        }

        assertThat(outputBytes, is(equalTo(serverResponse)));
        tempOutputFile.deleteOnExit();
    }

    @Test
    void successfulFileDownloadAsStream() throws Exception {
        fusionAPIManager = new FusionAPIManager(credentials, client);

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
        when(client.getInputStream("http://localhost:8080/test", expectedRequestHeaders))
                .thenReturn(expectedHttpResponse);

        InputStream responseStream = fusionAPIManager.callAPIFileDownload("http://localhost:8080/test");

        byte[] outputBytes = new byte[(int) serverResponse.length];
        responseStream.read(outputBytes);

        assertThat(outputBytes, is(equalTo(serverResponse)));
    }

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

        when(client.put(any(), any(), any()))
                .thenReturn(HttpResponse.<String>builder().statusCode(200).build());
        // TODO: Now we need to validate the stub properly
        // new ByteArrayInputStream("A,B,C\n1,2,3\n4,5,6\n7,8,9".getBytes())

        fusionAPIManager.callAPIFileUpload(
                "http://localhost:8080/test",
                getPathFromResource("upload-test.csv"),
                "2023-03-01",
                "2023-03-02",
                "2023-03-03");
    }

    @Test
    void successfulFileUploadWithStream() throws Exception {
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

        when(client.put(any(), any(), any()))
                .thenReturn(HttpResponse.<String>builder().statusCode(200).build());
        // TODO: Now we need to validate the stub properly
        // new ByteArrayInputStream("A,B,C\n1,2,3\n4,5,6\n7,8,9".getBytes())

        fusionAPIManager.callAPIFileUpload(
                "http://localhost:8080/test",
                Files.newInputStream(Paths.get(getPathFromResource("upload-test.csv"))),
                "2023-03-01",
                "2023-03-02",
                "2023-03-03");
    }

    private static String getPathFromResource(String resourceName) {
        URL url = FusionApiManagerTest.class.getResource(resourceName);
        try {
            Path path = Paths.get(url.toURI());
            return path.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to locate test data", e);
        }
    }
}
