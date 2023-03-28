package io.github.jpmorganchase.fusion.credential;

import io.github.jpmorganchase.fusion.api.ApiInputValidationException;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@Builder
public class OAuthCredentialAwareSessionTokenProvider implements OAuthSessionTokenProvider {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Credentials credentials;
    private final OAuthTokenRetriever tokenRetriever;

    private final CredentialType credentialType;

    private final TimeProvider timeProvider;

    private BearerToken bearerToken;

    private int sessionTokenRefreshes;

    public OAuthCredentialAwareSessionTokenProvider(Credentials credentials){
        this(credentials, new OAuthTokenRetriever(), new SystemTimeProvider());
    }

    public OAuthCredentialAwareSessionTokenProvider(Credentials credentials, OAuthTokenRetriever oAuthTokenRetriever, TimeProvider timeProvider){

        this.credentials = credentials;

        if (this.credentials instanceof OAuthSecretBasedCredentials){
            this.credentialType = CredentialType.SECRET;
        } else if ((this.credentials instanceof OAuthPasswordBasedCredentials)){
            this.credentialType = CredentialType.PASSWORD;
        } else {
            this.credentialType = CredentialType.TOKEN;
        }

        this.tokenRetriever = oAuthTokenRetriever;
        this.timeProvider = timeProvider;
    }

    @Override
    public final synchronized String getSessionBearerToken() {

        if (bearerToken.hasTokenExpired(timeProvider.currentTimeMillis())) {

            //TODO Move to Retriever
            switch (credentialType){
                case SECRET:
                    bearerToken = tokenRetriever.retrieveWithSecretCredentials((OAuthSecretBasedCredentials) credentials);
                    break;
                case PASSWORD:
                    bearerToken = tokenRetriever.retrieveWithPasswordCredentials((OAuthPasswordBasedCredentials) credentials);
                    break;
                case TOKEN:
                default:
                    return bearerToken.getToken();
            }

            sessionTokenRefreshes++;
            logger.atInfo()
                    .setMessage("Token expires at: {}")
                    // TODO: This wont necessarily be accurate for a non-standard TimeProvider implementation
                    .addArgument(bearerToken.getPrettyExpiryTime())
                    .log();
            logger.atInfo()
                    .setMessage("Number of token refreshes: {}")
                    .addArgument(this.sessionTokenRefreshes)
                    .log();

        }
        return bearerToken.getToken();
    }

    @Override
    public void updateCredentials(Credentials credentials) {
        if (this.credentials instanceof BearerTokenCredentials) {
            this.credentials = credentials;
        } else {
            throw new ApiInputValidationException(String.format(
                    "Cannot update bearer token for credentials of type %s",
                    credentials.getClass().getName()));
        }
    }

    private enum CredentialType {
        SECRET, PASSWORD, TOKEN
    }

}
