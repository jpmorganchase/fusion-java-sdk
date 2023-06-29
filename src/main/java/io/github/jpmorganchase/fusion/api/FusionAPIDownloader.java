package io.github.jpmorganchase.fusion.api;

import io.github.jpmorganchase.fusion.FusionException;
import io.github.jpmorganchase.fusion.api.exception.APICallException;
import io.github.jpmorganchase.fusion.api.exception.FileDownloadException;
import io.github.jpmorganchase.fusion.api.request.DownloadRequest;
import io.github.jpmorganchase.fusion.api.response.GetPartResponse;
import io.github.jpmorganchase.fusion.api.response.Head;
import io.github.jpmorganchase.fusion.api.stream.IntegrityCheckingInputStream;
import io.github.jpmorganchase.fusion.digest.DigestProducer;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.oauth.exception.OAuthException;
import io.github.jpmorganchase.fusion.oauth.provider.DatasetTokenProvider;
import io.github.jpmorganchase.fusion.oauth.provider.SessionTokenProvider;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Builder
@Getter
@Slf4j
public class FusionAPIDownloader implements APIDownloader {

    private static final String WRITE_TO_FILE_EXCEPTION_MSG =
            "Problem encountered attempting to write downloaded distribution to file";
    private static final String WRITE_TO_STREAM_EXCEPTION_MSG =
            "Problem encountered attempting to write downloaded distribution to stream";
    private static final String DOWNLOAD_FAILED_EXCEPTION_MSG =
            "Problem encountered attempting to download distribution";

    private static final String DEFAULT_FOLDER = "downloads";
    private static final String HEAD_PATH = "%s/operationType/download";
    private static final String HEAD_PATH_FOR_PART = "%s?downloadPartNumber=%d";

    private static final String FILE_RW_MODE = "rw";

    private final Client httpClient;
    private final SessionTokenProvider sessionTokenProvider;
    private final DatasetTokenProvider datasetTokenProvider;
    private final DigestProducer digestProducer;

    /**
     * Size of Thread-Pool to be used for uploading chunks of a multipart file
     * Defaults to number of available processors.
     */
    @Builder.Default
    int downloadThreadPoolSize = Runtime.getRuntime().availableProcessors();

    private final Object lock = new Object();

    /**
     * Calls the API to retrieve file data and saves to disk in the default location
     *
     * @param apiPath  the URL of the API endpoint to call
     * @param filePath the absolute path where the file will be persisted.
     * @param catalog the catalog the distribution to be downloaded is part of
     * @param dataset the dataset the distribution to be downloaded is a member of
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    @Override
    public void callAPIFileDownload(String apiPath, String filePath, String catalog, String dataset)
            throws APICallException, FileDownloadException {

        DownloadRequest dr = DownloadRequest.builder()
                .apiPath(apiPath)
                .filePath(filePath)
                .catalog(catalog)
                .dataset(dataset)
                .build();

        downloadToFile(dr);
    }

    /**
     * Calls the API to retrieve file data and returns as an InputStream
     *
     * @param apiPath the URL of the API endpoint to call
     * @param catalog the catalog the distribution to be downloaded is part of
     * @param dataset the dataset the distribution to be downloaded is a member of
     * @throws APICallException if the call to the Fusion API fails
     * @throws FileDownloadException if there is an issue handling the response from Fusion API
     * @throws OAuthException if a token could not be retrieved for authentication
     */
    @Override
    public InputStream callAPIFileDownload(String apiPath, String catalog, String dataset)
            throws APICallException, FileDownloadException {

        DownloadRequest dr = DownloadRequest.builder()
                .apiPath(apiPath)
                .catalog(catalog)
                .dataset(dataset)
                .isDownloadToStream(true)
                .build();

        return downloadToStream(dr);
    }

