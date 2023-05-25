package io.github.jpmorganchase.fusion.api;

import java.io.InputStream;

public class FusionAPIDownloader implements APIDownloader {

    @Override
    public void callAPIFileDownload(
            String apiPath, String downloadFolder, String fileName, String catalog, String dataset)
            throws APICallException {}

    @Override
    public void callAPIFileDownload(String apiPath, String fileName, String catalog, String dataset)
            throws APICallException, FileDownloadException {}

    @Override
    public InputStream callAPIFileDownload(String apiPath, String catalog, String dataset)
            throws APICallException, FileDownloadException {
        return null;
    }

    private void download() {
        // Need to first of all check HEAD for entire file and identify if it is a mp or sp

        // GET
        // /v1/catalogs/{{catalog}}/datasets/{{dataset}}/datasetseries/{{series}}/distributions/csv/operationType/download
        // following headers always returned :: x-jpmc-checksum-sha256, x-jpmc-version-id, x-jpmc-latest-version-id
        // following header is returned if mp :: x-jpmc-mp-parts-count

    }

    private void head() {}

    private void downloadSinglePartFile() {}

    private void downloadMultiPartFile() {}
}
