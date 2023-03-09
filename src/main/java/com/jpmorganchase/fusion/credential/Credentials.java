package com.jpmorganchase.fusion.credential;

import java.io.IOException;

public interface Credentials {
    String getBearerToken() throws IOException;
}
