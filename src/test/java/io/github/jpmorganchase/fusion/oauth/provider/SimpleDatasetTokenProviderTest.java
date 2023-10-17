package io.github.jpmorganchase.fusion.oauth.provider;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.jpmorganchase.fusion.oauth.exception.OAuthException;
import io.github.jpmorganchase.fusion.oauth.model.BearerToken;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

@SuppressWarnings("SameParameterValue")
public class SimpleDatasetTokenProviderTest {

    private SimpleDatasetTokenProvider tokenProvider;

    private Map<String, BearerToken> datasetTokens = new HashMap<>();

    private String actualToken;

    private OAuthException oAuthException;

    @Test
    public void testGetDatasetBearerToken_whenValidTokenExists_thenReturnToken() {
        givenDatasetTokens("catalog1_dataset1", "token1");
        givenTokenProvider();

        whenGetDatasetBearerTokenIsCalled("catalog1", "dataset1");

        thenTokenShouldMatch("token1");
    }

    @Test
    public void testGetDatasetBearerToken_whenInvalidTokenExists_thenThrowOAuthException() {
        givenTokenProvider();
        whenGetDatasetBearerTokenIsCalledExceptionIsExpected("catalog3", "dataset1");
        thenExceptionMessageShouldMatch("Bearer token not found for catalog catalog3 and dataset dataset1");
    }

    @Test
    public void testGetDatasetBearerToken_whenTokenDoesNotExist_thenThrowOAuthException() {
        givenTokenProvider();
        whenGetDatasetBearerTokenIsCalledExceptionIsExpected("catalog1", "dataset3");
        thenExceptionMessageShouldMatch("Bearer token not found for catalog catalog1 and dataset dataset3");
    }

    private void thenExceptionMessageShouldMatch(String expected) {
        assertThat("Exception Message does not match", oAuthException.getMessage(), is(equalTo(expected)));
    }

    private void whenGetDatasetBearerTokenIsCalledExceptionIsExpected(String catalog, String dataset) {
        oAuthException =
                assertThrows(OAuthException.class, () -> tokenProvider.getDatasetBearerToken(catalog, dataset));
    }

    private void thenTokenShouldMatch(String expected) {
        assertThat("Token does not match", actualToken, is(equalTo(expected)));
    }

    private void whenGetDatasetBearerTokenIsCalled(String catalog, String dataset) {
        actualToken = tokenProvider.getDatasetBearerToken(catalog, dataset);
    }

    private void givenDatasetTokens(String tokenKey, String token) {
        datasetTokens.put(tokenKey, BearerToken.of(token));
    }

    private void givenTokenProvider() {
        tokenProvider = new SimpleDatasetTokenProvider(datasetTokens);
    }
}
