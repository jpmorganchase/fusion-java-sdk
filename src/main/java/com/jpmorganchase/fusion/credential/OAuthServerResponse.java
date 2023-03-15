package com.jpmorganchase.fusion.credential;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OAuthServerResponse {

    private static final Gson gson = new GsonBuilder().create();

    @SerializedName("access_token")
    String accessToken;

    @SerializedName("token_type")
    String tokenType;

    @SerializedName("expires_in")
    int expiresIn;

    @SerializedName("id_token")
    String idToken;

    public static OAuthServerResponse fromJson(String json) {
        return gson.fromJson(json, OAuthServerResponse.class);
    }
}
