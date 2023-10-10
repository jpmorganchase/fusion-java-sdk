package io.github.jpmorganchase.fusion.api.request;

import java.io.InputStream;
import java.util.concurrent.Callable;
import lombok.Builder;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@ToString
public class CallablePart implements Callable<InputStream> {

    String path;
    PartFetcher partFetcher;

    @Override
    public InputStream call() {
        log.info("Preparing to make a call to download part from path {}", path);
        return partFetcher.fetch(path).getContent();
    }
}
