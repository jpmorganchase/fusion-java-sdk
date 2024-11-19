package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.model.Dataset;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

public class DatasetOperations {

    // WireMock server running on port 8080
    @RegisterExtension
    public WireMockExtension wireMockRule = WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig().port(8080)).build();

    private Fusion sdk;

    @BeforeEach
    public void setUp() {
        sdk = Fusion.builder().configuration(FusionConfiguration.builder()
                        .rootURL("http://localhost:8080/v1")
                .build()).build();
    }

    public void testGetDataFromApi() throws IOException, InterruptedException {
        // Stub WireMock to respond with a custom JSON
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/data"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"message\": \"Success\"}")));

        Dataset dataset = Dataset.builder()
                .identifier("SD0001")
                .description("Sample dataset description 1")
                .linkedEntity("SD0001/")
                .title("Sample Dataset 1 | North America")
                .frequency("Daily")
                .build();

        // Call your SDK method
        sdk.create(dataset);

        // Verify the response
        //MatcherAssert.assertThat("{\"message\": \"Success\"}", Matchers.is(Matchers.equalTo("abc")));
    }

}
