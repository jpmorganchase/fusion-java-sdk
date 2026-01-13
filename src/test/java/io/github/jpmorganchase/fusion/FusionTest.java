package io.github.jpmorganchase.fusion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.model.*;
import io.github.jpmorganchase.fusion.oauth.credential.BearerTokenCredentials;
import io.github.jpmorganchase.fusion.parsing.APIResponseParser;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
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

        when(apiManager.callAPI(String.format("%1scatalogs/%2s/datasets", config.getRootURL(), catalog)))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseDatasetResponse("{\"key\":value}", catalog)).thenReturn(stubResponse);

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

        when(apiManager.callAPI(String.format("%1scatalogs/%2s/products", config.getRootURL(), catalog)))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseDataProductResponse("{\"key\":value}")).thenReturn(stubResponse);

        return stubResponse;
    }

    @Test
    public void testDatasetSeriesInteraction() throws Exception {
        Fusion f = stubFusion();

        Map<String, DatasetSeries> stubResponse = new HashMap<>();
        stubResponse.put("first", DatasetSeries.builder().identifier("dataset1").build());

        when(apiManager.callAPI(String.format(
                        "%1scatalogs/%2s/datasets/%3s/datasetseries", config.getRootURL(), "common", "sample_dataset")))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseDatasetSeriesResponse("{\"key\":value}")).thenReturn(stubResponse);

        Map<String, DatasetSeries> actualResponse = f.listDatasetMembers("sample_dataset");
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testAttributeInteraction() throws Exception {
        Fusion f = stubFusion();

        Map<String, Attribute> stubResponse = new HashMap<>();
        stubResponse.put("first", Attribute.builder().identifier("attribute1").build());

        when(apiManager.callAPI(String.format(
                        "%1scatalogs/%2s/datasets/%3s/attributes", config.getRootURL(), "common", "sample_dataset")))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseAttributeResponse("{\"key\":value}", "common", "sample_dataset"))
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

        when(apiManager.callAPI(String.format(
                        "%1scatalogs/%2s/datasets/%3s/datasetseries/%4s/distributions",
                        config.getRootURL(), "common", "sample_dataset", "20230308")))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseDistributionResponse("{\"key\":value}")).thenReturn(stubResponse);

        Map<String, Distribution> actualResponse = f.listDistributions("sample_dataset", "20230308");
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testFileDownloadInteraction() throws Exception {
        Fusion f = stubFusion();

        // Mock listDistributionFiles to return a list of files
        String jsonResponse = "{\"resources\": [{\"identifier\": \"file1\"}]}";
        Map<String, DistributionFile> stubResponse = new LinkedHashMap<>();
        stubResponse.put(
                "file1",
                DistributionFile.builder()
                        .identifier("file1")
                        .fileExtension(".csv")
                        .build());

        when(apiManager.callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample_dataset", "20230308", "csv")))
                .thenReturn(jsonResponse);
        when(responseParser.parseDistributionFilesResponse(jsonResponse)).thenReturn(stubResponse);

        doNothing()
                .when(apiManager)
                .callAPIFileDownload(
                        anyString(),
                        eq(String.format("%s/%s", TMP_PATH, "file1.csv")),
                        eq("common"),
                        eq("sample_dataset"),
                        eq(new HashMap<>()),
                        eq("file1"),
                        eq(false));

        f.download("common", "sample_dataset", "20230308", "csv", TMP_PATH);
    }

    @Test
    public void testFileDownloadWithSkipsChecksumValidationWhenMissing() throws Exception {
        Fusion f = stubFusion();

        // Mock dataset response for checksum validation check
        String datasetJsonRaw = "{\"identifier\":\"sample_dataset\",\"deliveryChannel\":[\"glue\"]}";
        Dataset glueDataset = Dataset.builder()
                .identifier("sample_dataset")
                .varArg("deliveryChannel", Arrays.asList("glue"))
                .build();
        Map<String, Dataset> datasets = new HashMap<>();
        datasets.put("sample_dataset", glueDataset);

        when(apiManager.callAPI(
                        String.format("%scatalogs/%s/datasets/%s", config.getRootURL(), "common", "sample_dataset")))
                .thenReturn(datasetJsonRaw);
        when(responseParser.parseDatasetResponse(String.format("{\"resources\":[%s]}", datasetJsonRaw), "common"))
                .thenReturn(datasets);

        // Mock distribution files response
        String jsonResponse = "{\"resources\": [{\"identifier\": \"file1\"}]}";
        Map<String, DistributionFile> stubResponse = new LinkedHashMap<>();
        stubResponse.put(
                "file1",
                DistributionFile.builder()
                        .identifier("file1")
                        .fileExtension(".csv")
                        .build());

        when(apiManager.callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample_dataset", "20230308", "csv")))
                .thenReturn(jsonResponse);
        when(responseParser.parseDistributionFilesResponse(jsonResponse)).thenReturn(stubResponse);

        doNothing()
                .when(apiManager)
                .callAPIFileDownload(
                        anyString(), anyString(), anyString(), anyString(), anyMap(), anyString(), eq(true));

        f.download("common", "sample_dataset", "20230308", "csv", TMP_PATH);

        verify(apiManager)
                .callAPIFileDownload(
                        anyString(), anyString(), eq("common"), eq("sample_dataset"), anyMap(), eq("file1"), eq(true));
    }

    @Test
    public void testFileDownloadInteractionWithDefaultPath() throws Exception {
        Fusion f = stubFusion();

        // Mock listDistributionFiles to return a list of files
        String jsonResponse = "{\"resources\": [{\"identifier\": \"file1\"}]}";
        Map<String, DistributionFile> stubResponse = new LinkedHashMap<>();
        stubResponse.put(
                "file1",
                DistributionFile.builder()
                        .identifier("file1")
                        .fileExtension(".csv")
                        .build());

        when(apiManager.callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample_dataset", "20230308", "csv")))
                .thenReturn(jsonResponse);
        when(responseParser.parseDistributionFilesResponse(jsonResponse)).thenReturn(stubResponse);

        doNothing()
                .when(apiManager)
                .callAPIFileDownload(
                        anyString(),
                        eq(String.format("%s/%s", "downloads", "file1.csv")),
                        eq("common"),
                        eq("sample_dataset"),
                        eq(new HashMap<>()),
                        eq("file1"),
                        eq(false));

        f.download("common", "sample_dataset", "20230308", "csv");
    }

    @Test
    public void testFileDownloadAsStreamInteraction() throws Exception {
        Fusion f = stubFusion();

        // Mock listDistributionFiles to return a list of files
        String jsonResponse = "{\"resources\": [{\"identifier\": \"file1\"}]}";
        Map<String, DistributionFile> stubResponse = new LinkedHashMap<>();
        stubResponse.put("file1", DistributionFile.builder().identifier("file1").build());

        when(apiManager.callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample_dataset", "20230308", "csv")))
                .thenReturn(jsonResponse);
        when(responseParser.parseDistributionFilesResponse(jsonResponse)).thenReturn(stubResponse);

        when(apiManager.callAPIFileDownload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files/operationType/download",
                                config.getRootURL(), "common", "sample_dataset", "20230308", "csv"),
                        "common",
                        "sample_dataset",
                        new HashMap<>(),
                        "file1",
                        false))
                .thenReturn(new ByteArrayInputStream("A,B,C\nD,E,F".getBytes()));

        Map<String, InputStream> response = f.downloadStream("common", "sample_dataset", "20230308", "csv");

        assertThat(response.size(), is(equalTo(1)));
        assertThat(response.containsKey("file1"), is(true));

        InputStream stream = response.get("file1");
        String responseText = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
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

        when(apiManager.callAPI(String.format("%1scatalogs", config.getRootURL())))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseCatalogResponse("{\"key\":value}")).thenReturn(stubResponse);

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

        // Mock listDistributionFiles to return a list of files
        String jsonResponse = "{\"resources\": [{\"identifier\": \"file1\"}]}";
        Map<String, DistributionFile> stubResponse = new LinkedHashMap<>();
        stubResponse.put("file1", DistributionFile.builder().identifier("file1").build());

        when(apiManager.callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample", "1", "csv")))
                .thenReturn(jsonResponse);
        when(responseParser.parseDistributionFilesResponse(jsonResponse)).thenReturn(stubResponse);

        FusionException thrown = assertThrows(FusionException.class, () -> {
            f.download("common", "sample", "1", "csv", "\0");
        });
        assertThat(thrown.getMessage(), is(equalTo("Unable to save to \0")));
    }

    @Test
    public void testListDistributionFiles() throws Exception {
        Fusion f = stubFusion();

        String jsonResponse =
                "{\"resources\": [{\"identifier\": \"file1\"}, {\"identifier\": \"file2\"}, {\"identifier\": \"file3\"}]}";
        Map<String, DistributionFile> stubResponse = new LinkedHashMap<>();
        stubResponse.put("file1", DistributionFile.builder().identifier("file1").build());
        stubResponse.put("file2", DistributionFile.builder().identifier("file2").build());
        stubResponse.put("file3", DistributionFile.builder().identifier("file3").build());

        when(apiManager.callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample_dataset", "20230308", "csv")))
                .thenReturn(jsonResponse);
        when(responseParser.parseDistributionFilesResponse(jsonResponse)).thenReturn(stubResponse);

        Map<String, DistributionFile> files = f.listDistributionFiles("common", "sample_dataset", "20230308", "csv", 0);
        assertThat(files.size(), is(equalTo(3)));
        assertThat(files.containsKey("file1"), is(true));
        assertThat(files.containsKey("file2"), is(true));
        assertThat(files.containsKey("file3"), is(true));
        assertThat(files.get("file1").getIdentifier(), is(equalTo("file1")));
    }

    @Test
    public void testListDistributionFilesWithMaxResults() throws Exception {
        Fusion f = stubFusion();

        String jsonResponse =
                "{\"resources\": [{\"identifier\": \"file1\"}, {\"identifier\": \"file2\"}, {\"identifier\": \"file3\"}]}";
        Map<String, DistributionFile> stubResponse = new LinkedHashMap<>();
        stubResponse.put("file1", DistributionFile.builder().identifier("file1").build());
        stubResponse.put("file2", DistributionFile.builder().identifier("file2").build());
        stubResponse.put("file3", DistributionFile.builder().identifier("file3").build());

        when(apiManager.callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample_dataset", "20230308", "csv")))
                .thenReturn(jsonResponse);
        when(responseParser.parseDistributionFilesResponse(jsonResponse)).thenReturn(stubResponse);

        Map<String, DistributionFile> files = f.listDistributionFiles("common", "sample_dataset", "20230308", "csv", 2);
        assertThat(files.size(), is(equalTo(2)));
        assertThat(files.containsKey("file1"), is(true));
        assertThat(files.containsKey("file2"), is(true));
        assertThat(files.containsKey("file3"), is(false));
    }

    @Test
    public void testListDistributionFilesWhenEmpty() throws Exception {
        Fusion f = stubFusion();

        String jsonResponse = "{\"resources\": []}";
        Map<String, DistributionFile> stubResponse = new LinkedHashMap<>();

        when(apiManager.callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample_dataset", "20230308", "csv")))
                .thenReturn(jsonResponse);
        when(responseParser.parseDistributionFilesResponse(jsonResponse)).thenReturn(stubResponse);

        Map<String, DistributionFile> files = f.listDistributionFiles("common", "sample_dataset", "20230308", "csv", 0);
        assertThat(files.size(), is(equalTo(0)));
    }

    @Test
    public void testDownloadMultipleFiles() throws Exception {
        Fusion f = stubFusion();

        // Mock listDistributionFiles to return multiple files
        String jsonResponse = "{\"resources\": [{\"identifier\": \"file1\"}, {\"identifier\": \"file2\"}]}";
        Map<String, DistributionFile> stubResponse = new LinkedHashMap<>();
        stubResponse.put(
                "file1",
                DistributionFile.builder()
                        .identifier("file1")
                        .fileExtension(".csv")
                        .build());
        stubResponse.put(
                "file2",
                DistributionFile.builder()
                        .identifier("file2")
                        .fileExtension(".csv")
                        .build());

        when(apiManager.callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample_dataset", "20230308", "csv")))
                .thenReturn(jsonResponse);
        when(responseParser.parseDistributionFilesResponse(jsonResponse)).thenReturn(stubResponse);

        doNothing()
                .when(apiManager)
                .callAPIFileDownload(
                        anyString(),
                        eq(String.format("%s/%s", TMP_PATH, "file1.csv")),
                        eq("common"),
                        eq("sample_dataset"),
                        eq(new HashMap<>()),
                        eq("file1"),
                        eq(false));

        doNothing()
                .when(apiManager)
                .callAPIFileDownload(
                        anyString(),
                        eq(String.format("%s/%s", TMP_PATH, "file2.csv")),
                        eq("common"),
                        eq("sample_dataset"),
                        eq(new HashMap<>()),
                        eq("file2"),
                        eq(false));

        f.download("common", "sample_dataset", "20230308", "csv", TMP_PATH);

        verify(apiManager, times(1))
                .callAPIFileDownload(
                        anyString(),
                        eq(String.format("%s/%s", TMP_PATH, "file1.csv")),
                        eq("common"),
                        eq("sample_dataset"),
                        eq(new HashMap<>()),
                        eq("file1"),
                        eq(false));

        verify(apiManager, times(1))
                .callAPIFileDownload(
                        anyString(),
                        eq(String.format("%s/%s", TMP_PATH, "file2.csv")),
                        eq("common"),
                        eq("sample_dataset"),
                        eq(new HashMap<>()),
                        eq("file2"),
                        eq(false));
    }

    @Test
    public void testDownloadThrowsExceptionWhenNoFilesFound() throws Exception {
        Fusion f = stubFusion();

        // Mock listDistributionFiles to return empty list
        when(apiManager.callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample_dataset", "20230308", "csv")))
                .thenReturn("{\"resources\": []}");

        FusionException thrown = assertThrows(FusionException.class, () -> {
            f.download("common", "sample_dataset", "20230308", "csv", TMP_PATH);
        });

        assertThat(
                thrown.getMessage(),
                is(
                        equalTo(
                                "No files found to download for catalog=common, dataset=sample_dataset, series=20230308, distribution=csv")));
    }

    @Test
    public void testDownloadStreamMultipleFiles() throws Exception {
        Fusion f = stubFusion();

        // Mock listDistributionFiles to return multiple files
        String jsonResponse = "{\"resources\": [{\"identifier\": \"file1\"}, {\"identifier\": \"file2\"}]}";
        Map<String, DistributionFile> stubResponse = new LinkedHashMap<>();
        stubResponse.put("file1", DistributionFile.builder().identifier("file1").build());
        stubResponse.put("file2", DistributionFile.builder().identifier("file2").build());

        when(apiManager.callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample_dataset", "20230308", "csv")))
                .thenReturn(jsonResponse);
        when(responseParser.parseDistributionFilesResponse(jsonResponse)).thenReturn(stubResponse);

        when(apiManager.callAPIFileDownload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files/operationType/download",
                                config.getRootURL(), "common", "sample_dataset", "20230308", "csv"),
                        "common",
                        "sample_dataset",
                        new HashMap<>(),
                        "file1",
                        false))
                .thenReturn(new ByteArrayInputStream("A,B,C\n1,2,3".getBytes()));

        when(apiManager.callAPIFileDownload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files/operationType/download",
                                config.getRootURL(), "common", "sample_dataset", "20230308", "csv"),
                        "common",
                        "sample_dataset",
                        new HashMap<>(),
                        "file2",
                        false))
                .thenReturn(new ByteArrayInputStream("D,E,F\n4,5,6".getBytes()));

        Map<String, InputStream> response = f.downloadStream("common", "sample_dataset", "20230308", "csv");

        assertThat(response.size(), is(equalTo(2)));
        assertThat(response.containsKey("file1"), is(true));
        assertThat(response.containsKey("file2"), is(true));
    }

    @Test
    public void testDownloadStreamThrowsExceptionWhenNoFilesFound() throws Exception {
        Fusion f = stubFusion();

        // Mock listDistributionFiles to return empty list
        when(apiManager.callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample_dataset", "20230308", "csv")))
                .thenReturn("{\"resources\": []}");

        FusionException thrown = assertThrows(FusionException.class, () -> {
            f.downloadStream("common", "sample_dataset", "20230308", "csv");
        });

        assertThat(
                thrown.getMessage(),
                is(
                        equalTo(
                                "No files found to download for catalog=common, dataset=sample_dataset, series=20230308, distribution=csv")));
    }

    @Test
    public void testDownloadWithProvidedFileList() throws Exception {
        Fusion f = stubFusion();

        List<String> fileNames = Arrays.asList("custom_file");

        // Mock listDistributionFiles response
        String jsonResponse = "{\"resources\": [{\"identifier\": \"custom_file\", \"fileExtension\": \".csv\"}]}";
        Map<String, DistributionFile> stubResponse = new LinkedHashMap<>();
        stubResponse.put(
                "custom_file",
                DistributionFile.builder()
                        .identifier("custom_file")
                        .fileExtension(".csv")
                        .build());

        when(apiManager.callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample_dataset", "20230308", "csv")))
                .thenReturn(jsonResponse);
        when(responseParser.parseDistributionFilesResponse(jsonResponse)).thenReturn(stubResponse);

        doNothing()
                .when(apiManager)
                .callAPIFileDownload(
                        anyString(),
                        eq(String.format("%s/%s", TMP_PATH, "custom_file.csv")),
                        eq("common"),
                        eq("sample_dataset"),
                        eq(new HashMap<>()),
                        eq("custom_file"),
                        eq(false));

        f.download("common", "sample_dataset", "20230308", "csv", TMP_PATH, fileNames);

        verify(apiManager, times(1))
                .callAPIFileDownload(
                        anyString(),
                        eq(String.format("%s/%s", TMP_PATH, "custom_file.csv")),
                        eq("common"),
                        eq("sample_dataset"),
                        eq(new HashMap<>()),
                        eq("custom_file"),
                        eq(false));

        // Verify that listDistributionFiles IS called to validate files and get metadata
        verify(apiManager, times(1))
                .callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample_dataset", "20230308", "csv"));
    }

    @Test
    public void testDownloadWithNonExistentFilesThrowsException() throws Exception {
        Fusion f = stubFusion();

        List<String> fileNames = Arrays.asList("file1", "nonexistent_file", "another_missing_file");

        // Mock listDistributionFiles response - only file1 exists
        String jsonResponse = "{\"resources\": [{\"identifier\": \"file1\", \"fileExtension\": \".csv\"}]}";
        Map<String, DistributionFile> stubResponse = new LinkedHashMap<>();
        stubResponse.put(
                "file1",
                DistributionFile.builder()
                        .identifier("file1")
                        .fileExtension(".csv")
                        .build());

        when(apiManager.callAPI(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s/files",
                        config.getRootURL(), "common", "sample_dataset", "20230308", "csv")))
                .thenReturn(jsonResponse);
        when(responseParser.parseDistributionFilesResponse(jsonResponse)).thenReturn(stubResponse);

        FusionException thrown = assertThrows(FusionException.class, () -> {
            f.download("common", "sample_dataset", "20230308", "csv", TMP_PATH, fileNames);
        });

        assertThat(
                thrown.getMessage(),
                is(
                        equalTo(
                                "The following requested files do not exist in catalog=common, dataset=sample_dataset, series=20230308, distribution=csv: nonexistent_file, another_missing_file")));
    }
}
