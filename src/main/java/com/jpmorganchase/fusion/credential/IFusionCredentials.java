package com.jpmorganchase.fusion.credential;

import java.io.IOException;

public interface IFusionCredentials {
    //TODO: Revisit synchronization
    //TODO: Copied wholesale from FusionAPIManager for now, needs refactoring
    String getBearerToken() throws IOException;

    //TODO: This really shouldn't be in here
    boolean useProxy();
}
