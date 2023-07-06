package io.github.jpmorganchase.fusion;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FusionConfiguration {

    /**
     * Defines the fusion root url to be used with api interactions.
     * Defaults to "https://fusion-api.jpmorgan.com/fusion/v1/".
     */
    @Builder.Default
    String rootURL = "https://fusion-api.jpmorgan.com/fusion/v1/";

    /**
     * Defines the path to the credentials file for auth/authz.
     * Defaults to "config/client_credentials.json".
     */
    @Builder.Default
    String credentialsPath = "config/client_credentials.json";

    /***
     * Set the default catalog to be used with simplified API calls
     * Defaults to "common"
     */
    @Builder.Default
    String defaultCatalog = "common";

    /**
     * Configures the path where distributions should be downloaded to.
     * Defaults to "downloads"
     */
    @Builder.Default
    String downloadPath = "downloads";

    /**
     * Max size in MB of data allowed for a single part upload.
     * if 32MB was the max size then 32 would be provided.
     * <p>
     * Defaults to 50MB aka 50.
     */
    @Builder.Default
    int singlePartUploadSizeLimit = 50;

    /**
     * Upload part chunk size. Defaults to 16MB.
     * If a value such as 8MB is required, then client would set this value to 8
     */
    @Builder.Default
    int uploadPartSize = 8;

    /**
     * Size of Thread-Pool to be used for uploading chunks of a multipart file
     * Defaults to number of available processors.
     */
    @Builder.Default
    int uploadThreadPoolSize = Runtime.getRuntime().availableProcessors();

    /**
     * Size of Thread-Pool to be used for uploading chunks of a multipart file
     * Defaults to number of available processors.
     */
    @Builder.Default
    int downloadThreadPoolSize = Runtime.getRuntime().availableProcessors();

    /**
     * Digest algorithm used by fusion to verify the integrity of upload/downloads
     * Defaults to SHA-256.
     */
    @Builder.Default
    String digestAlgorithm = "SHA-256";

}
