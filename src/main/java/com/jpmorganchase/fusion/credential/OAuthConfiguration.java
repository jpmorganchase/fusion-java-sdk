package com.jpmorganchase.fusion.credential;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class OAuthConfiguration {

    @SerializedName("client_id")
    private final String clientId;
    private final String resource;
    @SerializedName("auth_url")
    private final String authServerUrl;
    private String bearerToken;
}
