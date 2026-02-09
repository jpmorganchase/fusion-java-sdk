package io.github.jpmorganchase.fusion.api.operations;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.exception.FileDownloadException;
import java.io.InputStream;
import java.util.Map;

public interface APIDownloadOperations {

    default void callAPIFileDownload(
            String apiPath, String fileName, String catalog, String dataset, Map<String, String> headers)
            throws APICallException, FileDownloadException {
        callAPIFileDownload(apiPath, fileName, catalog, dataset, headers, null, false);
    }

    default void callAPIFileDownload(
            String apiPath,
            String fileName,
            String catalog,
            String dataset,
            Map<String, String> headers,
            boolean skipChecksumValidationIfMissing)
            throws APICallException, FileDownloadException {
        callAPIFileDownload(apiPath, fileName, catalog, dataset, headers, null, skipChecksumValidationIfMissing);
    }

    default void callAPIFileDownload(
            String apiPath,
            String fileName,
            String catalog,
            String dataset,
            Map<String, String> headers,
            String fileIdentifier,
            boolean skipChecksumValidationIfMissing)
            throws APICallException, FileDownloadException {}

    default InputStream callAPIFileDownload(String apiPath, String catalog, String dataset, Map<String, String> headers)
            throws APICallException, FileDownloadException {
        return callAPIFileDownload(apiPath, catalog, dataset, headers, null, false);
    }

    default InputStream callAPIFileDownload(
            String apiPath,
            String catalog,
            String dataset,
            Map<String, String> headers,
            boolean skipChecksumValidationIfMissing)
            throws APICallException, FileDownloadException {
        return callAPIFileDownload(apiPath, catalog, dataset, headers, null, skipChecksumValidationIfMissing);
    }

    default InputStream callAPIFileDownload(
            String apiPath,
            String catalog,
            String dataset,
            Map<String, String> headers,
            String fileIdentifier,
            boolean skipChecksumValidationIfMissing)
            throws APICallException, FileDownloadException {
        return null;
    }
}
