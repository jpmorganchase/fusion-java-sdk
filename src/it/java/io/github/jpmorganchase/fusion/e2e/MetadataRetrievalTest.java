package io.github.jpmorganchase.fusion.e2e;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.model.Catalog;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.*;

/*
    Very early Beginnings of end-to-end testing. We need to consider how we want to spin up the stub for Fusion.
    Doing it programatically in Wiremock feels like it's going to be difficult to maintain
    Look at the work on Pact and decide if that's a good fit here?
 */
@Tag("integration")
public class MetadataRetrievalTest {

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().notifier(new Slf4jNotifier(true)))
            .configureStaticDsl(true)
            .build();

    @Test
    void retrieveListOfCatalogsUsingBearerToken() {
        stubFor(get("/catalogs").willReturn(aResponse().withBody(loadTestResource("/e2e-responses/catalog-success-response.json"))));

        Fusion fusion = Fusion.builder()
                .rootURL(wiremock.getRuntimeInfo().getHttpBaseUrl()+"/")
                .bearerToken("my-token")
                .build();


        Map<String, Catalog> catalogMap = fusion.listCatalogs();
        assertThat(catalogMap.size(), is(3));
        assertThat(catalogMap.get("test").getTitle(), is(equalTo("Test data catalog")));
    }

    private static String loadTestResource(String resourceName) {
        URL url = MetadataRetrievalTest.class.getResource(resourceName);
        try {
            Path path = Paths.get(url.toURI());
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data", e);
        }
    }
}
