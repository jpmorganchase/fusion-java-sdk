package com.jpmorganchase.fusion;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.jpmorganchase.fusion.credential.BearerTokenCredentials;
import com.jpmorganchase.fusion.credential.FusionCredentials;
import com.jpmorganchase.fusion.credential.IFusionCredentials;
import com.jpmorganchase.fusion.credential.OAuthCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.HttpURLConnection;

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

    private IFusionCredentials credentials;
    private FusionAPIManager fusionAPIManager;

    private static final String tokenJson = "{\"access_token\":\"my-oauth-generated-token\",\"token_type\":\"bearer\",\"expires_in\":3600}";

    @BeforeEach
    void setUp(){
        credentials = new BearerTokenCredentials("my-token");
        fusionAPIManager = FusionAPIManager.getAPIManager(credentials);
    }

    @Test
    void successfulGetCall() throws Exception{
        stubFor(get("/test").willReturn(aResponse().withBody("sample response")));

        String response = fusionAPIManager.callAPI("http://localhost:8080/test");

        verify(getRequestedFor(urlEqualTo("/test"))
                .withHeader("Authorization", WireMock.equalTo("Bearer my-token")));
        assertThat(response, is(equalTo("sample response")));
    }

    @Test
    void failureForResourceNotFound() throws Exception{
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
    void successWithOAuthTokenRetrieval() throws Exception{
        credentials = new OAuthCredentials("aClientID", "aClientSecret", "aResource", "http://localhost:8080/oAuth");
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

    //TODO: Obviously points to a need for refactoring
    private static final class TestFusionCredentials extends FusionCredentials {

        public TestFusionCredentials(String aClientID, String aClientSecret, String aResource, String anAuthServerURL) {
            super(aClientID, aClientSecret, aResource, anAuthServerURL);
        }

        @Override
        public boolean useProxy() {
            return false;
        }
    }

}
