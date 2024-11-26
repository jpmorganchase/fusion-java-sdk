package io.github.jpmorganchase.fusion.api.stream;

import io.github.jpmorganchase.fusion.digest.PartChecker;
import lombok.Builder;
import lombok.ToString;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Builder
@ToString
public class IntegrityCheckingInputStream extends InputStream {

    InputStream part;
    String checksum;
    PartChecker partChecker;

    private IntegrityCheckingInputStream(InputStream part, String checksum, PartChecker partChecker) {
        this.part = part;
        this.checksum = checksum;
        this.partChecker = partChecker;
    }

    @Override
    public int read() throws IOException {

        if (isEndOfStream()) {
            return -1;
        }

        int byteRead = part.read();
        if (-1 == byteRead) {
            verify();
            return -1;
        }

        partChecker.update(byteRead);
        return byteRead;
    }

    private void verify() throws IOException {
        partChecker.verify(checksum);
        part = null;
    }

    @Override
    public void close() throws IOException {
        if (!isEndOfStream()) {
            part.close();
        }
    }

    private boolean isEndOfStream() {
        return Objects.isNull(part);
    }

    public static class IntegrityCheckingInputStreamBuilder {
        public IntegrityCheckingInputStream build() {
            if (this.partChecker == null) {
                this.partChecker = PartChecker.builder().build();
            }
            return new IntegrityCheckingInputStream(this.part, this.checksum, this.partChecker);
        }
    }
}
