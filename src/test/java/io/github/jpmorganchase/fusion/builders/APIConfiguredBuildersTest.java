package io.github.jpmorganchase.fusion.builders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.api.APIManager;
import io.github.jpmorganchase.fusion.model.Dataset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class APIConfiguredBuildersTest {
    private APIConfiguredBuilders apiConfiguredBuilders;
    private FusionConfiguration configuration;

    @BeforeEach
    public void setUp() {

        APIManager mockApiManager = Mockito.mock(APIManager.class);
        configuration = Mockito.mock(FusionConfiguration.class);
        apiConfiguredBuilders = APIConfiguredBuilders.builder()
                .apiManager(mockApiManager)
                .configuration(configuration)
                .build();
    }

    @Test
    public void testDatasetBuilderReturned() {
        // Given
        Mockito.when(configuration.getRootURL()).thenReturn("http://foobar.com/api/v1");
        Mockito.when(configuration.getDefaultCatalog()).thenReturn("foobar");

        Dataset.DatasetBuilder datasetBuilder = apiConfiguredBuilders.dataset();
        assertNotNull(datasetBuilder);

        Dataset dataset = datasetBuilder.build();
        assertThat(dataset.getRootUrl(), is(equalTo("http://foobar.com/api/v1")));
        assertThat(dataset.getCatalogIdentifier(), is(equalTo("foobar")));
    }
}
