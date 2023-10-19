package io.github.jpmorganchase.fusion.oauth.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import io.github.jpmorganchase.fusion.oauth.credential.Credentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultFusionTokenProviderTest {

    @Mock
    private Credentials credentials;

    @Mock
    private SessionTokenProvider sessionTokenProvider;

    @Mock
    private DatasetTokenProvider datasetTokenProvider;

    private DefaultFusionTokenProvider fusionTokenProvider;

    @BeforeEach
    public void setUp() {
        fusionTokenProvider = DefaultFusionTokenProvider.builder()
                .sessionTokenProvider(sessionTokenProvider)
                .datasetTokenProvider(datasetTokenProvider)
                .build();
    }

    @Test
    public void shouldGetDatasetBearerToken() {
        String catalog = "myCatalog";
        String dataset = "myDataset";
        String expectedToken = "datasetBearerToken";
        when(datasetTokenProvider.getDatasetBearerToken(catalog, dataset)).thenReturn(expectedToken);

        String token = fusionTokenProvider.getDatasetBearerToken(catalog, dataset);

        assertEquals(expectedToken, token);
        verify(datasetTokenProvider, times(1)).getDatasetBearerToken(catalog, dataset);
    }

    @Test
    public void shouldGetSessionBearerToken() {
        String expectedToken = "sessionBearerToken";
        when(sessionTokenProvider.getSessionBearerToken()).thenReturn(expectedToken);

        String token = fusionTokenProvider.getSessionBearerToken();

        assertEquals(expectedToken, token);
        verify(sessionTokenProvider, times(1)).getSessionBearerToken();
    }

    @Test
    public void shouldUpdateCredentials() {
        Credentials credentials = mock(Credentials.class);

        fusionTokenProvider.updateCredentials(credentials);

        verify(sessionTokenProvider, times(1)).updateCredentials(credentials);
    }
}