    protected void downloadToFile(DownloadRequest dr) {

        Head head = callAPIToGetHead(dr);
        if (head.isMultipart()) {
            performMultiPartDownloadToFile(dr, head);
        } else {
            performSinglePartDownloadToFile(dr);
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
                            GetPartResponse getPartResponse = callToAPIToGetPart(dr, part);
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

    protected GetPartResponse callToAPIToGetPart(DownloadRequest dr, int partNumber) {

        String getPartPath = getPathForHeadAndGet(dr, partNumber);

        Map<String, String> requestHeaders = new HashMap<>();
        setSecurityHeaders(dr, requestHeaders);

        HttpResponse<InputStream> response = httpClient.getInputStream(getPartPath, requestHeaders);

        checkResponseStatus(response);

        // TODO :: knighto - this is a good place to verify the digest

        return GetPartResponse.builder()
                .content(response.getBody())
                .head(Head.builder().fromHeaders(response.getHeaders()).build())
                .build();
    }

    public void performSinglePartDownloadToFile(DownloadRequest dr) throws APICallException {

        try (InputStream input = performSinglePartDownloadToStream(dr)) {
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
    }

    protected InputStream downloadToStream(DownloadRequest dr) {
        Head head = callAPIToGetHead(dr);
        if (head.isMultipart()) {
            return performMultiPartDownloadToStream(dr, head);
        } else {
            return performSinglePartDownloadToStream(dr);
        }
    }

    protected InputStream performMultiPartDownloadToStream(DownloadRequest dr, Head head) {

        ExecutorService executor = getExecutor();
        List<CompletableFuture<GetPartResponse>> futures = new ArrayList<>();

        try {

            for (int p = 1; p <= head.getPartCount(); p++) {
                final int part = p;
                futures.add(CompletableFuture.supplyAsync(() -> callToAPIToGetPart(dr, part), executor));
            }

            List<GetPartResponse> inputStreams = futures.stream()
                    .map(CompletableFuture::join)
                    .sorted(Comparator.comparingInt(gpr -> gpr.getHead().getPartCount()))
                    .collect(Collectors.toList());

            return IntegrityCheckingInputStream.of(inputStreams);

        } catch (CompletionException | CancellationException | IOException e) {
            throw handleExceptionThrownWhenAttemptingToGetParts(e);
        } finally {
            executor.shutdown();
        }
    }

    public InputStream performSinglePartDownloadToStream(DownloadRequest dr) throws APICallException {
        Map<String, String> requestHeaders = new HashMap<>();
        setSecurityHeaders(dr, requestHeaders);

        HttpResponse<InputStream> response = httpClient.getInputStream(dr.getApiPath(), requestHeaders);

        checkResponseStatus(response);
        return response.getBody();
    }

    /**
     * Returns the Head object representing the entire file
     *
     * @param dr {@link DownloadRequest}
     * @return {@link Head} object describing the file
     */
    protected Head callAPIToGetHead(DownloadRequest dr) {
        String headPath = getPathForHeadAndGet(dr, 0);

        Map<String, String> requestHeaders = new HashMap<>();
        setSecurityHeaders(dr, requestHeaders);

        HttpResponse<InputStream> headResponse = httpClient.getInputStream(headPath, requestHeaders);
        checkResponseStatus(headResponse);

        return Head.builder().fromHeaders(headResponse.getHeaders()).build();
    }

    private String getPathForHeadAndGet(DownloadRequest dr, int partNumber) {
        String headPath = String.format(HEAD_PATH, dr.getApiPath());
        if (partNumber > 0) {
            headPath = String.format(HEAD_PATH_FOR_PART, headPath, partNumber);
        }
        return headPath;
    }

    private FusionException handleExceptionThrownWhenAttemptingToGetParts(Exception ex) {

        log.error("Exception encountered downloading parts, cause is " + ex.getCause());

        if (ex.getCause() instanceof FusionException) {
            return (FusionException) ex.getCause();
        }

        Throwable cause = (null != ex.getCause() ? ex.getCause() : ex);
        return new FileDownloadException(DOWNLOAD_FAILED_EXCEPTION_MSG, cause);
    }

    private ExecutorService getExecutor() {
        return Executors.newFixedThreadPool(downloadThreadPoolSize);
    }

    /**
     * TODO : Definite duplication - this needs to be moved
     *
     * @param response to be verified
     * @param <T> type expected in the body of the response
     * @throws APICallException indicating a failure to communicate with Fusion API
     */
    private <T> void checkResponseStatus(HttpResponse<T> response) throws APICallException {
        if (response.isError()) {
            throw new APICallException(response.getStatusCode());
        }
    }

    /**
     * TODO : This is a candidate to extract out to its own class to be used by upload & download
     *
     * @param dr The {@link DownloadRequest} requiring security
     * @param requestHeaders pertaining to the request
     */
    private void setSecurityHeaders(DownloadRequest dr, Map<String, String> requestHeaders) {
        requestHeaders.put("Authorization", "Bearer " + sessionTokenProvider.getSessionBearerToken());
        requestHeaders.put(
                "Fusion-Authorization",
                "Bearer " + datasetTokenProvider.getDatasetBearerToken(dr.getCatalog(), dr.getDataset()));
    }
}
