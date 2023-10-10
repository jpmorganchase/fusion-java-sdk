package io.github.jpmorganchase.fusion.api.request;

import static io.github.jpmorganchase.fusion.api.tools.ResponseChecker.checkResponseStatus;

import io.github.jpmorganchase.fusion.api.response.GetPartResponse;
import io.github.jpmorganchase.fusion.api.response.Head;
import io.github.jpmorganchase.fusion.api.stream.IntegrityCheckingInputStream;
import io.github.jpmorganchase.fusion.http.Client;
import io.github.jpmorganchase.fusion.http.HttpResponse;
import io.github.jpmorganchase.fusion.oauth.provider.FusionTokenProvider;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@ToString
public class PartFetcher {

    Client client;
    FusionTokenProvider credentials;
    DownloadRequest request;

    public GetPartResponse fetch(String partPath) {
        HttpResponse<InputStream> response = client.getInputStream(partPath, getSecurityHeaders(request));
        checkResponseStatus(response);

        Head head = Head.builder().fromHeaders(response.getHeaders()).build();
        InputStream inputStream = IntegrityCheckingInputStream.builder()
                .part(response.getBody())
                .checksum(head.getChecksum())
                .build();

        return GetPartResponse.builder().content(inputStream).head(head).build();
    }

    private Map<String, String> getSecurityHeaders(DownloadRequest dr) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + credentials.getSessionBearerToken());
        headers.put(
                "Fusion-Authorization",
                "Bearer " + credentials.getDatasetBearerToken(dr.getCatalog(), dr.getDataset()));
        return headers;
    }
}
