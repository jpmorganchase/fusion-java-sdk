package io.github.jpmorganchase.fusion.credential;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OAuthPasswordBasedCredentials extends OAuthCredentials {
    String username;
    String password;

    public OAuthPasswordBasedCredentials(
            String clientId, String resource, String authServerUrl, String username, String password) {
        super(clientId, resource, authServerUrl);
        this.username = username;
        this.password = password;
    }
}
