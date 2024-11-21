package io.github.jpmorganchase.fusion.parsing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.api.context.APIContext;
import io.github.jpmorganchase.fusion.model.DataProduct;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GsonAPIResponseParserDataProductTest {

    private static final String singleProductJson = loadTestResource("single-product-response.json");
    private static final String multipleProductJson = loadTestResource("multiple-product-response.json");

    // TODO: Need to map out all the fields
    private static final DataProduct testProduct = DataProduct.builder()
            .identifier("SDP001")
            .description("Sample data product 1")
            .linkedEntity("SDP001/")
            .status("Available")
            .title("Sample Data Product 1 Title")
            .build();

    private static final DataProduct testProduct2 = DataProduct.builder()
            .identifier("SDP002")
            .description("Sample data product 2")
            .linkedEntity("SDP002/")
            .status("Available")
            .title("Sample Data Product 2 Title")
            .build();

    private static final DataProduct testProduct3 = DataProduct.builder()
            .identifier("SDP003")
            .description("Sample data product 3")
            .linkedEntity("SDP003/")
            .status("Available")
            .title("Sample Data Product 3 Title")
            .build();

    private static final APIContext apiContext = APIContext.builder()
            .apiManager(Mockito.mock(APIManager.class))
            .rootUrl("http://foobar/api/v1/")
            .defaultCatalog("foobar")
            .build();
    private static final APIResponseParser responseParser = new GsonAPIResponseParser(apiContext);

    @Test
    public void singleProductInResourcesParsesCorrectly() {
        Map<String, DataProduct> productMap = responseParser.parseDataProductResponse(singleProductJson);
        assertThat(productMap.size(), is(1));

        DataProduct testProductResponse = productMap.get("SDP001");
        assertThat(testProductResponse, is(equalTo(testProduct)));
    }

    @Test
    public void multipleCatalogsInResourcesParseCorrectly() {
        Map<String, DataProduct> productMap = responseParser.parseDataProductResponse(multipleProductJson);
        assertThat(productMap.size(), is(3));

        DataProduct testProductResponse = productMap.get("SDP001");
        assertThat(testProductResponse, is(equalTo(testProduct)));

        DataProduct testProductResponse2 = productMap.get("SDP002");
        assertThat(testProductResponse2, is(equalTo(testProduct2)));

        DataProduct testProductResponse3 = productMap.get("SDP003");
        assertThat(testProductResponse3, is(equalTo(testProduct3)));
    }

    private static String loadTestResource(String resourceName) {
        URL url = GsonAPIResponseParser.class.getResource(resourceName);
        try {
            Path path = Paths.get(url.toURI());
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data", e);
        }
    }
}
