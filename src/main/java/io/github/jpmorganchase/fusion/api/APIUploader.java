package io.github.jpmorganchase.fusion.api;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import java.io.InputStream;

public interface APIUploader {

    void callAPIFileUpload(
            String apiPath,
            String fileName,
            String catalogName,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate)
            throws APICallException;

    void callAPIFileUpload(
            String apiPath,
            InputStream data,
            String catalogName,
            String dataset,
            String fromDate,
            String toDate,
            String createdDate)
            throws APICallException;
}
