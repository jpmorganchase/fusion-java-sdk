package com.jpmorganchase.fusion.credential;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OAuthServerResponse {

    @SerializedName("access_token")
    String accessToken;
    @SerializedName("token_type")
    String tokenType;
    @SerializedName("expires_in")
    int expiresIn;
    @SerializedName("id_token")
    String idToken;

    //TODO: Move to another class? Figure out what to do to stop creating new Gson objects all the time (thread-safe to be a static?)
    public static OAuthServerResponse fromJson(String json){
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, OAuthServerResponse.class);
    }
}
