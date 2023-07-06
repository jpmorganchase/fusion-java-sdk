package io.github.jpmorganchase.fusion.api;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.operations.APIDownloadOperations;
import io.github.jpmorganchase.fusion.api.operations.APIUploadOperations;

public interface APIManager extends APIDownloadOperations, APIUploadOperations {
    String callAPI(String apiPath) throws APICallException;
}
