package io.github.jpmorganchase.fusion.api.request;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Builder
public class CallableParts {

    LinkedList<CallablePart> parts;
    ExecutorService executor;

    public CallableParts(LinkedList<CallablePart> parts, ExecutorService executor) {
        this.parts = new LinkedList<>(parts);
        this.executor = executor;
    }

    public Future<InputStream> next() {
        final CallablePart callablePart = parts.poll();

        if (Objects.nonNull(callablePart)) {
            return executor.submit(callablePart);
        }
        return CompletableFuture.completedFuture(null);
    }

    public static class CallablePartsBuilder {
        public CallableParts build() throws IOException {
            if (this.executor == null) {
                this.executor = Executors.newSingleThreadExecutor();
                ;
            }
            return new CallableParts(this.parts, this.executor);
        }
    }
}
