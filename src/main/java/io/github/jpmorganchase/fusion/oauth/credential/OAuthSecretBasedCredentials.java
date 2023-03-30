package io.github.jpmorganchase.fusion.oauth.credential;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OAuthSecretBasedCredentials extends OAuthCredentials {
    @SerializedName("client_secret")
    String clientSecret;

    public OAuthSecretBasedCredentials(String clientId, String resource, String authServerUrl, String clientSecret) {
        super(clientId, resource, authServerUrl);
        this.clientSecret = clientSecret;
    }

    @Override
    public CredentialType getCredentialType() {
        return CredentialType.SECRET;
    }
}
