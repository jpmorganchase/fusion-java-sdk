package io.github.jpmorganchase.fusion.oauth.provider;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

import io.github.jpmorganchase.fusion.api.ApiInputValidationException;
import io.github.jpmorganchase.fusion.oauth.credential.BearerTokenCredentials;
import io.github.jpmorganchase.fusion.oauth.credential.Credentials;
import io.github.jpmorganchase.fusion.oauth.credential.OAuthPasswordBasedCredentials;
import io.github.jpmorganchase.fusion.oauth.credential.OAuthSecretBasedCredentials;
import io.github.jpmorganchase.fusion.oauth.model.BearerToken;
import io.github.jpmorganchase.fusion.oauth.retriever.TokenRetriever;
import io.github.jpmorganchase.fusion.time.TimeProvider;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
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

    private ApiInputValidationException exception;

    @Test
    public void testNewBearerTokenIsRetrievedWithSecretCredentials() {

        givenSecretCredentials("id", "resource", "secret", "http://auth/token");
        givenSessionTokenProvider();
        givenTheTokenRetrieverReturnsBearerToken("my-token", "2023-03-30T10:00:00", 3600);

        whenGetSessionBearerTokenIsCalled();

        thenSessionBearerTokenShouldBeEqualTo("my-token");
        thenSessionBearerTokenRefreshesShouldBeEqualTo(1);
    }

    @Test
    public void testNewBearerTokenIsRetrievedWithPasswordCredentials() {

        givenPasswordCredentials("id", "resource", "username", "password", "http://auth/token");
        givenSessionTokenProvider();
        givenTheTokenRetrieverReturnsBearerToken("my-token", "2023-03-30T10:00:00", 3600);

        whenGetSessionBearerTokenIsCalled();

        thenSessionBearerTokenShouldBeEqualTo("my-token");
        thenSessionBearerTokenRefreshesShouldBeEqualTo(1);
    }

    @Test
    public void testNewBearerTokenIsRefreshed() {

        givenSecretCredentials("id", "resource", "secret", "http://auth/token");
        givenSessionTokenProvider();
        givenTheTokenRetrieverReturnsBearerTokens(Arrays.asList(
                BearerToken.of("first-token", timeInMillis("2023-03-30T10:00:00"), 3600),
                BearerToken.of("second-token", timeInMillis("2023-03-30T11:00:00"), 3600)));
        givenTimeProviderReturnsCurrentTimeAs("2023-03-30T11:50:00");

        whenGetSessionBearerTokenIsCalled();

        thenSessionBearerTokenShouldBeEqualTo("first-token");
        thenSessionBearerTokenRefreshesShouldBeEqualTo(1);

        whenGetSessionBearerTokenIsCalled();

        thenSessionBearerTokenShouldBeEqualTo("second-token");
        thenSessionBearerTokenRefreshesShouldBeEqualTo(2);
    }

    @Test
    public void testUpdateCredentialsWithBearerTokenCredentials() {
        givenBearerCredentials("my-bearer-token-one");
        givenSessionTokenProvider();
        givenSessionTokenProviderIsUpdatedWithNewCredentials("my-bearer-token-two");

        whenGetSessionBearerTokenIsCalled();

        thenSessionBearerTokenShouldBeEqualTo("my-bearer-token-two");
    }

    @Test
    public void testUpdateCredentialsWithPasswordBasedCredentials() {
        givenPasswordCredentials("id", "resource", "user", "pass", "https://auth/token");
        givenSessionTokenProvider();

        whenAnAttemptIsMadeToUpdateTheCredentialsThenAnExceptionIsExpected();

        thenTheExceptionMessageShouldBeEqualTo("Cannot update bearer token for credentials of type "
                + credentials.getClass().getName());
    }

    @Test
    public void testUpdateCredentialsWithSecretBasedCredentials() {
        givenSecretCredentials("id", "resource", "secret", "http://auth/token");
        givenSessionTokenProvider();

        whenAnAttemptIsMadeToUpdateTheCredentialsThenAnExceptionIsExpected();

        thenTheExceptionMessageShouldBeEqualTo("Cannot update bearer token for credentials of type "
                + credentials.getClass().getName());
    }

    private void thenTheExceptionMessageShouldBeEqualTo(String message) {
        assertThat(exception.getMessage(), is(equalTo(message)));
    }

    private void whenAnAttemptIsMadeToUpdateTheCredentialsThenAnExceptionIsExpected() {
        exception = Assertions.assertThrows(
                ApiInputValidationException.class, () -> tokenProvider.updateCredentials(credentials));
    }

    private void givenSessionTokenProviderIsUpdatedWithNewCredentials(String token) {
        tokenProvider.updateCredentials(new BearerTokenCredentials(token));
    }

    private void givenTimeProviderReturnsCurrentTimeAs(String currentTime) {
        given(timeProvider.currentTimeMillis()).willReturn(timeInMillis(currentTime));
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

    private void givenBearerCredentials(String token) {
        credentials = new BearerTokenCredentials(token);
    }

    private void givenTheTokenRetrieverReturnsBearerTokens(List<BearerToken> tokens) {
        // TODO Change this so that the number of calls is dynamic.
        given(tokenRetriever.retrieve(ArgumentMatchers.eq(credentials)))
                .willReturn(tokens.get(0))
                .willReturn(tokens.get(1));
    }

    private void givenTheTokenRetrieverReturnsBearerToken(String token, String currentDateTime, int expiresIn) {
        BearerToken bearerToken = BearerToken.of(token, timeInMillis(currentDateTime), expiresIn);
        given(tokenRetriever.retrieve(ArgumentMatchers.eq(credentials))).willReturn(bearerToken);
    }

    private void givenPasswordCredentials(
            String id, String resource, String username, String password, String authServerUrl) {
        credentials = new OAuthPasswordBasedCredentials(id, resource, authServerUrl, username, password);
    }

    private long timeInMillis(String localDateTime) {
        ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.parse(localDateTime), ZoneId.of("UTC"));
        return zdt.toInstant().toEpochMilli();
    }
}
