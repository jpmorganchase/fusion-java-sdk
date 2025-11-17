package io.github.jpmorganchase.fusion.api.request;

import static io.github.jpmorganchase.fusion.api.tools.ResponseChecker.checkResponseStatus;

import io.github.jpmorganchase.fusion.FusionConfiguration;
import io.github.jpmorganchase.fusion.api.response.GetPartResponse;
import io.github.jpmorganchase.fusion.api.response.Head;
import io.github.jpmorganchase.fusion.api.stream.IntegrityCheckingInputStream;
import io.github.jpmorganchase.fusion.digest.PartChecker;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.oauth.provider.FusionTokenProvider;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@ToString
public class PartFetcher {

    private static final String HEAD_PATH = "%s/operationType/download";
    private static final String HEAD_PATH_FOR_PART = "%s?downloadPartNumber=%d";

    Client client;
    FusionTokenProvider credentials;
    FusionConfiguration configuration;

    /**
     * Makes a call to get the part corresponding to the part number in the {@link PartRequest}.
     * If optional {@link Head} is not specified, the {@link Head} from the response will be
     * returned.  For single part downloads, {@link Head} should always be specified in the {@link} PartRequest to
     * ensure the correct checksum is used to verify the download.
     * <p>
     * To simply return the head object of a download, part number should be provided as 0.
     *
     * @param pr - {@link PartRequest}
     * @return {@link GetPartResponse}
     */
    public GetPartResponse fetch(PartRequest pr) {
        HttpResponse<InputStream> response = callClientForInputStream(pr);
        checkResponseStatus(response);

        Head head = getHead(response, pr);
        InputStream inputStream = getIntegrityCheckingInputStream(response, head);

        return GetPartResponse.builder().content(inputStream).head(head).build();
    }

    private IntegrityCheckingInputStream getIntegrityCheckingInputStream(
            HttpResponse<InputStream> response, Head head) {
        return IntegrityCheckingInputStream.builder()
                .part(response.getBody())
                .checksum(head.getChecksum())
                .partChecker(PartChecker.builder()
                        .digestAlgo(getDigestAlgo(head))
                        .skipChecksum(configuration.isSkipCheckSumValidation())
                        .build())
                .build();
    }

    private String getDigestAlgo(Head head) {
        return head.getChecksumAlgorithm() != null ? head.getChecksumAlgorithm() : configuration.getDigestAlgorithm();
    }

    private Head getHead(HttpResponse<InputStream> response, PartRequest pr) {
        if (pr.isHeadRequired()) {
            return Head.builder().fromHeaders(response.getHeaders()).build();
        }
        return pr.getHead();
    }

    private HttpResponse<InputStream> callClientForInputStream(PartRequest pr) {
        return client.getInputStream(getPath(pr), getSecurityHeaders(pr));
    }

    private Map<String, String> getSecurityHeaders(PartRequest pr) {
        DownloadRequest dr = pr.getDownloadRequest();
        Map<String, String> headers = Optional.ofNullable(dr.getHeaders()).orElse(new HashMap<>());
        headers.put("Authorization", "Bearer " + credentials.getSessionBearerToken());
        headers.put(
                "Fusion-Authorization",
                "Bearer " + credentials.getDatasetBearerToken(dr.getCatalog(), dr.getDataset()));
        return headers;
    }

    private String getPath(PartRequest pr) {
        String path;

        if (pr.isHeadRequest()) {
            path = getHeadPath(pr);
        } else if (pr.isSinglePartDownloadRequest()) {
            path = getSinglePartPath(pr);
        } else {
            path = getMultiPartPath(pr);
        }

        return path;
    }

    private String getSinglePartPath(PartRequest pr) {
        return pr.getDownloadRequest().getApiPath();
    }

    private String getMultiPartPath(PartRequest pr) {
        return String.format(HEAD_PATH_FOR_PART, getHeadPath(pr), pr.getPartNo());
    }

    private String getHeadPath(PartRequest pr) {
        return String.format(HEAD_PATH, pr.getDownloadRequest().getApiPath());
    }
}
