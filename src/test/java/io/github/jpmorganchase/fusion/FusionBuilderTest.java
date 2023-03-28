package io.github.jpmorganchase.fusion;

import io.github.jpmorganchase.fusion.credential.BearerTokenCredentials;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FusionBuilderTest {

    @Test
    public void constructionWithNoCredentialsThrowsException() {
        FusionInitialisationException thrown = assertThrows(
                FusionInitialisationException.class,
                () -> Fusion.builder().build(),
                "Expected FusionInitialisationException but none thrown");
        assertThat(thrown.getMessage(), is(equalTo("No Fusion credentials provided, cannot build Fusion instance")));
    }

    @Test
    public void constructionWithNoUrlUsesDefaultUrl() {
        Fusion f = Fusion.builder()
                .credentials(new BearerTokenCredentials("my token"))
                .build();
        assertThat(f.getRootURL(), is(equalTo(Fusion.DEFAULT_ROOT_URL)));
    }

    @Test
    public void constructionWithUrlDoesNotUseDefaultUrl() {
        String newRootUrl = "https://my-api.domain.com/api";
        Fusion f = Fusion.builder()
                .credentials(new BearerTokenCredentials("my token"))
                .rootURL(newRootUrl)
                .build();
        assertThat(f.getRootURL(), is(equalTo(newRootUrl)));
    }

    @Test
    public void constructionWithNoCatalogDefaultsToCommon() {
        Fusion f = Fusion.builder()
                .credentials(new BearerTokenCredentials("my token"))
                .build();
        assertThat(f.getDefaultCatalog(), is(equalTo(Fusion.DEFAULT_CATALOG)));
    }

    // TODO: A better test is probably to make sure that it gets passed down to the next layer?
    @Test
    public void constructionWithCatalogUsesCorrectDefaultCatalog() {
        Fusion f = Fusion.builder()
                .credentials(new BearerTokenCredentials("my token"))
                .defaultCatalog("test")
                .build();
        assertThat(f.getDefaultCatalog(), is(equalTo("test")));
    }

    @Test
    public void constructWithBearerToken() {
        Fusion f = Fusion.builder().bearerToken("my-token").build();
        assertThat(f.getCredentials() instanceof BearerTokenCredentials, is(true));
    }

    @Test
    public void constructWithSecretBasedCredentials() {
        Fusion f = Fusion.builder()
                .secretBasedCredentials("aClientId", "aSecret", "aResource", "https://oauth-api.domain.com")
                .build();
        assertThat(f.getCredentials() instanceof OAuthSecretBasedCredentials, is(true));
        OAuthSecretBasedCredentials credentials = (OAuthSecretBasedCredentials) f.getCredentials();
        assertThat(credentials.getClientId(), is(equalTo("aClientId")));
        assertThat(credentials.getResource(), is(equalTo("aResource")));
        assertThat(credentials.getAuthServerUrl(), is(equalTo("https://oauth-api.domain.com")));
        assertThat(credentials.getClientSecret(), is(equalTo("aSecret")));
    }

    @Test
    public void constructWithPasswordBasedCredentials() {
        Fusion f = Fusion.builder()
                .passwordBasedCredentials(
                        "aClientId", "aUsername", "aPassword", "aResource", "https://oauth-api.domain.com")
                .build();
        assertThat(f.getCredentials() instanceof OAuthPasswordBasedCredentials, is(true));
        OAuthPasswordBasedCredentials credentials = (OAuthPasswordBasedCredentials) f.getCredentials();
        assertThat(credentials.getClientId(), is(equalTo("aClientId")));
        assertThat(credentials.getResource(), is(equalTo("aResource")));
        assertThat(credentials.getAuthServerUrl(), is(equalTo("https://oauth-api.domain.com")));
        assertThat(credentials.getUsername(), is(equalTo("aUsername")));
        assertThat(credentials.getPassword(), is(equalTo("aPassword")));
    }

    @Test
    public void constructWithCredentialFile() {
        URL url = FusionBuilderTest.class.getResource("test-user-credential.json");
        Path path;
        try {
            path = Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        Fusion f = Fusion.builder()
                .credentialFile(path.toAbsolutePath().toString())
                .build();
        assertThat(f.getCredentials() instanceof OAuthSecretBasedCredentials, is(true));
        OAuthSecretBasedCredentials credentials = (OAuthSecretBasedCredentials) f.getCredentials();
        assertThat(credentials.getClientId(), is(equalTo("aClientId")));
        assertThat(credentials.getResource(), is(equalTo("JPMC:URI:RS-12345-App-ENV")));
        assertThat(credentials.getAuthServerUrl(), is(equalTo("https://authserver.domain.com/as/token.oauth2")));
        assertThat(credentials.getClientSecret(), is(equalTo("aClientSecret")));
    }

    @Test
    public void constructWithInvalidCredentialFileThrowsException() {
        FusionInitialisationException thrown = assertThrows(
                FusionInitialisationException.class,
                () -> {
                    Fusion.builder().credentialFile("/not/a/valid/file/path").build();
                },
                "Expected FusionInitialisationException but none thrown");
        assertThat(thrown.getCause().getClass(), is(equalTo(NoSuchFileException.class)));
    }

    @Test
    public void constructWithProxy() {
        Fusion f = Fusion.builder()
                .bearerToken("my-token")
                .proxy("http://myproxy.domain.com", 8080)
                .build();
        assertThat(f.getCredentials() instanceof BearerTokenCredentials, is(true));
        // TODO: this is somewhat useless unless we can validate that the proxy was actually configured
    }
}
