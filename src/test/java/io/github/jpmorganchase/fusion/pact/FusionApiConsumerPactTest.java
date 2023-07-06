package io.github.jpmorganchase.fusion.pact;

import static io.github.jpmorganchase.fusion.pact.util.BodyBuilders.*;
import static io.github.jpmorganchase.fusion.pact.util.RequestResponseHelper.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.github.jpmorganchase.fusion.Fusion;
import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.oauth.provider.FusionTokenProvider;
import io.github.jpmorganchase.fusion.pact.util.FileHelper;
import io.github.jpmorganchase.fusion.parsing.ParsingException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PactConsumerTestExt.class)
public class FusionApiConsumerPactTest {

    private static final String FUSION_API_VERSION = "/v1/";

    Fusion fusion;
    InputStream downloadedFileInputStream;

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact listCatalogs(PactDslWithProvider builder) {
        return getExpectation(builder, "catalogs are available", "a request for catalogs", "/v1/catalogs", catalogs());
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact listCatalogsWhenNoneAreAvailable(PactDslWithProvider builder) {
        return getExpectation(
                builder, "no catalogs are available", "a request for catalogs", "/v1/catalogs", noCatalogs());
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact listCatalogsWhenNotAuthorized(PactDslWithProvider builder) {
        return failedGetExpectation(
                builder,
                "not authorized to list catalogs",
                "a request for catalogs",
                "/v1/catalogs",
                401,
                "Bearer invalid-bearer-token");
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact getCatalogResources(PactDslWithProvider builder) {
        return getExpectation(
                builder,
                "catalog resources exist",
                "a request for catalog resources",
                "/v1/catalogs/common",
                catalogResources());
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact getCatalogResourcesWhenCatalogNotFound(PactDslWithProvider builder) {
        return failedGetExpectation(
                builder,
                "a catalog that does not exist",
                "a request for catalogs resources",
                "/v1/catalogs/alternate",
                404);
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact listProducts(PactDslWithProvider builder) {
        return getExpectation(
                builder,
                "data products exist",
                "a request for a list of data products",
                "/v1/catalogs/common/products",
                products());
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact listProductsWhenNoneExist(PactDslWithProvider builder) {
        return getExpectation(
                builder,
                "no data products exist",
                "a request for a list of data products",
                "/v1/catalogs/common/products",
                noProducts());
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact listDatasets(PactDslWithProvider builder) {
        return getExpectation(
                builder,
                "a list of datasets",
                "a request for a list of datasets",
                "/v1/catalogs/common/datasets",
                datasets());
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact listDatasetsWhenNoneExist(PactDslWithProvider builder) {
        return getExpectation(
                builder,
                "no datasets exist",
                "a request for a list of datasets",
                "/v1/catalogs/common/datasets",
                noDatasets());
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact getDatasetResources(PactDslWithProvider builder) {
        return getExpectation(
                builder,
                "dataset resources are available",
                "a request for a dataset resources",
                "/v1/catalogs/common/datasets/GFI_OP_CF",
                datasetResource());
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact listDatasetMembers(PactDslWithProvider builder) {
        return getExpectation(
                builder,
                "dataset members are available",
                "a request for a list of dataset members",
                "/v1/catalogs/common/datasets/API_TEST/datasetseries",
                datasetMembers());
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact listDatasetMembersWhenNoneExist(PactDslWithProvider builder) {
        return getExpectation(
                builder,
                "no dataset members exist",
                "a request for a list of dataset members",
                "/v1/catalogs/common/datasets/API_TEST/datasetseries",
                noDatasetMembers());
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact getDatasetMemberResources(PactDslWithProvider builder) {
        return getExpectation(
                builder,
                "metadata belonging to a dataset series member",
                "a request for a dataset members metadata",
                "/v1/catalogs/common/datasets/API_TEST/datasetseries/2022-01-16",
                datasetMemberResources());
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact listAttributes(PactDslWithProvider builder) {
        return getExpectation(
                builder,
                "attributes belonging to a dataset",
                "a request for a list of attributes from a dataset",
                "/v1/catalogs/common/datasets/API_TEST/attributes",
                attributes(),
                "application/ld\\+json");
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact listDistributions(PactDslWithProvider builder) {
        return getExpectation(
                builder,
                "distributions available for a dataset member",
                "a request to list available distributions belonging to a dataset member",
                "/v1/catalogs/common/datasets/API_TEST/datasetseries/2022-01-16/distributions",
                distributions());
    }

    @Pact(provider = "110274-fusionapi-provider", consumer = "110274-fusionsdk-consumer")
    public RequestResponsePact download(PactDslWithProvider builder) {
        return downloadExpectation(
                builder,
                "a distribution that is available for download",
                "a request is made to download the distribution",
                "/v1/catalogs/common/datasets/API_TEST/datasetseries/2022-01-16/distributions/csv",
                "A,B,C");
    }

    @AfterEach
    @SneakyThrows
    public void cleanupDirectory() {
        Files.deleteIfExists(Paths.get("downloads/common_API_TEST_2022-01-16.csv"));
    }

    @Test
    @PactTestFor(pactMethod = "listCatalogs")
    void testListCatalogs(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        Map<String, Catalog> catalogs = fusion.listCatalogs();
        assertThat("Catalogs must not be empty", catalogs, Is.is(notNullValue()));

        assertThat("Expected catalog is missing", catalogs.containsKey("common"), is(true));

        Catalog catalog = catalogs.get("common");
        assertThat("Expected catalog is null", catalog, is(notNullValue()));
        assertThat("Expected catalog identifier to match", catalog.getIdentifier(), is(equalTo("common")));
        assertThat("Expected catalog title to match", catalog.getTitle(), is(equalTo("Common data catalog")));
        assertThat(
                "Expected catalog description to match",
                catalog.getDescription(),
                is(equalTo("A catalog of common data")));
        assertThat("Expected catalog LinkedEntity to match", catalog.getLinkedEntity(), is(equalTo("common")));
    }

    @Test
    @PactTestFor(pactMethod = "listCatalogsWhenNoneAreAvailable")
    void testListCatalogsWhenNoneAreAvailable(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        ParsingException ex = Assertions.assertThrows(ParsingException.class, () -> fusion.listCatalogs());
        assertThat(
                "Exception message is incorrect",
                ex.getMessage(),
                is(equalTo("Failed to parse resources from JSON, none found")));
    }

    @Test
    @PactTestFor(pactMethod = "listCatalogsWhenNotAuthorized")
    void testListCatalogsWhenNotAuthorized(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer, "invalid-bearer-token");

        APICallException ex = Assertions.assertThrows(APICallException.class, () -> fusion.listCatalogs());
        assertThat(
                "Exception message is incorrect",
                ex.getMessage(),
                is(equalTo("The bearer token is missing or an invalid bearer token was provided")));
        assertThat("Exception message is incorrect", ex.getResponseCode(), is(equalTo(401)));
    }

    @Test
    @PactTestFor(pactMethod = "getCatalogResources")
    void testCatalogResources(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        Map<String, Map<String, Object>> resources = fusion.catalogResources("common");
        assertThat("Catalog resources must not be empty", resources, Is.is(notNullValue()));

        assertThat("Catalog resource missing resources", resources.size(), greaterThanOrEqualTo(1));

        resources.keySet().forEach(key -> {
            assertThat("Catalog resource missing resource", resources.containsKey(key), is(true));

            Map<String, Object> dataset = resources.get(key);
            assertThat("resource @id is missing from resource", dataset.containsKey("@id"), is(true));
            assertThat("resource description is missing from resource", dataset.containsKey("description"), is(true));
            assertThat("resource identifier is missing from resource", dataset.containsKey("identifier"), is(true));
            assertThat("resource title is missing from resource", dataset.containsKey("title"), is(true));
        });
    }

    @Test
    @PactTestFor(pactMethod = "getCatalogResourcesWhenCatalogNotFound")
    void testCatalogResourcesNotFound(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        APICallException ex =
                Assertions.assertThrows(APICallException.class, () -> fusion.catalogResources("alternate"));

        assertThat(
                "Exception message is incorrect",
                ex.getMessage(),
                is(equalTo("The requested resource does not exist.")));
        assertThat("Exception response code is incorrect", ex.getResponseCode(), is(equalTo(404)));
    }

    @Test
    @PactTestFor(pactMethod = "listProducts")
    void testListProducts(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        Map<String, DataProduct> products = fusion.listProducts("common");
        assertThat("Products must not be empty", products, Is.is(notNullValue()));
        assertThat("Data Products Missing", products.get("ESG00008"), is(notNullValue()));

        DataProduct dataProduct = products.get("ESG00008");
        assertThat(
                "data product description incorrect",
                dataProduct.getDescription(),
                is(equalTo("MSCI ESG Description")));
        assertThat("data product linkedEntity is incorrect", dataProduct.getLinkedEntity(), is(equalTo("ESG00008/")));
        assertThat("data product title is incorrect", dataProduct.getTitle(), is(equalTo("MSCI ESG Ratings")));
        assertThat("data product status is incorrect", dataProduct.getStatus(), is(equalTo("Available")));
        assertThat("data product identifier is incorrect", dataProduct.getIdentifier(), is(equalTo("ESG00008")));
    }

    @Test
    @PactTestFor(pactMethod = "listProductsWhenNoneExist")
    void testListProductsWhenNoneExist(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        ParsingException ex = Assertions.assertThrows(ParsingException.class, () -> fusion.listProducts("common"));
        assertThat(
                "Exception message is incorrect",
                ex.getMessage(),
                is(equalTo("Failed to parse resources from JSON, none found")));
    }

    @Test
    @PactTestFor(pactMethod = "listDatasets")
    void testListDatasets(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        Map<String, Dataset> datasets = fusion.listDatasets("common");
        assertThat("Datasets must not be empty", datasets, Is.is(notNullValue()));
        assertThat("Dataset Missing", datasets.get("GFI_OP_CF"), is(notNullValue()));

        Dataset dataset = datasets.get("GFI_OP_CF");
        assertThat(
                "dataset description incorrect",
                dataset.getDescription(),
                is(equalTo("Premium, volatility, and greeks for EUR")));
        assertThat("dataset linkedEntity is incorrect", dataset.getLinkedEntity(), is(equalTo("GFI_OP_CF/")));
        assertThat("dataset title is incorrect", dataset.getTitle(), is(equalTo("Swaptions Caps & Floors")));
        assertThat("dataset identifier is incorrect", dataset.getIdentifier(), is(equalTo("GFI_OP_CF")));
        assertThat("dataset frequency is incorrect", dataset.getFrequency(), is(equalTo("Daily")));
    }

    @Test
    @PactTestFor(pactMethod = "listDatasetsWhenNoneExist")
    void testListDatasetsWhenNoneExist(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        ParsingException ex = Assertions.assertThrows(ParsingException.class, () -> fusion.listDatasets("common"));
        assertThat(
                "Exception message is incorrect",
                ex.getMessage(),
                is(equalTo("Failed to parse resources from JSON, none found")));
    }

    @Test
    @PactTestFor(pactMethod = "getDatasetResources")
    void testDatasetResources(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        Map<String, Map<String, Object>> datasetResources = fusion.datasetResources("common", "GFI_OP_CF");

        assertThat("dataset resources must not be empty", datasetResources, Is.is(notNullValue()));
        assertThat(
                "dataset resources expected to contain key", datasetResources.containsKey("datasetseries"), is(true));

        Map<String, Object> resource = datasetResources.get("datasetseries");
        assertThat("dataset resource @id is incorrect", resource.get("@id"), is(equalTo("datasetseries/")));
        assertThat(
                "dataset resource description is incorrect",
                resource.get("description"),
                is(equalTo("A list of available datasetseries of a dataset")));
        assertThat(
                "dataset resource identifier is incorrect", resource.get("identifier"), is(equalTo("datasetseries")));
        assertThat("dataset resource title is incorrect", resource.get("title"), is(equalTo("Datasetseries")));
    }

    @Test
    @PactTestFor(pactMethod = "listDatasetMembers")
    void testListDatasetMembers(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        Map<String, DatasetSeries> datasetMembers = fusion.listDatasetMembers("common", "API_TEST");
        assertThat("list of dataset members should not be null", datasetMembers, Is.is(notNullValue()));

        DatasetSeries series = datasetMembers.get("20230319");
        assertThat("dataset series @id is incorrect", series.getIdentifier(), is(equalTo("20230319")));
        assertThat("dataset series linked entity is incorrect", series.getLinkedEntity(), is(equalTo("20230319/")));
        assertThat(
                "dataset series createdDate is incorrect",
                series.getCreatedDate(),
                is(equalTo(LocalDate.of(2023, 3, 19))));
        assertThat(
                "dataset series fromDate is incorrect", series.getFromDate(), is(equalTo(LocalDate.of(2023, 3, 18))));
        assertThat("dataset series toDate is incorrect", series.getToDate(), is(equalTo(LocalDate.of(2023, 3, 17))));
    }

    @Test
    @PactTestFor(pactMethod = "listDatasetMembersWhenNoneExist")
    void testListDatasetMembersWhenNoneExist(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        ParsingException ex =
                Assertions.assertThrows(ParsingException.class, () -> fusion.listDatasetMembers("common", "API_TEST"));
        assertThat(
                "Exception message is incorrect",
                ex.getMessage(),
                is(equalTo("Failed to parse resources from JSON, none found")));
    }

    @Test
    @PactTestFor(pactMethod = "getDatasetMemberResources")
    void testDatasetMemberResources(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        Map<String, Map<String, Object>> datasetMemberResources =
                fusion.datasetMemberResources("common", "API_TEST", "2022-01-16");

        assertThat("dataset member resources must not be empty", datasetMemberResources, Is.is(notNullValue()));
        assertThat(
                "dataset resources expected to contain key",
                datasetMemberResources.containsKey("distributions"),
                is(true));

        Map<String, Object> resource = datasetMemberResources.get("distributions");
        assertThat("dataset resource @id is incorrect", resource.get("@id"), is(equalTo("distributions/")));
        assertThat(
                "dataset resource description is incorrect",
                resource.get("description"),
                is(equalTo("A list of available distributions")));
        assertThat(
                "dataset resource identifier is incorrect", resource.get("identifier"), is(equalTo("distributions")));
        assertThat("dataset resource title is incorrect", resource.get("title"), is(equalTo("Distributions")));
    }

    @Test
    @PactTestFor(pactMethod = "listAttributes")
    void testListAttributes(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        Map<String, Attribute> attributes = fusion.listAttributes("common", "API_TEST");

        assertThat("attributes must not be empty", attributes, Is.is(notNullValue()));
        assertThat("attributes expected to contain key", attributes.containsKey("A"), is(true));

        Attribute attribute = attributes.get("A");
        assertThat("attribute identifier is incorrect", attribute.getIdentifier(), is(equalTo("A")));
        assertThat(
                "attribute description is incorrect",
                attribute.getDescription(),
                is(equalTo("Description for attribute A")));
        assertThat("attribute title is incorrect", attribute.getTitle(), is(equalTo("A")));
        assertThat("attribute index is incorrect", attribute.getIndex(), is(equalTo(1L)));
        assertThat("attribute dataType is incorrect", attribute.getDataType(), is(equalTo("String")));
    }

    @Test
    @PactTestFor(pactMethod = "listAttributes")
    void testListAttributeResources(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        Map<String, Map<String, Object>> resources = fusion.attributeResources("common", "API_TEST");

        assertThat("attribute resources must not be empty", resources, Is.is(notNullValue()));
        assertThat("attribute resources expected to contain key", resources.containsKey("A"), is(true));

        Map<String, Object> resource = resources.get("A");
        assertThat("attribute resource id is incorrect", String.valueOf(resource.get("id")), is(equalTo("4000003.0")));
        assertThat("attribute resource identifier is incorrect", resource.get("identifier"), is(equalTo("A")));
        assertThat("attribute resource source is incorrect", resource.get("source"), is(equalTo("Data Query")));
        assertThat("attribute resource dataType is incorrect", resource.get("dataType"), is(equalTo("String")));
        assertThat(
                "attribute resource description is incorrect",
                resource.get("description"),
                is(equalTo("Description for attribute A")));
        // TODO : Query - gson seems to translate our index integer as a double
        assertThat("attribute resource index is incorrect", resource.get("index"), is(equalTo(1.0)));
        assertThat("attribute resource isDatasetKey is incorrect", resource.get("isDatasetKey"), is(false));
        assertThat(
                "attribute resource sourceFieldId is incorrect",
                resource.get("sourceFieldId"),
                is(equalTo("a_source_field")));
        assertThat("attribute resource title is incorrect", resource.get("title"), is(equalTo("A")));
    }

    @Test
    @PactTestFor(pactMethod = "listDistributions")
    void testListDistributions(MockServer mockServer) {

        givenInstanceOfFusionSdk(mockServer);

        Map<String, Distribution> distributions = fusion.listDistributions("common", "API_TEST", "2022-01-16");

        assertThat("distributions must not be empty", distributions, Is.is(notNullValue()));
        assertThat("distributions expected to contain key", distributions.containsKey("csv"), is(true));

        Distribution distribution = distributions.get("csv");
        assertThat("distribution identifier is incorrect", distribution.getIdentifier(), is(equalTo("csv")));
        assertThat(
                "distribution description is incorrect",
                distribution.getDescription(),
                is(equalTo("Snapshot data, in tabular, csv format")));
        assertThat("distribution @id is incorrect", distribution.getLinkedEntity(), is(equalTo("csv/")));
        assertThat("distribution title is incorrect", distribution.getTitle(), is(equalTo("CSV")));
        assertThat("distribution fileExtension is incorrect", distribution.getFileExtension(), is(equalTo(".csv")));
        assertThat(
                "distribution mediaType is incorrect",
                distribution.getMediaType(),
                is(equalTo("text/csv; header=present; charset=utf-8")));
    }

    @SneakyThrows
    private void thenTheFileContentsShouldBeEqualTo(String expected) {
        String actual = FileHelper.readContentsFromStream(downloadedFileInputStream);
        assertThat("download file does not match expected", actual, is(equalTo(expected)));
    }

    @SneakyThrows
    private void thenTheFileShouldBeDownloaded(String path) {
        downloadedFileInputStream = Files.newInputStream(Paths.get(path));
        assertThat(
                "downloaded file is unavailable", downloadedFileInputStream.available(), is(greaterThanOrEqualTo(1)));
    }

    private void givenInstanceOfFusionSdk(MockServer mockServer) {
        givenInstanceOfFusionSdk(mockServer, "my-bearer-token");
    }

    private void givenInstanceOfFusionSdk(MockServer mockServer, String bearerToken) {

        fusion = Fusion.builder()
                .configuration(FusionConfiguration.builder()
                        .rootURL(mockServer.getUrl() + FUSION_API_VERSION)
                        .build())
                .fusionTokenProvider(new DummyFusionTokenProvider(bearerToken, "my-fusion-bearer"))
                .build();
    }

    @AllArgsConstructor
    private static class DummyFusionTokenProvider implements FusionTokenProvider {

        private String sessionBearerToken;
        private String datasetBearerToken;


        public String getDatasetBearerToken(String catalog, String dataset) {
            return datasetBearerToken;
        }

        public String getSessionBearerToken() {
            return sessionBearerToken;
        }

    }

}
