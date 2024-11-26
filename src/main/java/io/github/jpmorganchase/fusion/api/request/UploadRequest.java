package io.github.jpmorganchase.fusion.api.request;

import io.github.jpmorganchase.fusion.api.exception.ApiInputValidationException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class UploadRequest {

    private static final String FILE_READ_ERROR = "File does not exist at supplied input location: %s";
    private static final String STREAM_READ_ERROR = "Unable to read data from provided stream";

    private String apiPath;
    private String catalog;
    private String dataset;
    private String fromDate;
    private String toDate;
    private String createdDate;
    private InputStream data;

    private boolean isFromStream;
    private boolean isMultiPartUploadCandidate;

    private Map<String, String> headers;

    /**
     * Returns a copy of the header map that was provided by the client
     * @return headers
     */
    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public static class UploadRequestBuilder {
        private String fileName;
        private InputStream fromStream;
        private int maxSinglePartFileSize;
        private Map<String, String> headers;

        public UploadRequestBuilder fromFile(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public UploadRequestBuilder fromStream(InputStream fromStream) {
            this.fromStream = fromStream;
            return this;
        }

        public UploadRequestBuilder maxSinglePartFileSize(int maxSinglePartFileSize) {
            this.maxSinglePartFileSize = maxSinglePartFileSize;
            return this;
        }

        @SuppressWarnings("PIT")
        private UploadRequestBuilder isFromStream(boolean isFromStream) {
            return this;
        }

        @SuppressWarnings("PIT")
        private UploadRequestBuilder isMultiPartUploadCandidate(boolean isMultiPartUploadCandidate) {
            return this;
        }

        @SuppressWarnings("PIT")
        private UploadRequestBuilder size(long size) {
            return this;
        }

        @SuppressWarnings("PIT")
        private UploadRequestBuilder file(File file) {
            return this;
        }

        @SuppressWarnings("PIT")
        private UploadRequestBuilder data(InputStream data) {
            return this;
        }

        /**
         * Builder used to return an UploadRequest used to manage the core data needed to upload a distribution to fusion.
         * @return a {@link UploadRequest} ready to be used
         * @throws ApiInputValidationException if there is a problem with the inputs provided
         */
        public UploadRequest build() {

            if (maxSinglePartFileSize <= 0) {
                throw new ApiInputValidationException("Max single part file size must be specified");
            }

            if (fileName != null) {
                buildFromFile();
            }

            if (null == this.data && null != this.fromStream) {
                buildFromStream();
            }

            Map<String, String> copyOfHeaders = copyHeaderMap();

            return new UploadRequest(
                    apiPath,
                    catalog,
                    dataset,
                    fromDate,
                    toDate,
                    createdDate,
                    data,
                    isFromStream,
                    isMultiPartUploadCandidate,
                    copyOfHeaders);
        }

        private Map<String, String> copyHeaderMap() {
            if (null == this.headers) {
                return new HashMap<>();
            }
            return new HashMap<>(this.headers);
        }

        private void buildFromStream() {

            this.isFromStream = true;

            BufferedInputStream bufferedInputStream = new BufferedInputStream(fromStream);
            bufferedInputStream.mark((maxSinglePartFileSize + 1) * (1024 * 1024));

            int totalBytes = 0;
            int bytesRead;
            byte[] buffer = new byte[1024 * 1024];

            try {
                while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                    totalBytes += bytesRead;
                    if (totalBytes >= (maxSinglePartFileSize * (1024.0 * 1024.0))) {
                        this.isMultiPartUploadCandidate = true;
                        break;
                    }
                }
                bufferedInputStream.reset();
                this.data = bufferedInputStream;

            } catch (IOException e) {
                throw new ApiInputValidationException(STREAM_READ_ERROR, e);
            }
        }

        private void buildFromFile() {
            this.isFromStream = false;
            File file = new File(fileName);
            try {

                if (file.length() > (maxSinglePartFileSize * (1024.0 * 1024.0))) {
                    this.isMultiPartUploadCandidate = true;
                }

                this.data = Files.newInputStream(file.toPath());
            } catch (IOException e) {
                throw new ApiInputValidationException(String.format(FILE_READ_ERROR, fileName), e);
            }
        }
    }
}
