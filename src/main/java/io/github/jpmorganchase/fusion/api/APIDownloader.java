package io.github.jpmorganchase.fusion.api;

import java.io.InputStream;

public interface APIDownloader {

    void callAPIFileDownload(String apiPath, String downloadFolder, String fileName, String catalog, String dataset)
            throws APICallException;

    void callAPIFileDownload(String apiPath, String fileName, String catalog, String dataset)
            throws APICallException, FileDownloadException;

    InputStream callAPIFileDownload(String apiPath, String catalog, String dataset)
            throws APICallException, FileDownloadException;
}
