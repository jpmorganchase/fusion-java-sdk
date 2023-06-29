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

    boolean isEndOfStream = false;

    public static IntegrityCheckingInputStream of(List<GetPartResponse> orderedResponses)
            throws IOException {
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
        nextResponse();
        resetDigest();
    }

    @Override
    public int read() throws IOException {

        if (isEndOfStream) {
            return -1;
        }

        int byteRead = currentResponse.getContent().read();
        if (-1 == byteRead) {

            if (verifyResetAndGetNextResponse() < 0) {
                return -1;
            }

            return this.read();
        }

        currentDigest.update(Integer.valueOf(byteRead).byteValue());
        return byteRead;
    }

    @Override
    public void close() throws IOException {
        currentResponse.getContent().close();
        nextResponse();
        while (null != currentResponse) {
            currentResponse.getContent().close();
            nextResponse();
        }
    }

    private int verifyResetAndGetNextResponse() throws IOException {
        verifyDigest();
        resetDigest();
        closeCurrentResponse();
        nextResponse();
        return (isEndOfStream ? -1 : 1);
    }

    private void closeCurrentResponse() throws IOException {
        if (!isEndOfStream) {
            currentResponse.getContent().close();
        }
    }

    private void nextResponse() {
        currentResponse = orderedResponses.poll();
        if (null == currentResponse) {
            isEndOfStream = true;
        }
    }

    private void resetDigest() throws IOException {
        try {
            currentDigest = MessageDigest.getInstance(digestAlgo);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Unable to read from stream, invalid digest algorithm provided",e);
        }
    }

    private void verifyDigest() throws IOException {
        String encodedDigest = Base64.getEncoder().encodeToString(currentDigest.digest());
        if (Objects.isNull(currentResponse.getHead())
                || !currentResponse.getHead().getChecksum().equals(encodedDigest)) {
            throw new IOException("Corrupted stream, verification of checksum failed");
        }
    }
}
