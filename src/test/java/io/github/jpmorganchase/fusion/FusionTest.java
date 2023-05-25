package io.github.jpmorganchase.fusion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.api.APIUploader;
import io.github.jpmorganchase.fusion.http.Client;
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
import java.util.HashMap;
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
    private APIUploader apiUploader;

    @Mock
    private APIResponseParser responseParser;

    @Mock
    private Client httpClient;

    @Test
    public void testListDatasetsInteraction() throws Exception {
        Fusion f = stubFusion();

        Map<String, Dataset> stubResponse = setupDatasetTest("common");

        Map<String, Dataset> actualResponse = f.listDatasets();
        assertThat(actualResponse, is(equalTo(stubResponse)));
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
        stubResponse.put("first", Dataset.builder().identifier("dataset1").build());

        when(apiManager.callAPI(String.format("%1scatalogs/%2s/datasets", Fusion.DEFAULT_ROOT_URL, catalog)))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseDatasetResponse("{\"key\":value}")).thenReturn(stubResponse);

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

        when(apiManager.callAPI(String.format("%1scatalogs/%2s/products", Fusion.DEFAULT_ROOT_URL, catalog)))
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
                        "%1scatalogs/%2s/datasets/%3s/datasetseries",
                        Fusion.DEFAULT_ROOT_URL, "common", "sample_dataset")))
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
                        "%1scatalogs/%2s/datasets/%3s/attributes",
                        Fusion.DEFAULT_ROOT_URL, "common", "sample_dataset")))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseAttributeResponse("{\"key\":value}")).thenReturn(stubResponse);

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
                        "%1scatalogs/%2s/datasets/%3s/attributes",
                        Fusion.DEFAULT_ROOT_URL, "common", "sample_dataset")))
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
                        Fusion.DEFAULT_ROOT_URL, "common", "sample_dataset", "20230308")))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseDistributionResponse("{\"key\":value}")).thenReturn(stubResponse);

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
                                Fusion.DEFAULT_ROOT_URL, "common", "sample_dataset", "20230308", "csv"),
                        String.format("%s/%s_%s_%s.%s", TMP_PATH, "common", "sample_dataset", "20230308", "csv"));

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
                                Fusion.DEFAULT_ROOT_URL, "common", "sample_dataset", "20230308", "csv"),
                        String.format("%s/%s_%s_%s.%s", "downloads", "common", "sample_dataset", "20230308", "csv"));

        f.download("common", "sample_dataset", "20230308", "csv");
    }

    @Test
    public void testFileDownloadAsStreamInteraction() throws Exception {
        Fusion f = stubFusion();

        when(apiManager.callAPIFileDownload(String.format(
                        "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                        Fusion.DEFAULT_ROOT_URL, "common", "sample_dataset", "20230308", "csv")))
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

        verify(apiUploader)
                .callAPIFileUpload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                                Fusion.DEFAULT_ROOT_URL, "common", "sample_dataset", "20230308", "csv"),
                        "/tmp/file.csv",
                        "common",
                        "sample_dataset",
                        "2023-03-09",
                        "2023-03-09",
                        "2023-03-09");
    }

    @Test
    public void testFileUploadAsStreamInteraction() throws Exception {
        Fusion f = stubFusion();
        LocalDate d = LocalDate.of(2023, 3, 9);

        InputStream requestBodyStream = new ByteArrayInputStream("A,B,C\nD,E,F".getBytes());

        f.upload("common", "sample_dataset", "20230308", "csv", requestBodyStream, d, d, d);

        verify(apiUploader)
                .callAPIFileUpload(
                        String.format(
                                "%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",
                                Fusion.DEFAULT_ROOT_URL, "common", "sample_dataset", "20230308", "csv"),
                        requestBodyStream,
                        "common",
                        "sample_dataset",
                        "2023-03-09",
                        "2023-03-09",
                        "2023-03-09");
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
                .credentials(new BearerTokenCredentials("my token"))
                .api(apiManager)
                .responseParser(responseParser)
                .uploader(apiUploader)
                .build();
    }

    @Test
    public void testListCatalogsInteraction() throws Exception {
        Fusion f = stubFusion();

        Map<String, Catalog> stubResponse = new HashMap<>();
        stubResponse.put("first", Catalog.builder().identifier("catalog1").build());

        when(apiManager.callAPI(String.format("%1scatalogs", Fusion.DEFAULT_ROOT_URL)))
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

        when(apiManager.callAPI(String.format("%1scatalogs/common", Fusion.DEFAULT_ROOT_URL)))
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

        when(apiManager.callAPI(String.format(
                        "%1scatalogs/%2s/datasets/%3s", Fusion.DEFAULT_ROOT_URL, "common", "sample_dataset")))
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
                        Fusion.DEFAULT_ROOT_URL, "common", "sample_dataset", "1")))
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
}
