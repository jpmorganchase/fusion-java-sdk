package com.jpmorganchase.fusion.credential;

import java.io.IOException;

public interface FusionCredentials {
    String getBearerToken() throws IOException;
}
