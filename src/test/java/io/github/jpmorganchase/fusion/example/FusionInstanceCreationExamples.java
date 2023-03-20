package io.github.jpmorganchase.fusion.example;

import io.github.jpmorganchase.fusion.Fusion;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * This class exists to allow us to have working code examples we can link to from documentation
 * e.g. in the README.md for the repo. Having these as tests ensures they will not go out of date
 * as the code changes. If you make changes to this class, please check the documentation still
 * looks correct
 */
public class FusionInstanceCreationExamples {

    private static final String BEARER_TOKEN = "bearer-token-value";

    private static final String CLIENT_ID = "id";
    private static final String CLIENT_SECRET = "secret";
    private static final String RESOURCE = "resource";
    private static final String AUTH_SERVER_URL = "https://auth-server.domain.com/adfs/oauth2/token";

    private static final String CREDENTIAL_FILE_PATH = "";

    @Test
    void createWithBearerToken() {
        Fusion fusion = Fusion.builder().bearerToken(BEARER_TOKEN).build();
    }

    @Test
    void createFromSecretBasedCredentials() {
        Fusion fusion = Fusion.builder()
                .secretBasedCredentials(CLIENT_ID, CLIENT_SECRET, RESOURCE, AUTH_SERVER_URL)
                .build();
    }

    @Test
    @Disabled("Disabling temporarily")
    void loadCredentialsFromFile() {
        Fusion fusion = Fusion.builder().credentialFile(CREDENTIAL_FILE_PATH).build();
    }
}
