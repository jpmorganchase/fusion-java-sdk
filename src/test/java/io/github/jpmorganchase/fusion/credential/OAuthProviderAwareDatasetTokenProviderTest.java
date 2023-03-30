package io.github.jpmorganchase.fusion.credential;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OAuthProviderAwareDatasetTokenProviderTest {

    @Mock
    private OAuthSessionTokenProvider sessionTokenProvider;

    @Mock
    private OAuthTokenRetriever tokenRetriever;

    @Mock
    private TimeProvider timeProvider;

    private String fusionRootUrl;

    private Map<String, BearerToken> datasetTokens = new HashMap<>();

    private OAuthProviderAwareDatasetTokenProvider tokenProvider;

    private Credentials datasetCredentials;

    private String datasetBearerToken;

    @Test
    public void testGetDatasetBearerTokenReturnsNewToken() {

        givenFusionRootUrl("https://fusion/");
        givenTheDatasetTokenProvider();
        givenTheBearerTokenFromTheSessionProvider("session-token");
        givenTheDatasetCredentials(
                "session-token", "common", "test", "https://fusion/catalogs/%s/datasets/%s/authorize/token");
        givenTheTokenRetrieverReturnsBearerToken("my-token", "2023-03-30T10:00:00", 3600);

        whenGetDatasetBearerTokenIsCalled("common", "test");

        thenDatasetBearerTokenShouldBeEqualTo("my-token");
    }

    @Test
    public void testGetDatasetBearerTokenReturnsExistingToken() {

        givenFusionRootUrl("https://fusion/");
        givenExistingDatasetToken("common", "test", "common-test-token", "2023-03-30T10:00:00");
        givenTheDatasetTokenProvider();
        givenTimeProviderReturnsCurrentTimeAs("2023-03-30T10:50:00");

        whenGetDatasetBearerTokenIsCalled("common", "test");

        thenDatasetBearerTokenShouldBeEqualTo("common-test-token");
    }

    @Test
    public void testGetDatasetBearerTokenReturnsExistingTokenWhenManyExist() {

        givenFusionRootUrl("https://fusion/");
        givenExistingDatasetToken("common", "abc", "ca-token", "2023-03-30T10:00:00");
        givenExistingDatasetToken("common", "def", "cd-token", "2023-03-30T10:20:00");
        givenExistingDatasetToken("common", "ghi", "cg-token", "2023-03-30T10:33:00");
        givenExistingDatasetToken("foo", "abc", "fa-token", "2023-03-30T09:50:00");
        givenTheDatasetTokenProvider();
        givenTimeProviderReturnsCurrentTimeAs("2023-03-30T10:50:00");

        whenGetDatasetBearerTokenIsCalled("common", "abc");

        thenDatasetBearerTokenShouldBeEqualTo("ca-token");
    }

    @Test
    public void testGetDatasetBearerTokenRefreshesExistingToken() {

        givenFusionRootUrl("https://fusion/");
        givenExistingDatasetToken("common", "test", "common-test-token", "2023-03-30T10:00:00");
        givenTheDatasetTokenProvider();
        givenTimeProviderReturnsCurrentTimeAs("2023-03-30T11:50:00");

        givenTheBearerTokenFromTheSessionProvider("session-token");
        givenTheDatasetCredentials(
                "session-token", "common", "test", "https://fusion/catalogs/%s/datasets/%s/authorize/token");
        givenTheTokenRetrieverReturnsBearerToken("updated-token", "2023-03-30T11:50:00", 3600);

        whenGetDatasetBearerTokenIsCalled("common", "test");

        thenDatasetBearerTokenShouldBeEqualTo("updated-token");
    }

    private void givenTimeProviderReturnsCurrentTimeAs(String currentTime) {
        given(timeProvider.currentTimeMillis()).willReturn(timeInMillis(currentTime));
    }

    private void givenExistingDatasetToken(String catalog, String dataset, String token, String issuedTime) {
        datasetTokens.put(catalog + "_" + dataset, BearerToken.of(token, timeInMillis(issuedTime), 3600));
    }

    private void givenTheDatasetCredentials(String sessionBearerToken, String catalog, String dataset, String authUrl) {
        this.datasetCredentials = OAuthDatasetCredentials.builder()
                .token(sessionBearerToken)
                .catalog(catalog)
                .dataset(dataset)
                .authServerUrl(authUrl)
                .build();
    }

    private void thenDatasetBearerTokenShouldBeEqualTo(String token) {
        assertThat(datasetBearerToken, is(equalTo(token)));
    }

    private void whenGetDatasetBearerTokenIsCalled(String catalog, String dataset) {
        this.datasetBearerToken = tokenProvider.getDatasetBearerToken(catalog, dataset);
    }

    private void givenTheTokenRetrieverReturnsBearerToken(String token, String currentDateTime, int expiresIn) {
        BearerToken bearerToken = BearerToken.of(token, timeInMillis(currentDateTime), expiresIn);
        given(tokenRetriever.retrieve(ArgumentMatchers.eq(datasetCredentials))).willReturn(bearerToken);
    }

    private void givenTheBearerTokenFromTheSessionProvider(String sessionBearerToken) {
        given(sessionTokenProvider.getSessionBearerToken()).willReturn(sessionBearerToken);
    }

    private void givenTheDatasetTokenProvider() {
        this.tokenProvider = new OAuthProviderAwareDatasetTokenProvider(
                fusionRootUrl, sessionTokenProvider, tokenRetriever, timeProvider, datasetTokens);
    }

    private void givenFusionRootUrl(String fusionRootUrl) {
        this.fusionRootUrl = fusionRootUrl;
    }

    private long timeInMillis(String localDateTime) {
        ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.parse(localDateTime), ZoneId.of("UTC"));
        return zdt.toInstant().toEpochMilli();
    }
}
