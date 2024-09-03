package io.github.jpmorganchase.fusion.api.operations;

import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.FusionException;
import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.exception.FileDownloadException;
import io.github.jpmorganchase.fusion.api.request.*;
import io.github.jpmorganchase.fusion.api.response.GetPartResponse;
import io.github.jpmorganchase.fusion.api.response.Head;
import io.github.jpmorganchase.fusion.api.stream.DeferredMultiPartInputStream;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.oauth.exception.OAuthException;
import io.github.jpmorganchase.fusion.oauth.provider.FusionTokenProvider;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Builder
@Getter
@Slf4j
public class FusionAPIDownloadOperations implements APIDownloadOperations {

    private static final String WRITE_TO_FILE_EXCEPTION_MSG =
            "Problem encountered attempting to write downloaded distribution to file";
    private static final String WRITE_TO_STREAM_EXCEPTION_MSG =
            "Problem encountered attempting to write downloaded distribution to stream";
    private static final String DOWNLOAD_FAILED_EXCEPTION_MSG =
            "Problem encountered attempting to download distribution";

    private static final String FILE_RW_MODE = "rw";

    private PartFetcher partFetcher;

    /**
     * Size of Thread-Pool to be used for uploading chunks of a multipart file
     * See {@link FusionConfiguration} for defaults.
     */
    int downloadThreadPoolSize;

    private final Object lock = new Object();

