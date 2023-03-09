package com.jpmorganchase.fusion.credential;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OAuthSecretBasedConfiguration extends OAuthConfiguration {
    @SerializedName("client_secret")
    String clientSecret;

    public OAuthSecretBasedConfiguration(String clientId, String resource, String authServerUrl, String clientSecret) {
        super(clientId, resource, authServerUrl);
        this.clientSecret = clientSecret;
    }
}
