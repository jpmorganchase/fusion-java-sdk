package io.github.jpmorganchase.fusion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

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
                .bearerToken("my token")
                .build();

        assertThat(f.getRootURL(), is(equalTo(FusionConfiguration.builder().build().getRootURL())));
    }

    @Test
    public void constructionWithUrlDoesNotUseDefaultUrl() {
        String newRootUrl = "https://my-api.domain.com/api";
        Fusion f = Fusion.builder()
                .configuration(FusionConfiguration.builder().rootURL("https://my-api.domain.com/api").build())
                .bearerToken("my token")
                .build();
        assertThat(f.getRootURL(), is(equalTo(newRootUrl)));
    }

    @Test
    public void constructionWithNoPathUsesDefaultPath() {
        Fusion f = Fusion.builder()
                .bearerToken("my token")
                .build();

        assertThat(f.getDefaultPath(), is(equalTo(FusionConfiguration.builder().build().getDownloadPath())));
    }

    @Test
    public void constructionWithPathDoesNotUseDefaultPath() {
        String newPath = "/download/to/here";
        Fusion f = Fusion.builder()
                .configuration(FusionConfiguration.builder().downloadPath("/download/to/here").build())
                .bearerToken("my token")
                .build();
        assertThat(f.getDefaultPath(), is(equalTo(newPath)));
    }

    @Test
    public void constructionWithNoCatalogDefaultsToCommon() {
        Fusion f = Fusion.builder()
                .bearerToken("my token")
                .build();
        assertThat(f.getDefaultCatalog(), is(equalTo(FusionConfiguration.builder().build().getDefaultCatalog())));
    }

    @Test
    public void constructionWithCatalogUsesCorrectDefaultCatalog() {
        Fusion f = Fusion.builder()
                .configuration(FusionConfiguration.builder().defaultCatalog("test").build())
                .bearerToken("my token")
                .build();
        assertThat(f.getDefaultCatalog(), is(equalTo("test")));
    }

    @Test
    public void constructWithBearerToken() {
        //TODO : Should be moved to FusionTokenProvider
/*        Fusion f = Fusion.builder().bearerToken("my-token").build();
        assertThat(f.getCredentials() instanceof BearerTokenCredentials, is(true));*/
    }

    @Test
    public void constructWithSecretBasedCredentials() {
        //TODO : Should be moved to FusionTokenProvider
/*        Fusion f = Fusion.builder()
                .secretBasedCredentials("aClientId", "aSecret", "aResource", "https://oauth-api.domain.com")
                .build();
        assertThat(f.getCredentials() instanceof OAuthSecretBasedCredentials, is(true));
        OAuthSecretBasedCredentials credentials = (OAuthSecretBasedCredentials) f.getCredentials();
        assertThat(credentials.getClientId(), is(equalTo("aClientId")));
        assertThat(credentials.getResource(), is(equalTo("aResource")));
        assertThat(credentials.getAuthServerUrl(), is(equalTo("https://oauth-api.domain.com")));
        assertThat(credentials.getClientSecret(), is(equalTo("aSecret")));*/
    }

    @Test
    public void constructWithPasswordBasedCredentials() {
        //TODO : Should be moved to FusionTokenProvider
/*        Fusion f = Fusion.builder()
                .passwordBasedCredentials(
                        "aClientId", "aUsername", "aPassword", "aResource", "https://oauth-api.domain.com")
                .build();
        assertThat(f.getCredentials() instanceof OAuthPasswordBasedCredentials, is(true));
        OAuthPasswordBasedCredentials credentials = (OAuthPasswordBasedCredentials) f.getCredentials();
        assertThat(credentials.getClientId(), is(equalTo("aClientId")));
        assertThat(credentials.getResource(), is(equalTo("aResource")));
        assertThat(credentials.getAuthServerUrl(), is(equalTo("https://oauth-api.domain.com")));
        assertThat(credentials.getUsername(), is(equalTo("aUsername")));
        assertThat(credentials.getPassword(), is(equalTo("aPassword")));*/
    }

    @Test
    public void constructWithCredentialFile() {
        //TODO : Should be moved to FusionTokenProvider
  /*      URL url = FusionBuilderTest.class.getResource("test-user-credential.json");
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
        assertThat(credentials.getClientSecret(), is(equalTo("aClientSecret")));*/
    }

    @Test
    public void constructWithInvalidCredentialFileThrowsException() {
        //TODO : Should be moved to FusionTokenProvider
/*        FusionInitialisationException thrown = assertThrows(
                FusionInitialisationException.class,
                () -> {
                    Fusion.builder().credentialFile("/not/a/valid/file/path").build();
                },
                "Expected FusionInitialisationException but none thrown");
        assertThat(thrown.getCause().getClass(), is(equalTo(NoSuchFileException.class)));*/
    }

    @Test
    public void constructWithProxy() {
        //TODO : Remove, doesn't anything useful;
/*        Fusion f = Fusion.builder()
                .bearerToken("my-token")
                .proxy("http://myproxy.domain.com", 8080)
                .build();*/

        //TODO IK : Once constructed; I don't want to return the credentials; protect them
        //assertThat(f.getCredentials() instanceof BearerTokenCredentials, is(true));
        // TODO: this is somewhat useless unless we can validate that the proxy was actually configured
    }
}
