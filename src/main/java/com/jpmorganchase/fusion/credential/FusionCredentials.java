package com.jpmorganchase.fusion.credential;

import java.io.IOException;

public interface FusionCredentials {
    String getBearerToken() throws IOException;

    //TODO: This really shouldn't be in here
    boolean useProxy();
}
