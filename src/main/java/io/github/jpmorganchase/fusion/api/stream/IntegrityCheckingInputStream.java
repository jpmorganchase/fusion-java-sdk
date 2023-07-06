package io.github.jpmorganchase.fusion.api.stream;

import io.github.jpmorganchase.fusion.api.response.GetPartResponse;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import lombok.Builder;

@Builder
public class IntegrityCheckingInputStream extends InputStream {

    private static final String DEFAULT_DIGEST_ALGO = "SHA-256";

    private final LinkedList<GetPartResponse> orderedParts;
    GetPartResponse currentPart;
    MessageDigest currentDigest;
    String digestAlgo;

    private IntegrityCheckingInputStream(LinkedList<GetPartResponse> orderedParts, String digestAlgo)
            throws IOException {
        this.orderedParts = orderedParts;
        this.digestAlgo = digestAlgo;
        resetDigest();
        nextResponse();
    }

    @Override
    public int read() throws IOException {

        if (isEndOfStream()) {
            return -1;
        }

        int byteRead = currentPart.getContent().read();

        if (-1 == byteRead) {
            if (verifyResetAndGetNextResponse()) {
                return this.read();
            }
            return -1;
        }

        currentDigest.update(Integer.valueOf(byteRead).byteValue());
        return byteRead;
    }

    @Override
    public void close() throws IOException {
        if (!isEndOfStream()) {
            currentPart.getContent().close();
            while (nextResponse()) {
                currentPart.getContent().close();
            }
        }
    }

    private boolean verifyResetAndGetNextResponse() throws IOException {
        verifyDigest();
        resetDigest();
        closeCurrentResponse();
        return nextResponse();
    }

    private void closeCurrentResponse() throws IOException {
        currentPart.getContent().close();
    }

    private boolean nextResponse() {
        currentPart = orderedParts.poll();
        return !isEndOfStream();
    }

    private void resetDigest() throws IOException {
        try {
            currentDigest = MessageDigest.getInstance(digestAlgo);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Unable to read from stream, invalid digest algorithm provided", e);
        }
    }

    private void verifyDigest() throws IOException {
        String encodedDigest = Base64.getEncoder().encodeToString(currentDigest.digest());
        if (Objects.isNull(currentPart.getHead())
                || !currentPart.getHead().getChecksum().equals(encodedDigest)) {
            throw new IOException("Corrupted stream, verification of checksum failed");
        }
    }

    private boolean isEndOfStream() {
        return Objects.isNull(currentPart);
    }

    /**
     * Builder to be used to construct the {@link IntegrityCheckingInputStream}
     *
     * if digest algorithm is not specified, it will default to "SHA-256".
     * <p>
     * If both a list of parts and a single part is provided, the ordered parts will first
     * of all be constructed with the list and then the single part will be added to tail.
     *
     */
    public static class IntegrityCheckingInputStreamBuilder {

        private GetPartResponse part;
        private List<GetPartResponse> parts;

        public IntegrityCheckingInputStreamBuilder part(GetPartResponse part) {
            this.part = part;
            return this;
        }

        public IntegrityCheckingInputStreamBuilder parts(List<GetPartResponse> parts) {
            this.parts = parts;
            return this;
        }

        public IntegrityCheckingInputStreamBuilder algorithm(String algorithm) {
            return this;
        }

        private IntegrityCheckingInputStreamBuilder orderedParts(LinkedList<GetPartResponse> orderedParts) {
            return this;
        }

        private IntegrityCheckingInputStreamBuilder currentPart(GetPartResponse currentPart) {
            return this;
        }

        public IntegrityCheckingInputStream build() throws IOException {

            LinkedList<GetPartResponse> orderedParts = new LinkedList<>();

            if (Objects.isNull(digestAlgo)) {
                this.digestAlgo = DEFAULT_DIGEST_ALGO;
            }

            if (Objects.nonNull(parts)) {
                orderedParts.addAll(parts);
            }

            if (Objects.nonNull(part)) {
                orderedParts.add(part);
            }

            return new IntegrityCheckingInputStream(orderedParts, digestAlgo);
        }
    }
}
