package io.github.jpmorganchase.fusion.api.stream;

import io.github.jpmorganchase.fusion.api.request.CallableParts;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.Builder;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
public class DeferredMultiPartInputStream extends InputStream {

    CallableParts parts;
    Future<InputStream> nextPart;
    InputStream currentPart;

    @Builder
    public DeferredMultiPartInputStream(CallableParts parts) throws IOException {
        this.parts = parts;
        init();
    }

    @Override
    public int read() throws IOException {

        if (Objects.isNull(currentPart)) {
            return -1;
        }

        int byteRead = currentPart.read();
        if (-1 == byteRead) {
            if (nextPart()) {
                return this.read();
            }
            return -1;
        }

        return byteRead;
    }

    private void init() throws IOException {
        primeNextPart();
        nextPart();
    }

    private boolean nextPart() throws IOException {
        try {
            if (reassignCurrentPart()) {
                primeNextPart();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Unable to read from stream", e.getCause());
        }
        return isStreamAvailable();
    }

    private boolean reassignCurrentPart() throws IOException, InterruptedException, ExecutionException {
        close();
        currentPart = nextPart.get();
        return isStreamAvailable();
    }

    private void primeNextPart() {
        log.debug("Priming next part ready for reading");
        this.nextPart = callForNextPart();
    }

    private Future<InputStream> callForNextPart() {
        return parts.next();
    }

    private boolean isStreamAvailable() {
        return Objects.nonNull(currentPart);
    }

    @Override
    public void close() throws IOException {
        if (isStreamAvailable()) {
            currentPart.close();
        }
    }
}
