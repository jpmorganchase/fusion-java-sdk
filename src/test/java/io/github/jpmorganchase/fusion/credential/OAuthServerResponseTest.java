package io.github.jpmorganchase.fusion.credential;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class OAuthServerResponseTest {

    private static final String sampleJson =
            "{\"access_token\":\"my access token\",\"token_type\":\"bearer\",\"expires_in\":3600,\"id_token\":\"my id token\"}";

    private static final OAuthServerResponse expectedResult = OAuthServerResponse.builder()
            .accessToken("my access token")
            .idToken("my id token")
            .expiresIn(3600)
            .tokenType("bearer")
            .build();

    @Test
    void testCreationFromValidJson() {
        OAuthServerResponse response = OAuthServerResponse.fromJson(sampleJson);
        assertThat(response, is(equalTo(expectedResult)));
    }
}
