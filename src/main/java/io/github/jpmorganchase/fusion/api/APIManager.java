package io.github.jpmorganchase.fusion.api;

import io.github.jpmorganchase.fusion.api.exception.APICallException;

public interface APIManager {
    String callAPI(String apiPath) throws APICallException;
}
