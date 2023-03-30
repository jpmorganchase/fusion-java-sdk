package io.github.jpmorganchase.fusion.oauth.credential;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public abstract class OAuthCredentials implements Credentials {

    @SerializedName("client_id")
    private final String clientId;

    private final String resource;

    @SerializedName("auth_url")
    private final String authServerUrl;
}
