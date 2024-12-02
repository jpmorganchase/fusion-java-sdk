package io.github.jpmorganchase.fusion.packaging;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.FusionConfiguration;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@ExtendWith(WireMockExtension.class)
public class BaseOperationsIT {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @RegisterExtension
    public static WireMockExtension wireMockRule = WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig().dynamicPort()).build();

    @Getter
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

}
