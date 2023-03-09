package com.jpmorganchase.fusion.credential;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OAuthPasswordBasedConfiguration extends OAuthConfiguration {
    String username;
    String password;

    public OAuthPasswordBasedConfiguration(String clientId, String resource, String authServerUrl, String username, String password) {
        super(clientId, resource, authServerUrl);
        this.username = username;
        this.password = password;
    }
}
