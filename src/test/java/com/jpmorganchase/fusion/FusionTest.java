package com.jpmorganchase.fusion;

import com.jpmorganchase.fusion.credential.BearerTokenCredentials;
import com.jpmorganchase.fusion.credential.FusionCredentials;
import com.jpmorganchase.fusion.credential.OAuthPasswordBasedCredentials;
import com.jpmorganchase.fusion.credential.OAuthSecretBasedCredentials;
import com.jpmorganchase.fusion.model.*;
import com.jpmorganchase.fusion.parsing.APIResponseParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FusionTest {

    @Mock
    private IFusionAPIManager apiManager;

    @Mock
    private APIResponseParser responseParser;

    @Test
    public void constructionWithNoUrlUsesDefaultUrl(){
        Fusion f = Fusion.builder()
                .credentials(new BearerTokenCredentials("my token"))
                .build();
        assertThat(f.getRootURL(), is(equalTo(Fusion.DEFAULT_ROOT_URL)));
    }

    @Test
    public void constructionWithUrlDoesNotUseDefaultUrl(){
        Fusion f = Fusion.builder()
                .credentials(new BearerTokenCredentials("my token"))
                .rootURL("https://my-api.domain.com/api")
                .build();
        assertThat(f.getRootURL(), is(equalTo("https://my-api.domain.com/api")));
    }

    @Test
    public void constructionWithNoCatalogDefaultsToCommon(){
        Fusion f = Fusion.builder()
                .credentials(new BearerTokenCredentials("my token"))
                .build();
        assertThat(f.getDefaultCatalog(), is(equalTo(Fusion.DEFAULT_CATALOG)));
    }

    //TODO: A better test is probably to make sure that it gets passed down to the next layer?
    @Test
    public void constructionWithCatalogUsesCorrectDefaultCatalog(){
        Fusion f = Fusion.builder()
                .credentials(new BearerTokenCredentials("my token"))
                .defaultCatalog("test")
                .build();
        assertThat(f.getDefaultCatalog(), is(equalTo("test")));
    }

    @Test
    public void constructWithBearerToken(){
        Fusion f = Fusion.builder()
                .bearerToken("my-token")
                .build();
        assertThat(f.getCredentials() instanceof BearerTokenCredentials, is(true));
    }

    @Test
    public void constructWithSecretBasedCredentials(){
        Fusion f = Fusion.builder()
                .secretBasedCredentials("aClientId", "aSecret", "aResource", "https://oauth-api.domain.com")
                .build();
        assertThat(f.getCredentials() instanceof OAuthSecretBasedCredentials, is(true));
        OAuthSecretBasedCredentials credentials = (OAuthSecretBasedCredentials) f.getCredentials();
        assertThat(credentials.getClientId(), is(equalTo("aClientId")));
        assertThat(credentials.getResource(), is(equalTo("aResource")));
        assertThat(credentials.getAuthServerUrl(), is(equalTo("https://oauth-api.domain.com")));
        //TODO: validate secret?
    }

    @Test
    public void constructWithPasswordBasedCredentials(){
        Fusion f = Fusion.builder()
                .passwordBasedCredentials("aClientId", "aUsername", "aPassword", "aResource", "https://oauth-api.domain.com")
                .build();
        assertThat(f.getCredentials() instanceof OAuthPasswordBasedCredentials, is(true));
        OAuthPasswordBasedCredentials credentials = (OAuthPasswordBasedCredentials) f.getCredentials();
        assertThat(credentials.getClientId(), is(equalTo("aClientId")));
        assertThat(credentials.getResource(), is(equalTo("aResource")));
        assertThat(credentials.getAuthServerUrl(), is(equalTo("https://oauth-api.domain.com")));
        //TODO: validate password?
    }

    @Test
    public void constructWithProxy(){
        Fusion f = Fusion.builder()
                .bearerToken("my-token")
                .proxy("http://myproxy.domain.com", 8080)
                .build();
        assertThat(f.getCredentials() instanceof BearerTokenCredentials, is(true));
    }

    //TODO: Add interaction tests that do not use the default catalog
    @Test
    public void testListDatasetsInteraction() throws Exception{
        Fusion f = stubFusion();

        Map<String, Dataset> stubResponse = new HashMap<>();
        stubResponse.put("first", Dataset.builder().identifier("dataset1").build());

        when(apiManager.callAPI(String.format("%1scatalogs/%2s/datasets",Fusion.DEFAULT_ROOT_URL, "common")))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseDatasetResponse("{\"key\":value}"))
                .thenReturn(stubResponse);

        Map<String, Dataset> actualResponse = f.listDatasets();
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testListProductsInteraction() throws Exception{
        Fusion f = stubFusion();

        Map<String, DataProduct> stubResponse = new HashMap<>();
        stubResponse.put("first", DataProduct.builder().identifier("product1").build());

        when(apiManager.callAPI(String.format("%1scatalogs/%2s/products",Fusion.DEFAULT_ROOT_URL, "common")))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseDataProductResponse("{\"key\":value}"))
                .thenReturn(stubResponse);

        Map<String, DataProduct> actualResponse = f.listProducts();
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testDatasetSeriesInteraction() throws Exception{
        Fusion f = stubFusion();

        Map<String, DatasetSeries> stubResponse = new HashMap<>();
        stubResponse.put("first", DatasetSeries.builder().identifier("dataset1").build());

        when(apiManager.callAPI(String.format("%1scatalogs/%2s/datasets/%3s/datasetseries",Fusion.DEFAULT_ROOT_URL, "common", "sample_dataset")))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseDatasetSeriesResponse("{\"key\":value}"))
                .thenReturn(stubResponse);

        Map<String, DatasetSeries> actualResponse = f.listDatasetMembers("sample_dataset");
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testAttributeInteraction() throws Exception{
        Fusion f = stubFusion();

        Map<String, Attribute> stubResponse = new HashMap<>();
        stubResponse.put("first", Attribute.builder().identifier("attribute1").build());

        when(apiManager.callAPI(String.format("%1scatalogs/%2s/datasets/%3s/attributes",Fusion.DEFAULT_ROOT_URL, "common","sample_dataset")))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseAttributeResponse("{\"key\":value}"))
                .thenReturn(stubResponse);

        Map<String, Attribute> actualResponse = f.listAttributes("sample_dataset");
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testDistributionInteraction() throws Exception{
        Fusion f = stubFusion();

        Map<String, Distribution> stubResponse = new HashMap<>();
        stubResponse.put("first", Distribution.builder().identifier("attribute1").build());

        when(apiManager.callAPI(String.format("%1scatalogs/%2s/datasets/%3s/datasetseries/%4s/distributions",
                        Fusion.DEFAULT_ROOT_URL, "common","sample_dataset", "20230308")))
                .thenReturn("{\"key\":value}");
        when(responseParser.parseDistributionResponse("{\"key\":value}"))
                .thenReturn(stubResponse);

        Map<String, Distribution> actualResponse = f.listDistributions("sample_dataset", "20230308");
        assertThat(actualResponse, is(equalTo(stubResponse)));
    }

    @Test
    public void testFileDownloadInteraction() throws Exception{
        Fusion f = stubFusion();

        doNothing().when(apiManager).callAPIFileDownload(
                String.format("%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s", Fusion.DEFAULT_ROOT_URL, "common", "sample_dataset", "20230308", "csv"),
                String.format("%s/%s_%s_%s.%s","/tmp", "common","sample_dataset", "20230308","csv"));

        int result = f.download("common", "sample_dataset", "20230308", "csv", "/tmp");
        assertThat(result, is(1));
    }

    @Test
    public void testFileUploadInteraction() throws Exception{
        Fusion f = stubFusion();
        LocalDate d = LocalDate.of(2023, 3, 9);

        when(apiManager.callAPIFileUpload(
                String.format("%scatalogs/%s/datasets/%s/datasetseries/%s/distributions/%s",Fusion.DEFAULT_ROOT_URL, "common","sample_dataset", "20230308","csv")
                , "/tmp/file.csv", "2023-03-09", "2023-03-09", "2023-03-09")) //TODO: get the dates
                .thenReturn(1);

        //TODO: test with different dates as well
        int result = f.upload("common", "sample_dataset", "20230308", "csv", "/tmp/file.csv", d);
        assertThat(result, is(1));
    }

    private Fusion stubFusion(){
        return Fusion.builder()
                .credentials(new BearerTokenCredentials("my token"))
                .api(apiManager)
                .responseParser(responseParser)
                .build();
    }

    //TODO: Can we get this to work?
    /*private <T extends CatalogResource> Map<String, T> createStubResponse(Class<T> resourceClass){
        Map<String, T> stubResponse = new HashMap<>();
        try {
            stubResponse.put("first", ((T)resourceClass.getDeclaredMethod("builder").invoke(null)).identifier("dataset1").build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return stubResponse;
    }*/
}

