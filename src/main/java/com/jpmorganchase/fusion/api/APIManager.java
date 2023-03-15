package com.jpmorganchase.fusion.api;

import java.io.InputStream;

public interface APIManager {
    String callAPI(String apiPath) throws APICallException;

    void callAPIFileDownload(String apiPath, String downloadFolder, String fileName) throws APICallException;

    void callAPIFileDownload(String apiPath, String fileName) throws APICallException, FileDownloadException;

    InputStream callAPIFileDownload(String apiPath) throws APICallException, FileDownloadException;

    int callAPIFileUpload(String apiPath, String fileName, String fromDate, String toDate, String createdDate)
            throws APICallException;

    int callAPIFileUpload(String apiPath, InputStream data, String fromDate, String toDate, String createdDate)
            throws APICallException;
}
