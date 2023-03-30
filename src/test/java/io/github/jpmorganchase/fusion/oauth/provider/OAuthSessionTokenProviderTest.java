package io.github.jpmorganchase.fusion.oauth.provider;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

import io.github.jpmorganchase.fusion.oauth.credential.Credentials;
import io.github.jpmorganchase.fusion.oauth.credential.OAuthSecretBasedCredentials;
import io.github.jpmorganchase.fusion.oauth.model.BearerToken;
import io.github.jpmorganchase.fusion.oauth.retriever.TokenRetriever;
import io.github.jpmorganchase.fusion.time.TimeProvider;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OAuthSessionTokenProviderTest {

    private OAuthSessionTokenProvider tokenProvider;

    private Credentials credentials;

    @Mock
    private TokenRetriever tokenRetriever;

    @Mock
    private TimeProvider timeProvider;

    private String sessionBearerToken;

    @Test
    public void testNewBearerTokenIsRetrievedWithSecretCredentials() {

        givenSecretCredentials("id", "resource", "secret", "http://auth/token");
        givenSessionTokenProvider();
        givenTheTokenRetrieverReturnsBearerToken("my-token", "2023-03-30T10:00:00", 3600);

        whenGetSessionBearerTokenIsCalled();

        thenSessionBearerTokenShouldBeEqualTo("my-token");
        thenSessionBearerTokenRefreshesShouldBeEqualTo(1);
    }

    private void thenSessionBearerTokenRefreshesShouldBeEqualTo(int refreshCount) {
        assertThat(tokenProvider.getSessionTokenRefreshes(), is(equalTo(refreshCount)));
    }

    private void thenSessionBearerTokenShouldBeEqualTo(String token) {
        assertThat(sessionBearerToken, is(equalTo(token)));
    }

    private void whenGetSessionBearerTokenIsCalled() {
        sessionBearerToken = tokenProvider.getSessionBearerToken();
    }

    private void givenSessionTokenProvider() {
        tokenProvider = new OAuthSessionTokenProvider(credentials, tokenRetriever, timeProvider);
    }

    private void givenSecretCredentials(String id, String resource, String secret, String authServerUrl) {
        credentials = new OAuthSecretBasedCredentials(id, resource, authServerUrl, secret);
    }

    private void givenTheTokenRetrieverReturnsBearerToken(String token, String currentDateTime, int expiresIn) {
        BearerToken bearerToken = BearerToken.of(token, timeInMillis(currentDateTime), expiresIn);
        given(tokenRetriever.retrieve(ArgumentMatchers.eq(credentials))).willReturn(bearerToken);
    }

    private long timeInMillis(String localDateTime) {
        ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.parse(localDateTime), ZoneId.of("UTC"));
        return zdt.toInstant().toEpochMilli();
    }
}
