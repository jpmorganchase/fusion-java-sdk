package io.github.jpmorganchase.fusion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.oauth.credential.BearerTokenCredentials;
import io.github.jpmorganchase.fusion.parsing.APIResponseParser;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FusionTest {

    private static final String TMP_PATH = System.getProperty("java.io.tmpdir");

    @Mock
    private APIManager apiManager;

    @Mock
    private APIResponseParser responseParser;

    @Mock
    private Client httpClient;

    private final FusionConfiguration config = FusionConfiguration.builder().build();

    @Test
    public void testListDatasetsInteraction() throws Exception {
        Fusion f = stubFusion();

        Map<String, Dataset> stubResponse = setupDatasetTest("common");

        Map<String, Dataset> actualResponse = f.listDatasets();
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testListDatasetsInteractionWithContainsForId() throws Exception {
        Fusion f = stubFusion();

        Map<String, Dataset> stubResponse = setupDatasetTest("common");

        Map<String, Dataset> actualResponse = f.listDatasets("common", "dataset1", true);
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testListDatasetsInteractionWithContainsForIdNoMatch() throws Exception {
        Fusion f = stubFusion();

        setupDatasetTest("common");
        Map<String, Dataset> actualResponse = f.listDatasets("common", "dataset2", true);
        assertThat(actualResponse, is(equalTo(new HashMap<>())));
    }

    @Test
    public void testListDatasetsInteractionWithContains() throws Exception {
        Fusion f = stubFusion();

        Map<String, Dataset> stubResponse = setupDatasetTest("common");

        Map<String, Dataset> actualResponse = f.listDatasets("common", "datasetOne", false);
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testListDatasetsInteractionWithContainsNoMatch() throws Exception {
        Fusion f = stubFusion();

        setupDatasetTest("common");
        Map<String, Dataset> actualResponse = f.listDatasets("common", "datasetTwo", false);
        assertThat(actualResponse, is(equalTo(new HashMap<>())));
    }

    @Test
    public void testListDatasetsInteractionWithNonDefaultCatalog() throws Exception {
        Fusion f = stubFusion();

        Map<String, Dataset> stubResponse = setupDatasetTest("other-catalog");

        Map<String, Dataset> actualResponse = f.listDatasets("other-catalog");
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    private Map<String, Dataset> setupDatasetTest(String catalog) throws Exception {

        Map<String, Dataset> stubResponse = new HashMap<>();
        stubResponse.put(
                "dataset1",
                Dataset.builder()
                        .identifier("dataset1")
                        .description("Description datasetOne")
                        .title("Title datasetOne")
                        .build());

        HttpResponse<String> httpResponse = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"key\":\"value\"}]}")
                .headers(new HashMap<>())
                .build();

        when(apiManager.callAPIWithResponse(
                        eq(String.format("%1scatalogs/%2s/datasets", config.getRootURL(), catalog)), anyMap()))
                .thenReturn(httpResponse);
        when(responseParser.parseDatasetResponse("{\"resources\":[{\"key\":\"value\"}]}", catalog))
                .thenReturn(stubResponse);

        return stubResponse;
    }

    @Test
    public void testListProductsInteraction() throws Exception {
        Fusion f = stubFusion();

        Map<String, DataProduct> stubResponse = setupProductTest("common");

        Map<String, DataProduct> actualResponse = f.listProducts();
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testListProductsInteractionWithNonDefaultCatalog() throws Exception {
        Fusion f = stubFusion();

        Map<String, DataProduct> stubResponse = setupProductTest("other-catalog");

        Map<String, DataProduct> actualResponse = f.listProducts("other-catalog");
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    private Map<String, DataProduct> setupProductTest(String catalog) throws Exception {

        Map<String, DataProduct> stubResponse = new HashMap<>();
        stubResponse.put("first", DataProduct.builder().identifier("product1").build());

        HttpResponse<String> httpResponse = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"key\":\"value\"}]}")
                .headers(new HashMap<>())
                .build();

        when(apiManager.callAPIWithResponse(
                        eq(String.format("%1scatalogs/%2s/products", config.getRootURL(), catalog)), anyMap()))
                .thenReturn(httpResponse);
        when(responseParser.parseDataProductResponse("{\"resources\":[{\"key\":\"value\"}]}"))
                .thenReturn(stubResponse);

        return stubResponse;
    }

    @Test
    public void testDatasetSeriesInteraction() throws Exception {
        Fusion f = stubFusion();

        Map<String, DatasetSeries> stubResponse = new HashMap<>();
        stubResponse.put("first", DatasetSeries.builder().identifier("dataset1").build());

        HttpResponse<String> httpResponse = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"key\":\"value\"}]}")
                .headers(new HashMap<>())
                .build();

        when(apiManager.callAPIWithResponse(
                        eq(String.format(
                                "%1scatalogs/%2s/datasets/%3s/datasetseries",
                                config.getRootURL(), "common", "sample_dataset")),
                        anyMap()))
                .thenReturn(httpResponse);
        when(responseParser.parseDatasetSeriesResponse("{\"resources\":[{\"key\":\"value\"}]}"))
                .thenReturn(stubResponse);

        Map<String, DatasetSeries> actualResponse = f.listDatasetMembers("sample_dataset");
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testAttributeInteraction() throws Exception {
        Fusion f = stubFusion();

        Map<String, Attribute> stubResponse = new HashMap<>();
        stubResponse.put("first", Attribute.builder().identifier("attribute1").build());

        HttpResponse<String> httpResponse = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"key\":\"value\"}]}")
                .headers(new HashMap<>())
                .build();

        when(apiManager.callAPIWithResponse(
                        eq(String.format(
                                "%1scatalogs/%2s/datasets/%3s/attributes",
                                config.getRootURL(), "common", "sample_dataset")),
                        anyMap()))
                .thenReturn(httpResponse);
        when(responseParser.parseAttributeResponse("{\"resources\":[{\"key\":\"value\"}]}", "common", "sample_dataset"))
                .thenReturn(stubResponse);

        Map<String, Attribute> actualResponse = f.listAttributes("sample_dataset");
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testAttributeInteractionUntyped() throws Exception {
        Fusion f = stubFusion();

        Map<String, Map<String, Object>> stubResponse = new HashMap<>();
        Map<String, Object> attribute1 = new HashMap<>();
        attribute1.put(
                "attribute1", Attribute.builder().identifier("attribute1").build());
        stubResponse.put("attribute1", attribute1);

        when(apiManager.callAPI(String.format(
                        "%1scatalogs/%2s/datasets/%3s/attributes", config.getRootURL(), "common", "sample_dataset")))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseResourcesUntyped("{\"key\":value}")).thenReturn(stubResponse);

        Map<String, Map<String, Object>> actualResponse = f.attributeResources("common", "sample_dataset");
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testDistributionInteraction() throws Exception {
        Fusion f = stubFusion();

        Map<String, Distribution> stubResponse = new HashMap<>();
        stubResponse.put(
                "first", Distribution.builder().identifier("attribute1").build());

        HttpResponse<String> httpResponse = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"identifier\":\"attribute1\"}]}")
                .headers(new HashMap<>())
                .build();

        when(apiManager.callAPIWithResponse(
                        eq(String.format(
                                "%1scatalogs/%2s/datasets/%3s/datasetseries/%4s/distributions",
                                config.getRootURL(), "common", "sample_dataset", "20230308")),
                        anyMap()))
                .thenReturn(httpResponse);
        when(responseParser.parseDistributionResponse("{\"resources\":[{\"identifier\":\"attribute1\"}]}"))
                .thenReturn(stubResponse);

        Map<String, Distribution> actualResponse = f.listDistributions("sample_dataset", "20230308");
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testFileDownloadInteraction() throws Exception {
        Fusion f = stubFusion();

        doNothing()
                .when(apiManager)
                .callAPIFileDownload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                                config.getRootURL(), "common", "sample_dataset", "20230308", "csv"),
                        String.format("%s/%s_%s_%s.%s", TMP_PATH, "common", "sample_dataset", "20230308", "csv"),
                        "common",
                        "sample_dataset",
                        new HashMap<>());

        f.download("common", "sample_dataset", "20230308", "csv", TMP_PATH);
    }

    @Test
    public void testFileDownloadInteractionWithDefaultPath() throws Exception {
        Fusion f = stubFusion();

        doNothing()
                .when(apiManager)
                .callAPIFileDownload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                                config.getRootURL(), "common", "sample_dataset", "20230308", "csv"),
                        String.format("%s/%s_%s_%s.%s", "downloads", "common", "sample_dataset", "20230308", "csv"),
                        "common",
                        "sample_dataset",
                        new HashMap<>());

        f.download("common", "sample_dataset", "20230308", "csv");
    }

    @Test
    public void testFileDownloadAsStreamInteraction() throws Exception {
        Fusion f = stubFusion();

        when(apiManager.callAPIFileDownload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                                config.getRootURL(), "common", "sample_dataset", "20230308", "csv"),
                        "common",
                        "sample_dataset",
                        new HashMap<>()))
                .thenReturn(new ByteArrayInputStream("A,B,C\nD,E,F".getBytes()));

        InputStream response = f.downloadStream("common", "sample_dataset", "20230308", "csv");

        String responseText = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        assertThat(responseText, is(equalTo("A,B,C\nD,E,F")));
    }

    @Test
    public void testFileUploadInteraction() throws Exception {
        Fusion f = stubFusion();
        LocalDate d = LocalDate.of(2023, 3, 9);

        f.upload("common", "sample_dataset", "20230308", "csv", "/tmp/file.csv", d);

        verify(apiManager)
                .callAPIFileUpload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                                config.getRootURL(), "common", "sample_dataset", "20230308", "csv"),
                        "/tmp/file.csv",
                        "common",
                        "sample_dataset",
                        "2023-03-09",
                        "2023-03-09",
                        "2023-03-09",
                        new HashMap<>());
    }

    @Test
    public void testFileUploadInteractionWithHeaders() throws Exception {
        Fusion f = stubFusion();
        LocalDate d = LocalDate.of(2023, 3, 9);
        Map<String, String> headers = new HashMap<>();
        headers.put("my-header-0", "my-value-0");

        f.upload("common", "sample_dataset", "20230308", "csv", "/tmp/file.csv", d, headers);

        verify(apiManager)
                .callAPIFileUpload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                                config.getRootURL(), "common", "sample_dataset", "20230308", "csv"),
                        "/tmp/file.csv",
                        "common",
                        "sample_dataset",
                        "2023-03-09",
                        "2023-03-09",
                        "2023-03-09",
                        headers);
    }

    @Test
    public void testFileUploadInteractionWithAllDates() throws Exception {
        Fusion f = stubFusion();
        LocalDate fDate = LocalDate.of(2023, 3, 9);
        LocalDate tDate = LocalDate.of(2023, 3, 10);
        LocalDate cDate = LocalDate.of(2023, 3, 11);

        f.upload("common", "sample_dataset", "20230308", "csv", "/tmp/file.csv", fDate, tDate, cDate);

        verify(apiManager)
                .callAPIFileUpload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                                config.getRootURL(), "common", "sample_dataset", "20230308", "csv"),
                        "/tmp/file.csv",
                        "common",
                        "sample_dataset",
                        "2023-03-09",
                        "2023-03-10",
                        "2023-03-11",
                        new HashMap<>());
    }

    @Test
    public void testFileUploadInteractionWithAllDatesAndHeaders() throws Exception {
        Fusion f = stubFusion();
        LocalDate fDate = LocalDate.of(2023, 3, 9);
        LocalDate tDate = LocalDate.of(2023, 3, 10);
        LocalDate cDate = LocalDate.of(2023, 3, 11);
        Map<String, String> headers = new HashMap<>();
        headers.put("my-header-0", "my-value-0");

        f.upload("common", "sample_dataset", "20230308", "csv", "/tmp/file.csv", fDate, tDate, cDate, headers);

        verify(apiManager)
                .callAPIFileUpload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                                config.getRootURL(), "common", "sample_dataset", "20230308", "csv"),
                        "/tmp/file.csv",
                        "common",
                        "sample_dataset",
                        "2023-03-09",
                        "2023-03-10",
                        "2023-03-11",
                        headers);
    }

    @Test
    public void testFileUploadAsStreamInteraction() throws Exception {
        Fusion f = stubFusion();
        LocalDate d = LocalDate.of(2023, 3, 9);

        InputStream requestBodyStream = new ByteArrayInputStream("A,B,C\nD,E,F".getBytes());

        f.upload("common", "sample_dataset", "20230308", "csv", requestBodyStream, d);

        verify(apiManager)
                .callAPIFileUpload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                                config.getRootURL(), "common", "sample_dataset", "20230308", "csv"),
                        requestBodyStream,
                        "common",
                        "sample_dataset",
                        "2023-03-09",
                        "2023-03-09",
                        "2023-03-09",
                        new HashMap<>());
    }

    @Test
    public void testFileUploadAsStreamInteractionWithHeaders() throws Exception {
        Fusion f = stubFusion();
        LocalDate d = LocalDate.of(2023, 3, 9);
        Map<String, String> headers = new HashMap<>();
        headers.put("my-header-0", "my-value-0");

        InputStream requestBodyStream = new ByteArrayInputStream("A,B,C\nD,E,F".getBytes());

        f.upload("common", "sample_dataset", "20230308", "csv", requestBodyStream, d, headers);

        verify(apiManager)
                .callAPIFileUpload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                                config.getRootURL(), "common", "sample_dataset", "20230308", "csv"),
                        requestBodyStream,
                        "common",
                        "sample_dataset",
                        "2023-03-09",
                        "2023-03-09",
                        "2023-03-09",
                        headers);
    }

    @Test
    public void testFileUploadAsStreamInteractionWithHeadersAllDates() throws Exception {
        Fusion f = stubFusion();
        LocalDate fDate = LocalDate.of(2023, 3, 9);
        LocalDate tDate = LocalDate.of(2023, 3, 10);
        LocalDate cDate = LocalDate.of(2023, 3, 11);

        InputStream requestBodyStream = new ByteArrayInputStream("A,B,C\nD,E,F".getBytes());

        f.upload("common", "sample_dataset", "20230308", "csv", requestBodyStream, fDate, tDate, cDate);

        verify(apiManager)
                .callAPIFileUpload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                                config.getRootURL(), "common", "sample_dataset", "20230308", "csv"),
                        requestBodyStream,
                        "common",
                        "sample_dataset",
                        "2023-03-09",
                        "2023-03-10",
                        "2023-03-11",
                        new HashMap<>());
    }

    @Test
    public void testFileUploadAsStreamInteractionWithHeadersAllDatesAndHeaders() throws Exception {
        Fusion f = stubFusion();
        LocalDate fDate = LocalDate.of(2023, 3, 9);
        LocalDate tDate = LocalDate.of(2023, 3, 10);
        LocalDate cDate = LocalDate.of(2023, 3, 11);
        Map<String, String> headers = new HashMap<>();
        headers.put("my-header-0", "my-value-0");

        InputStream requestBodyStream = new ByteArrayInputStream("A,B,C\nD,E,F".getBytes());

        f.upload("common", "sample_dataset", "20230308", "csv", requestBodyStream, fDate, tDate, cDate, headers);

        verify(apiManager)
                .callAPIFileUpload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                                config.getRootURL(), "common", "sample_dataset", "20230308", "csv"),
                        requestBodyStream,
                        "common",
                        "sample_dataset",
                        "2023-03-09",
                        "2023-03-10",
                        "2023-03-11",
                        headers);
    }

    @Test
    public void updatingDefaultCatalogWorksCorrectly() {
        Fusion f = Fusion.builder()
                .credentials(new BearerTokenCredentials("my token"))
                .build();
        f.setDefaultCatalog("new catalog");
        assertThat(f.getDefaultCatalog(), is(equalTo("new catalog")));
    }

    private Fusion stubFusion() {
        return Fusion.builder()
                .configuration(config)
                .bearerToken("my token")
                .api(apiManager)
                .responseParser(responseParser)
                .build();
    }

    @Test
    public void testListCatalogsInteraction() throws Exception {
        Fusion f = stubFusion();

        Map<String, Catalog> stubResponse = new HashMap<>();
        stubResponse.put("first", Catalog.builder().identifier("catalog1").build());

        HttpResponse<String> httpResponse = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"identifier\":\"catalog1\"}]}")
                .headers(new HashMap<>())
                .build();

        when(apiManager.callAPIWithResponse(eq(String.format("%1scatalogs", config.getRootURL())), anyMap()))
                .thenReturn(httpResponse);
        when(responseParser.parseCatalogResponse("{\"resources\":[{\"identifier\":\"catalog1\"}]}"))
                .thenReturn(stubResponse);

        Map<String, Catalog> actualResponse = f.listCatalogs();
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testCatalogInteractionUntyped() throws Exception {
        Fusion f = stubFusion();

        Map<String, Map<String, Object>> stubResponse = new HashMap<>();
        Map<String, Object> catalog1 = new HashMap<>();
        catalog1.put("catalog1", Catalog.builder().identifier("catalog1").build());
        stubResponse.put("catalog1", catalog1);

        when(apiManager.callAPI(String.format("%1scatalogs/common", config.getRootURL())))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseResourcesUntyped("{\"key\":value}")).thenReturn(stubResponse);

        Map<String, Map<String, Object>> actualResponse = f.catalogResources("common");
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testDatasetInteractionUntyped() throws Exception {
        Fusion f = stubFusion();

        Map<String, Map<String, Object>> stubResponse = new HashMap<>();
        Map<String, Object> dataset1 = new HashMap<>();
        dataset1.put("dataset1", Dataset.builder().identifier("dataset1").build());
        stubResponse.put("dataset1", dataset1);

        when(apiManager.callAPI(
                        String.format("%1scatalogs/%2s/datasets/%3s", config.getRootURL(), "common", "sample_dataset")))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseResourcesUntyped("{\"key\":value}")).thenReturn(stubResponse);

        Map<String, Map<String, Object>> actualResponse = f.datasetResources("sample_dataset");
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testDatasetMemberInteractionUntyped() throws Exception {
        Fusion f = stubFusion();

        Map<String, Map<String, Object>> stubResponse = new HashMap<>();
        Map<String, Object> datasetMember1 = new HashMap<>();
        datasetMember1.put(
                "datasetMember1", Dataset.builder().identifier("datasetMember1").build());
        stubResponse.put("datasetMember1", datasetMember1);

        when(apiManager.callAPI(String.format(
                        "%1scatalogs/%2s/datasets/%3s/datasetseries/%4s",
                        config.getRootURL(), "common", "sample_dataset", "1")))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseResourcesUntyped("{\"key\":value}")).thenReturn(stubResponse);

        Map<String, Map<String, Object>> actualResponse = f.datasetMemberResources("sample_dataset", "1");
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void fileDownloadWithInvalidPathThrowsFusionException() throws Exception {
        Fusion f = stubFusion();

        FusionException thrown = assertThrows(FusionException.class, () -> {
            f.download("common", "sample", "1", "csv", "\0");
        });
        assertThat(thrown.getCause().getClass(), is(equalTo(InvalidPathException.class)));
    }

    @Test
    public void testListDatasetsWithPagination() throws Exception {
        Fusion f = stubFusion();

        Map<String, List<String>> headers1 = new HashMap<>();
        headers1.put("x-jpmc-next-token", Collections.singletonList("token123"));

        HttpResponse<String> httpResponse1 = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"identifier\":\"dataset1\",\"description\":\"First\"}]}")
                .headers(headers1)
                .build();

        HttpResponse<String> httpResponse2 = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"identifier\":\"dataset2\",\"description\":\"Second\"}]}")
                .headers(new HashMap<>())
                .build();

        when(apiManager.callAPIWithResponse(
                        eq(String.format("%1scatalogs/%2s/datasets", config.getRootURL(), "common")),
                        argThat(headers -> !headers.containsKey("x-jpmc-next-token"))))
                .thenReturn(httpResponse1);

        when(apiManager.callAPIWithResponse(
                        eq(String.format("%1scatalogs/%2s/datasets", config.getRootURL(), "common")),
                        argThat(headers -> "token123".equals(headers.get("x-jpmc-next-token")))))
                .thenReturn(httpResponse2);

        Map<String, Dataset> stubResponse = new HashMap<>();
        stubResponse.put("dataset1", Dataset.builder().identifier("dataset1").build());
        stubResponse.put("dataset2", Dataset.builder().identifier("dataset2").build());

        String aggregatedJson =
                "{\"resources\":[{\"identifier\":\"dataset1\",\"description\":\"First\"},{\"identifier\":\"dataset2\",\"description\":\"Second\"}]}";
        when(responseParser.parseDatasetResponse(aggregatedJson, "common")).thenReturn(stubResponse);

        Map<String, Dataset> actualResponse = f.listDatasets("common");
        assertThat(actualResponse.size(), is(equalTo(2)));
        assertThat(actualResponse.containsKey("dataset1"), is(true));
        assertThat(actualResponse.containsKey("dataset2"), is(true));

        verify(apiManager, times(2)).callAPIWithResponse(anyString(), anyMap());
    }

    @Test
    public void testListProductsWithPagination() throws Exception {
        Fusion f = stubFusion();

        Map<String, List<String>> headers1 = new HashMap<>();
        headers1.put("x-jpmc-next-token", Collections.singletonList("productToken"));

        HttpResponse<String> httpResponse1 = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"identifier\":\"product1\"}]}")
                .headers(headers1)
                .build();

        HttpResponse<String> httpResponse2 = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"identifier\":\"product2\"}]}")
                .headers(new HashMap<>())
                .build();

        when(apiManager.callAPIWithResponse(
                        eq(String.format("%1scatalogs/%2s/products", config.getRootURL(), "common")),
                        argThat(headers -> !headers.containsKey("x-jpmc-next-token"))))
                .thenReturn(httpResponse1);

        when(apiManager.callAPIWithResponse(
                        eq(String.format("%1scatalogs/%2s/products", config.getRootURL(), "common")),
                        argThat(headers -> "productToken".equals(headers.get("x-jpmc-next-token")))))
                .thenReturn(httpResponse2);

        Map<String, DataProduct> stubResponse = new HashMap<>();
        stubResponse.put(
                "product1", DataProduct.builder().identifier("product1").build());
        stubResponse.put(
                "product2", DataProduct.builder().identifier("product2").build());

        String aggregatedJson = "{\"resources\":[{\"identifier\":\"product1\"},{\"identifier\":\"product2\"}]}";
        when(responseParser.parseDataProductResponse(aggregatedJson)).thenReturn(stubResponse);

        Map<String, DataProduct> actualResponse = f.listProducts("common");
        assertThat(actualResponse.size(), is(equalTo(2)));

        verify(apiManager, times(2)).callAPIWithResponse(anyString(), anyMap());
    }

    @Test
    public void testListAttributesWithPagination() throws Exception {
        Fusion f = stubFusion();

        Map<String, List<String>> headers1 = new HashMap<>();
        headers1.put("x-jpmc-next-token", Collections.singletonList("attrToken"));

        HttpResponse<String> httpResponse1 = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"identifier\":\"attr1\"}]}")
                .headers(headers1)
                .build();

        HttpResponse<String> httpResponse2 = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"identifier\":\"attr2\"}]}")
                .headers(new HashMap<>())
                .build();

        when(apiManager.callAPIWithResponse(
                        eq(String.format(
                                "%1scatalogs/%2s/datasets/%3s/attributes",
                                config.getRootURL(), "common", "sample_dataset")),
                        argThat(headers -> !headers.containsKey("x-jpmc-next-token"))))
                .thenReturn(httpResponse1);

        when(apiManager.callAPIWithResponse(
                        eq(String.format(
                                "%1scatalogs/%2s/datasets/%3s/attributes",
                                config.getRootURL(), "common", "sample_dataset")),
                        argThat(headers -> "attrToken".equals(headers.get("x-jpmc-next-token")))))
                .thenReturn(httpResponse2);

        Map<String, Attribute> stubResponse = new HashMap<>();
        stubResponse.put("attr1", Attribute.builder().identifier("attr1").build());
        stubResponse.put("attr2", Attribute.builder().identifier("attr2").build());

        String aggregatedJson = "{\"resources\":[{\"identifier\":\"attr1\"},{\"identifier\":\"attr2\"}]}";
        when(responseParser.parseAttributeResponse(aggregatedJson, "common", "sample_dataset"))
                .thenReturn(stubResponse);

        Map<String, Attribute> actualResponse = f.listAttributes("common", "sample_dataset");
        assertThat(actualResponse.size(), is(equalTo(2)));

        verify(apiManager, times(2)).callAPIWithResponse(anyString(), anyMap());
    }

    @Test
    public void testListDatasetMembersWithPagination() throws Exception {
        Fusion f = stubFusion();

        Map<String, List<String>> headers1 = new HashMap<>();
        headers1.put("x-jpmc-next-token", Collections.singletonList("seriesToken"));

        HttpResponse<String> httpResponse1 = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"identifier\":\"series1\"}]}")
                .headers(headers1)
                .build();

        HttpResponse<String> httpResponse2 = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"identifier\":\"series2\"}]}")
                .headers(new HashMap<>())
                .build();

        when(apiManager.callAPIWithResponse(
                        eq(String.format(
                                "%1scatalogs/%2s/datasets/%3s/datasetseries",
                                config.getRootURL(), "common", "sample_dataset")),
                        argThat(headers -> !headers.containsKey("x-jpmc-next-token"))))
                .thenReturn(httpResponse1);

        when(apiManager.callAPIWithResponse(
                        eq(String.format(
                                "%1scatalogs/%2s/datasets/%3s/datasetseries",
                                config.getRootURL(), "common", "sample_dataset")),
                        argThat(headers -> "seriesToken".equals(headers.get("x-jpmc-next-token")))))
                .thenReturn(httpResponse2);

        Map<String, DatasetSeries> stubResponse = new HashMap<>();
        stubResponse.put(
                "series1", DatasetSeries.builder().identifier("series1").build());
        stubResponse.put(
                "series2", DatasetSeries.builder().identifier("series2").build());

        when(responseParser.parseDatasetSeriesResponse(anyString())).thenReturn(stubResponse);

        Map<String, DatasetSeries> actualResponse = f.listDatasetMembers("common", "sample_dataset");
        assertThat(actualResponse.size(), is(equalTo(2)));

        verify(apiManager, times(2)).callAPIWithResponse(anyString(), anyMap());
    }

    @Test
    public void testListCatalogsWithPagination() throws Exception {
        Fusion f = stubFusion();

        Map<String, List<String>> headers1 = new HashMap<>();
        headers1.put("x-jpmc-next-token", Collections.singletonList("catalogToken"));

        HttpResponse<String> httpResponse1 = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"identifier\":\"catalog1\"}]}")
                .headers(headers1)
                .build();

        HttpResponse<String> httpResponse2 = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"identifier\":\"catalog2\"}]}")
                .headers(new HashMap<>())
                .build();

        when(apiManager.callAPIWithResponse(
                        eq(String.format("%1scatalogs", config.getRootURL())),
                        argThat(headers -> !headers.containsKey("x-jpmc-next-token"))))
                .thenReturn(httpResponse1);

        when(apiManager.callAPIWithResponse(
                        eq(String.format("%1scatalogs", config.getRootURL())),
                        argThat(headers -> "catalogToken".equals(headers.get("x-jpmc-next-token")))))
                .thenReturn(httpResponse2);

        Map<String, Catalog> stubResponse = new HashMap<>();
        stubResponse.put("catalog1", Catalog.builder().identifier("catalog1").build());
        stubResponse.put("catalog2", Catalog.builder().identifier("catalog2").build());

        String aggregatedJson = "{\"resources\":[{\"identifier\":\"catalog1\"},{\"identifier\":\"catalog2\"}]}";
        when(responseParser.parseCatalogResponse(aggregatedJson)).thenReturn(stubResponse);

        Map<String, Catalog> actualResponse = f.listCatalogs();
        assertThat(actualResponse.size(), is(equalTo(2)));

        verify(apiManager, times(2)).callAPIWithResponse(anyString(), anyMap());
    }

    @Test
    public void testListDistributionsWithPagination() throws Exception {
        Fusion f = stubFusion();

        Map<String, List<String>> headers1 = new HashMap<>();
        headers1.put("x-jpmc-next-token", Collections.singletonList("distToken"));

        HttpResponse<String> httpResponse1 = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"identifier\":\"csv\"}]}")
                .headers(headers1)
                .build();

        HttpResponse<String> httpResponse2 = HttpResponse.<String>builder()
                .statusCode(200)
                .body("{\"resources\":[{\"identifier\":\"parquet\"}]}")
                .headers(new HashMap<>())
                .build();

        when(apiManager.callAPIWithResponse(
                        eq(String.format(
                                "%1scatalogs/%2s/datasets/%3s/datasetseries/%4s/distributions",
                                config.getRootURL(), "common", "sample_dataset", "20230308")),
                        argThat(headers -> !headers.containsKey("x-jpmc-next-token"))))
                .thenReturn(httpResponse1);

        when(apiManager.callAPIWithResponse(
                        eq(String.format(
                                "%1scatalogs/%2s/datasets/%3s/datasetseries/%4s/distributions",
                                config.getRootURL(), "common", "sample_dataset", "20230308")),
                        argThat(headers -> "distToken".equals(headers.get("x-jpmc-next-token")))))
                .thenReturn(httpResponse2);

        Map<String, Distribution> stubResponse = new HashMap<>();
        stubResponse.put("csv", Distribution.builder().identifier("csv").build());
        stubResponse.put("parquet", Distribution.builder().identifier("parquet").build());

        String aggregatedJson = "{\"resources\":[{\"identifier\":\"csv\"},{\"identifier\":\"parquet\"}]}";
        when(responseParser.parseDistributionResponse(aggregatedJson)).thenReturn(stubResponse);

        Map<String, Distribution> actualResponse = f.listDistributions("common", "sample_dataset", "20230308");
        assertThat(actualResponse.size(), is(equalTo(2)));

        verify(apiManager, times(2)).callAPIWithResponse(anyString(), anyMap());
    }
}
