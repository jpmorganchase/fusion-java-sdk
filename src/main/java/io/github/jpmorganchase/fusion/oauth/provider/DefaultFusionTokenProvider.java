package io.github.jpmorganchase.fusion.oauth.provider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.FusionInitialisationException;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.JdkClient;
import io.github.jpmorganchase.fusion.oauth.credential.Credentials;
import io.github.jpmorganchase.fusion.oauth.credential.OAuthSecretBasedCredentials;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.Builder;

@Builder
public class DefaultFusionTokenProvider implements FusionTokenProvider {

    private SessionTokenProvider sessionTokenProvider;
    private DatasetTokenProvider datasetTokenProvider;

    @Override
    public String getDatasetBearerToken(String catalog, String dataset) {
        return datasetTokenProvider.getDatasetBearerToken(catalog, dataset);
    }

    @Override
    public String getSessionBearerToken() {
        return sessionTokenProvider.getSessionBearerToken();
    }

    @Override
    public void updateCredentials(Credentials credentials) {
        sessionTokenProvider.updateCredentials(credentials);
    }

    public static DefaultFusionTokenProviderBuilder builder() {
        return new CustomDefaultFusionTokenProviderBuilder();
    }

    public static class DefaultFusionTokenProviderBuilder {

        protected Client client;
        protected Credentials credentials;
        protected SessionTokenProvider sessionTokenProvider;
        protected DatasetTokenProvider datasetTokenProvider;

        protected FusionConfiguration configuration =
                FusionConfiguration.builder().build();

        public DefaultFusionTokenProviderBuilder client(Client client) {
            this.client = client;
            return this;
        }

        public DefaultFusionTokenProviderBuilder credentials(Credentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public DefaultFusionTokenProviderBuilder configuration(FusionConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }
    }

    private static class CustomDefaultFusionTokenProviderBuilder extends DefaultFusionTokenProviderBuilder {

        @Override
        public DefaultFusionTokenProvider build() {

            if (null == client) {
                client = JdkClient.builder().noProxy().build();
            }

            if (null == sessionTokenProvider && null == credentials) {
                Gson gson = new GsonBuilder().create();
                try {
                    // Java 8 doesn't allow specification of the charset if we use a FileReader
                    InputStreamReader fileReader = new InputStreamReader(
                            Files.newInputStream(Paths.get(configuration.getCredentialsPath())),
                            StandardCharsets.UTF_8);
                    credentials = gson.fromJson(fileReader, OAuthSecretBasedCredentials.class);
                    fileReader.close();
                } catch (IOException e) {
                    throw new FusionInitialisationException(
                            String.format(
                                    "Failed to load credential file from path: %s", configuration.getCredentialsPath()),
                            e);
                }
            }

            if (null == sessionTokenProvider && null == credentials) {
                throw new FusionInitialisationException("Failed to initialise, no credentials defined");
            }

            if (null == sessionTokenProvider) {
                this.sessionTokenProvider = new OAuthSessionTokenProvider(credentials, client);
            }

            if (null == datasetTokenProvider) {
                this.datasetTokenProvider =
                        new OAuthDatasetTokenProvider(configuration.getRootURL(), sessionTokenProvider, client);
            }

            return super.build();
        }
    }
}
