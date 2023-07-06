package io.github.jpmorganchase.fusion.example;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.model.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * THIS IS NOT A TEST - it is purely to verify the contract that we have published on the README.md has not changed.
 * If any of these tests break, please ensure you update the corresponding example in the README.md.
 *
 * If there's a better way to do this we should discuss. This is a bit clunky
 *
 */
@ExtendWith(MockitoExtension.class)
public class FusionReadMeExampleVerification {

    private static final String BEARER_TOKEN = "bearer-token-value";
    private static final String CLIENT_ID = "id";
    private static final String CLIENT_SECRET = "secret";
    private static final String RESOURCE = "resource";
    private static final String AUTH_SERVER_URL = "https://auth-server.domain.com/adfs/oauth2/token";
    private static final String CREDENTIAL_FILE_PATH = "";

    @Mock
    private Fusion fusion;

    @Test
    void createWithBearerToken() {
        Fusion.builder().bearerToken(BEARER_TOKEN).build();
    }

    @Test
    void createFromSecretBasedCredentials() {
        Fusion.builder()
                .secretBasedCredentials(CLIENT_ID, CLIENT_SECRET, RESOURCE, AUTH_SERVER_URL)
                .build();
    }

    @Test
    @Disabled("Disabling temporarily")
    void loadCredentialsFromFile() {
        Fusion.builder()
                .configuration(FusionConfiguration.builder()
                        .credentialsPath(CREDENTIAL_FILE_PATH)
                        .build())
                .build();
    }

    @Test
    void listCatalogs() {
        Map<String, Catalog> catalogs = new HashMap<>();
        catalogs.put("my-catalog", Catalog.builder().build());

        given(fusion.listCatalogs()).willReturn(catalogs);
        assertThat(
                "Fusion interface has changed, please correct README.md examples",
                fusion.listCatalogs(),
                is(notNullValue()));
    }

    @Test
    void listDatasets() {
        Map<String, Dataset> datasets = new HashMap<>();
        datasets.put("my-dataset", Dataset.builder().build());

        given(fusion.listDatasets("my-catalog")).willReturn(datasets);
        assertThat(
                "Fusion interface has changed, please correct README.md examples",
                fusion.listDatasets("my-catalog"),
                is(notNullValue()));
    }

    @Test
    void listAttributes() {
        Map<String, Attribute> attributes = new HashMap<>();
        attributes.put("my-attributes", Attribute.builder().build());

        given(fusion.listAttributes("my-catalog", "my-dataset")).willReturn(attributes);
        assertThat(
                "Fusion interface has changed, please correct README.md examples",
                fusion.listAttributes("my-catalog", "my-dataset"),
                is(notNullValue()));
    }

    @Test
    void listDatasetMembers() {
        Map<String, DatasetSeries> series = new HashMap<>();
        series.put("my-dataset-series", DatasetSeries.builder().build());

        given(fusion.listDatasetMembers("my-catalog", "my-dataset")).willReturn(series);
        assertThat(
                "Fusion interface has changed, please correct README.md examples",
                fusion.listDatasetMembers("my-catalog", "my-dataset"),
                is(notNullValue()));
    }

    @Test
    void listDistributions() {
        Map<String, Distribution> series = new HashMap<>();
        series.put("distributions", Distribution.builder().build());

        given(fusion.listDistributions("my-catalog", "my-dataset", "my-series-member"))
                .willReturn(series);
        assertThat(
                "Fusion interface has changed, please correct README.md examples",
                fusion.listDistributions("my-catalog", "my-dataset", "my-series-member"),
                is(notNullValue()));
    }

    @Test
    void downloadAsFile() {
        fusion.download("my-catalog", "my-dataset", "my-series-member", "csv", "/downloads/distributions");
        verify(fusion, times(1))
                .download("my-catalog", "my-dataset", "my-series-member", "csv", "/downloads/distributions");
    }

    @Test
    void downloadAsStream() {
        given(fusion.downloadStream("my-catalog", "my-dataset", "my-series-member", "csv"))
                .willReturn(mock(InputStream.class));
        assertThat(
                "Fusion interface has changed, please correct README.md examples",
                fusion.downloadStream("my-catalog", "my-dataset", "my-series-member", "csv"),
                is(notNullValue()));
    }
}
