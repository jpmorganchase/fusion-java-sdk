package io.github.jpmorganchase.fusion.credential;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Builder
public class OAuthDatasetCredentials implements Credentials {

    private String token;
    private String catalog;
    private String dataset;
    private String authServerUrl;


    @Override
    public CredentialType getCredentialType() {
        return CredentialType.DATASET;
    }
}
