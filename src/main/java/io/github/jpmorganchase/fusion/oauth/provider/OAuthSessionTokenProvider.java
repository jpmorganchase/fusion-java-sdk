package io.github.jpmorganchase.fusion.oauth.provider;

import static io.github.jpmorganchase.fusion.oauth.credential.Credentials.CredentialType.BEARER;

import io.github.jpmorganchase.fusion.api.exception.ApiInputValidationException;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.oauth.credential.BearerTokenCredentials;
import io.github.jpmorganchase.fusion.oauth.credential.Credentials;
import io.github.jpmorganchase.fusion.oauth.model.BearerToken;
import io.github.jpmorganchase.fusion.oauth.retriever.OAuthTokenRetriever;
import io.github.jpmorganchase.fusion.oauth.retriever.TokenRetriever;
import io.github.jpmorganchase.fusion.time.SystemTimeProvider;
import io.github.jpmorganchase.fusion.time.TimeProvider;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthSessionTokenProvider implements SessionTokenProvider {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Credentials credentials;
    private final TokenRetriever tokenRetriever;

    private final TimeProvider timeProvider;

    private BearerToken bearerToken;

    @Getter(AccessLevel.PUBLIC)
    private int sessionTokenRefreshes;

    public OAuthSessionTokenProvider(Credentials credentials) {
        this(credentials, new OAuthTokenRetriever(), new SystemTimeProvider());
    }

    public OAuthSessionTokenProvider(Credentials credentials, Client httpClient) {
        this(credentials, new OAuthTokenRetriever(httpClient), new SystemTimeProvider());
    }

    public OAuthSessionTokenProvider(
            Credentials credentials, TokenRetriever oAuthTokenRetriever, TimeProvider timeProvider) {

        this.credentials = credentials;
        this.tokenRetriever = oAuthTokenRetriever;
        this.timeProvider = timeProvider;

        if (BEARER.equals(credentials.getCredentialType())) {
            this.bearerToken = BearerToken.of((BearerTokenCredentials) credentials);
        }
    }

    @Override
    public final synchronized String getSessionBearerToken() {

        if (Objects.isNull(bearerToken) || bearerToken.hasTokenExpired(timeProvider.currentTimeMillis())) {
            bearerToken = tokenRetriever.retrieve(credentials);

            sessionTokenRefreshes++;
            logger.info("Token expires at: {}", bearerToken.getPrettyExpiryTime());
            logger.info("Number of token refreshes: {}", this.sessionTokenRefreshes);
        }
        return bearerToken.getToken();
    }

    @Override
    public void updateCredentials(Credentials credentials) {
        if (BEARER.equals(credentials.getCredentialType())) {
            this.credentials = credentials;
            this.bearerToken = BearerToken.of((BearerTokenCredentials) credentials);
        } else {
            throw new ApiInputValidationException(String.format(
                    "Cannot update bearer token for credentials of type %s",
                    credentials.getClass().getName()));
        }
    }
}
