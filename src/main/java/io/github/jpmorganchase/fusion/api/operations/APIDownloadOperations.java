package io.github.jpmorganchase.fusion.api.operations;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.exception.FileDownloadException;
import java.io.InputStream;

public interface APIDownloadOperations {

    default void callAPIFileDownload(String apiPath, String fileName, String catalog, String dataset)
            throws APICallException, FileDownloadException {}

    default InputStream callAPIFileDownload(String apiPath, String catalog, String dataset)
            throws APICallException, FileDownloadException {return null;}
}
