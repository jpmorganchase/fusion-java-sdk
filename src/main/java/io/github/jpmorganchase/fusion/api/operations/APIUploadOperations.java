package io.github.jpmorganchase.fusion.api.operations;

import io.github.jpmorganchase.fusion.api.exception.APICallException;

import java.io.InputStream;
import java.util.Map;

public interface APIUploadOperations {

    default void callAPIFileUpload(
            String apiPath,
            String fileName,
            String catalogName,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate,
            Map<String, String> headers)
            throws APICallException {}

    default void callAPIFileUpload(
            String apiPath,
            InputStream data,
            String catalogName,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate,
            Map<String, String> headers)
            throws APICallException {}
}
