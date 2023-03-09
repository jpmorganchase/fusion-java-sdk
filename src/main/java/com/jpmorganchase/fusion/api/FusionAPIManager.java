package com.jpmorganchase.fusion.api;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface FusionAPIManager {
    String callAPI(String apiPath) throws APICallException, IOException;

    void callAPIFileDownload(String apiPath, String downloadFolder, String fileName) throws IOException, APICallException;

    void callAPIFileDownload(String apiPath, String fileName) throws IOException, APICallException;

    //TODO: Sort out error handling
    int callAPIFileUpload(String apiPath, String fileName, String fromDate, String toDate, String createdDate) throws APICallException, IOException, NoSuchAlgorithmException;
}
