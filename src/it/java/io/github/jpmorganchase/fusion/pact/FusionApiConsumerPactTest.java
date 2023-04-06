package io.github.jpmorganchase.fusion.pact;


import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.model.Catalog;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@Tag("integration")
@ExtendWith(PactConsumerTestExt.class)
public class FusionApiConsumerPactTest {

    private static final String FUSION_API_VERSION = "/v1/";
    private static final String USER_AGENT_VAL = "fusion-java-sdk/UNPACKAGED \\(JdkClient\\) Java/1\\.8\\.0_60";
    private static final String BEARER_TOKEN = "my-bearer-token";
    private static final String AUTH_VAL = "Bearer " + BEARER_TOKEN;

    @Pact(provider = "FusionProvider", consumer = "fusion_sdk_consumer")
    public RequestResponsePact catalogs(PactDslWithProvider builder) {

        PactDslJsonBody b = new PactDslJsonBody();

        b.object("@context")
            .stringType("@vocab", "https://www.w3.org/ns/dcat3.jsondld")
            .stringType("@base", "https://fusion-api.test.aws.jpmchase.net/v1")
            .closeObject()
        .asBody()
        .stringType("@id", "catalogs/")
        .stringType("description", "A list of available catalogs")
        .stringType("identifier", "catalogs")
        .eachLike("resources")
                .stringType("@id", "common")
                .stringType("description", "A catalog of common data")
                .stringType("identifier", "common")
                .stringType("title", "Common data catalog")
        .closeArray();

        return builder
                .given("a list of catalogs")
                .uponReceiving("a request for available catalogs")
                    .path("/v1/catalogs")
                    .matchHeader("Authorization", AUTH_VAL)
                    .matchHeader("User-Agent", USER_AGENT_VAL)
                    .method("GET")
                .willRespondWith()
                .status(200)
                .body(b)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "catalogs")
    void testListCatalogs(MockServer mockServer) {

        Fusion fusion = Fusion.builder().rootURL(mockServer.getUrl() + FUSION_API_VERSION).bearerToken("my-bearer-token").build();

        Map<String, Catalog> catalogs = fusion.listCatalogs();
        assertThat("Catalogs must not be empty", catalogs, Is.is(notNullValue()));

        assertThat("Expected catalog is missing", catalogs.containsKey("common"), is(true));

        Catalog catalog = catalogs.get("common");
        assertThat("Expected catalog is null", catalog, is(notNullValue()));
        assertThat("Expected catalog identifier to match", catalog.getIdentifier(), is(equalTo("common")));
        assertThat("Expected catalog title to match", catalog.getTitle(), is(equalTo("Common data catalog")));
        assertThat("Expected catalog description to match", catalog.getDescription(), is(equalTo("A catalog of common data")));
        assertThat("Expected catalog LinkedEntity to match", catalog.getLinkedEntity(), is(equalTo("common")));

    }




}
