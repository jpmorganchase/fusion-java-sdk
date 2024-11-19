package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.model.Dataset;
import io.github.jpmorganchase.fusion.test.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;

@ExtendWith(WireMockExtension.class)
public class DatasetOperationsIT {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @RegisterExtension
    public static WireMockExtension wireMockRule = WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig().dynamicPort()).build();

    private Fusion sdk;

    @BeforeEach
    public void setUp() {
        int port = wireMockRule.getRuntimeInfo().getHttpPort();
        logger.debug("Wiremock is configured to port {}", port);

        sdk = Fusion.builder()
                .bearerToken("my-token")
                .configuration(FusionConfiguration.builder()
                        .rootURL("http://localhost:" + port + "/")
                .build()).build();
    }

    @Test
    public void testCreateDataset() {
        // Given
        wireMockRule.stubFor(WireMock.post(WireMock.urlEqualTo("/catalogs/common/datasets/SD0001"))
                .withRequestBody(equalToJson(TestUtils.loadJsonForIt("dataset/dataset_create_request.json")))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("dataset/dataset_create_response.json")));

        Dataset dataset = Dataset.builder()
                .identifier("SD0001")
                .description("Sample dataset description 1")
                .linkedEntity("SD0001/")
                .title("Sample Dataset 1 | North America")
                .frequency("Daily")
                .build();

        // When
        sdk.create(dataset);


        // THen Verify the response
    }

}
