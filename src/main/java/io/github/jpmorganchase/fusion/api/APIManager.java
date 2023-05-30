package io.github.jpmorganchase.fusion.api;

public interface APIManager {
    String callAPI(String apiPath) throws APICallException;
}
