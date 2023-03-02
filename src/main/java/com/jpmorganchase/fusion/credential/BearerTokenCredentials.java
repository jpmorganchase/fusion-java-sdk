package com.jpmorganchase.fusion.credential;

import lombok.Value;

@Value
public class BearerTokenCredentials implements IFusionCredentials{

    String bearerToken;

    @Override
    public boolean useProxy() {
        return false;
    }
}