    /**
     * Calls the API to retrieve file data and saves to disk in the default location
     *
     * @param apiPath  the URL of the API endpoint to call
     * @param filePath the absolute path where the file will be persisted.
     * @param catalog the catalog the distribution to be downloaded is part of
     * @param dataset the dataset the distribution to be downloaded is a member of
     * @param headers http headers to be provided in the request.  For headers with multiple instances, the value should be a csv list
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    @Override
    public void callAPIFileDownload(String apiPath, String filePath, String catalog, String dataset, Map<String, String> headers)
            throws APICallException, FileDownloadException {

        DownloadRequest dr = DownloadRequest.builder()
                .apiPath(apiPath)
                .filePath(filePath)
                .catalog(catalog)
                .dataset(dataset)
                .headers(headers)
                .build();

        downloadToFile(dr);
    }

    /**
     * Calls the API to retrieve file data and returns as an InputStream
     *
     * @param apiPath the URL of the API endpoint to call
     * @param catalog the catalog the distribution to be downloaded is part of
     * @param dataset the dataset the distribution to be downloaded is a member of
     * @param headers http headers to be provided in the request.  For headers with multiple instances, the value should be a csv list
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    @Override
    public InputStream callAPIFileDownload(String apiPath, String catalog, String dataset, Map<String, String> headers)
            throws APICallException, FileDownloadException {

        DownloadRequest dr = DownloadRequest.builder()
                .apiPath(apiPath)
                .catalog(catalog)
                .dataset(dataset)
                .isDownloadToStream(true)
                .headers(headers)
                .build();

        return downloadToStream(dr);
    }

    protected void downloadToFile(DownloadRequest dr) {

        Head head = callAPIToGetHead(dr);
        if (head.isMultipart()) {
            performMultiPartDownloadToFile(dr, head);
        } else {
            performSinglePartDownloadToFile(dr, head);
        }
    }

    protected void performMultiPartDownloadToFile(DownloadRequest dr, Head head) {

        ExecutorService executor = getExecutor();

        try (RandomAccessFile raf = new RandomAccessFile(dr.getFilePath(), FILE_RW_MODE)) {

            raf.setLength(head.getContentLength());
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int p = 1; p <= head.getPartCount(); p++) {
                final int part = p;
                futures.add(CompletableFuture.runAsync(
                        () -> {
                            GetPartResponse getPartResponse = partFetcher.fetch(PartRequest.builder()
                                    .partNo(part)
                                    .downloadRequest(dr)
                                    .build());
                            writePartToFile(getPartResponse, raf);
                        },
                        executor));
            }

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allFutures.join();

        } catch (IOException | CompletionException | CancellationException ex) {
            throw handleExceptionThrownWhenAttemptingToGetParts(ex);
        } finally {
            executor.shutdown();
        }
        log.info("Distribution downloaded to file {}", dr.getFilePath());
    }

    private void writePartToFile(GetPartResponse gpr, RandomAccessFile raf) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream input = gpr.getContent()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            synchronized (lock) {
                raf.seek(gpr.getHead().getContentRange().getStart());
                raf.write(baos.toByteArray());
            }

        } catch (IOException ex) {
            throw new FileDownloadException(WRITE_TO_FILE_EXCEPTION_MSG, ex);
        }
    }

    public void performSinglePartDownloadToFile(DownloadRequest dr, Head head) throws APICallException {
        try (InputStream input = performSinglePartDownloadToStream(dr, head)) {
            try (FileOutputStream fileOutput = new FileOutputStream(dr.getFilePath())) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = input.read(buf)) != -1) {
                    fileOutput.write(buf, 0, len);
                }
            }
        } catch (IOException e) {
            throw new FileDownloadException(WRITE_TO_FILE_EXCEPTION_MSG, e);
        }
        log.info("Distribution downloaded to file {}", dr.getFilePath());
    }

    protected InputStream downloadToStream(DownloadRequest dr) {
        Head head = callAPIToGetHead(dr);
        if (head.isMultipart()) {
            return performMultiPartDownloadToStream(dr, head);
        } else {
            return performSinglePartDownloadToStream(dr, head);
        }
    }

    private Head callAPIToGetHead(DownloadRequest dr) {
        return partFetcher
                .fetch(PartRequest.builder().partNo(0).downloadRequest(dr).build())
                .getHead();
    }

    protected InputStream performMultiPartDownloadToStream(DownloadRequest dr, Head head) {

        LinkedList<CallablePart> parts = new LinkedList<>();
        try {

            for (int p = 1; p <= head.getPartCount(); p++) {
                parts.add(CallablePart.builder()
                        .partNo(p)
                        .partFetcher(partFetcher)
                        .downloadRequest(dr)
                        .build());
            }

            return DeferredMultiPartInputStream.builder()
                    .parts(CallableParts.builder().parts(parts).build())
                    .build();

        } catch (IOException e) {
            throw handleExceptionThrownWhenAttemptingToGetParts(e);
        }
    }

    public InputStream performSinglePartDownloadToStream(DownloadRequest dr, Head head) throws APICallException {
        return partFetcher
                .fetch(PartRequest.builder()
                        .partNo(1)
                        .head(head)
                        .downloadRequest(dr)
                        .build())
                .getContent();
    }

    private FusionException handleExceptionThrownWhenAttemptingToGetParts(Exception ex) {
        if (ex.getCause() instanceof FusionException) {
            return (FusionException) ex.getCause();
        }

        Throwable cause = (null != ex.getCause() ? ex.getCause() : ex);
        return new FileDownloadException(DOWNLOAD_FAILED_EXCEPTION_MSG, cause);
    }

    private ExecutorService getExecutor() {
        return Executors.newFixedThreadPool(downloadThreadPoolSize);
    }

    public static FusionAPIDownloadOperationsBuilder builder() {
        return new CustomFusionAPIDownloadOperationsBuilder();
    }

    public static class FusionAPIDownloadOperationsBuilder {

        protected FusionConfiguration configuration =
                FusionConfiguration.builder().build();

        int downloadThreadPoolSize;
        Client httpClient;

        FusionTokenProvider fusionTokenProvider;

        PartFetcher partFetcher;

        public FusionAPIDownloadOperationsBuilder configuration(FusionConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public FusionAPIDownloadOperationsBuilder httpClient(Client httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public FusionAPIDownloadOperationsBuilder fusionTokenProvider(FusionTokenProvider fusionTokenProvider) {
            this.fusionTokenProvider = fusionTokenProvider;
            return this;
        }

        @SuppressWarnings("PIT")
        private FusionAPIDownloadOperationsBuilder downloadThreadPoolSize(int downloadThreadPoolSize) {
            this.downloadThreadPoolSize = downloadThreadPoolSize;
            return this;
        }

        public FusionAPIDownloadOperationsBuilder partFetcher(PartFetcher partFetcher) {
            this.partFetcher = partFetcher;
            return this;
        }
    }

    private static class CustomFusionAPIDownloadOperationsBuilder extends FusionAPIDownloadOperationsBuilder {
        @Override
        public FusionAPIDownloadOperations build() {
            this.downloadThreadPoolSize = configuration.getDownloadThreadPoolSize();

            if (Objects.isNull(partFetcher))
                this.partFetcher = PartFetcher.builder()
                        .client(httpClient)
                        .credentials(fusionTokenProvider)
                        .build();

            return super.build();
        }
    }
}
