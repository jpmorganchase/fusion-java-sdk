package io.github.jpmorganchase.fusion.api;

import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.exception.FileDownloadException;
import java.io.InputStream;

public interface APIDownloader {

    void callAPIFileDownload(String apiPath, String fileName, String catalog, String dataset)
            throws APICallException, FileDownloadException;

    InputStream callAPIFileDownload(String apiPath, String catalog, String dataset)
            throws APICallException, FileDownloadException;
}
