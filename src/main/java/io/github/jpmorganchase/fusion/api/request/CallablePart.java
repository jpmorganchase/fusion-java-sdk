package io.github.jpmorganchase.fusion.api.request;

import lombok.Builder;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.concurrent.Callable;

@Slf4j
@Builder
@ToString
public class CallablePart implements Callable<InputStream> {

    int partNo;
    DownloadRequest downloadRequest;
    PartFetcher partFetcher;

    @Override
    public InputStream call() {
        log.info("Preparing to make a call to download part {} for download request {}", partNo, downloadRequest);
        return partFetcher
                .fetch(PartRequest.builder()
                        .partNo(partNo)
                        .downloadRequest(downloadRequest)
                        .build())
                .getContent();
    }
}
