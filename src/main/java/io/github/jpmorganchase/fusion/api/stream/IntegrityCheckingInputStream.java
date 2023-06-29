package io.github.jpmorganchase.fusion.api.stream;

import io.github.jpmorganchase.fusion.api.response.GetPartResponse;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class IntegrityCheckingInputStream extends InputStream {

    private static final String DEFAULT_DIGEST_ALGO = "SHA-256";

    private final LinkedList<GetPartResponse> orderedResponses;
    GetPartResponse currentResponse;
    MessageDigest currentDigest;

    String digestAlgo;

    public static IntegrityCheckingInputStream of(List<GetPartResponse> orderedResponses) throws IOException {
        return new IntegrityCheckingInputStream(new LinkedList<>(orderedResponses), DEFAULT_DIGEST_ALGO);
    }

    public static IntegrityCheckingInputStream of(List<GetPartResponse> orderedResponses, String digestAlgo)
            throws IOException {
        return new IntegrityCheckingInputStream(new LinkedList<>(orderedResponses), digestAlgo);
    }

    private IntegrityCheckingInputStream(LinkedList<GetPartResponse> orderedResponses, String digestAlgo)
            throws IOException {
        this.orderedResponses = orderedResponses;
        this.digestAlgo = digestAlgo;
        resetDigest();
        nextResponse();
    }

    @Override
    public int read() throws IOException {

        int byteRead;
        if (isEndOfStream()) {
            byteRead = -1;
        } else {
            byteRead = currentResponse.getContent().read();
            if (byteRead > -1) {
                currentDigest.update(Integer.valueOf(byteRead).byteValue());
            } else {
                if (verifyResetAndGetNextResponse()) {
                    byteRead = this.read();
                }
            }
        }
        return byteRead;
    }

    @Override
    public void close() throws IOException {
        if (!isEndOfStream()) {
            currentResponse.getContent().close();
            while (nextResponse()) {
                currentResponse.getContent().close();
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
        currentResponse.getContent().close();
    }

    private boolean nextResponse() {
        currentResponse = orderedResponses.poll();
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
        if (Objects.isNull(currentResponse.getHead())
                || !currentResponse.getHead().getChecksum().equals(encodedDigest)) {
            throw new IOException("Corrupted stream, verification of checksum failed");
        }
    }

    private boolean isEndOfStream() {
        return Objects.isNull(currentResponse);
    }
}
