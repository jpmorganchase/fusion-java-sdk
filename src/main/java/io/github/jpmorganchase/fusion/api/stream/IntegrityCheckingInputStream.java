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

    boolean isEndOfStream = false;

    public static IntegrityCheckingInputStream of(List<GetPartResponse> orderedResponses)
            throws IOException, NoSuchAlgorithmException {
        return new IntegrityCheckingInputStream(new LinkedList<>(orderedResponses), DEFAULT_DIGEST_ALGO);
    }

    public static IntegrityCheckingInputStream of(List<GetPartResponse> orderedResponses, String digestAlgo)
            throws IOException, NoSuchAlgorithmException {
        return new IntegrityCheckingInputStream(new LinkedList<>(orderedResponses), digestAlgo);
    }

    private IntegrityCheckingInputStream(LinkedList<GetPartResponse> orderedResponses, String digestAlgo)
            throws IOException, NoSuchAlgorithmException {
        this.orderedResponses = orderedResponses;
        this.currentResponse = orderedResponses.poll();
        if (null == currentResponse) {
            throw new IOException();
        }
        currentDigest = MessageDigest.getInstance(digestAlgo);
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
    public int read(byte[] b) throws IOException {

        if (isEndOfStream) {
            return -1;
        }

        int bytesRead = currentResponse.getContent().read(b);
        if (-1 == bytesRead) {

            if (verifyResetAndGetNextResponse() < 0) {
                return -1;
            }
            return this.read(b);
        }

        this.currentDigest.update(b, 0, bytesRead);
        return readFromStreamsUntilByteArrayFilled(b, b.length, bytesRead);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {

        if (isEndOfStream) {
            return -1;
        }

        int bytesRead = currentResponse.getContent().read(b, off, len);
        if (-1 == bytesRead) {

            if (verifyResetAndGetNextResponse() < 0) {
                return -1;
            }
            return this.read(b, off, len);
        }

        this.currentDigest.update(b, off, bytesRead);
        return readFromStreamsUntilByteArrayFilled(b, len, bytesRead);
    }

    private int readFromStreamsUntilByteArrayFilled(byte[] b, int len, int bytesRead) throws IOException {
        while (bytesRead < len) {

            if (verifyResetAndGetNextResponse() < 0) {
                return bytesRead;
            }

            byte[] fromNextStream = new byte[len - bytesRead];
            int nextStreamBytesRead = currentResponse.getContent().read(fromNextStream);

            if (nextStreamBytesRead < 0) {
                return bytesRead;
            }

            this.currentDigest.update(fromNextStream, 0, nextStreamBytesRead);

            System.arraycopy(fromNextStream, 0, b, bytesRead, fromNextStream.length);
            bytesRead += nextStreamBytesRead;
        }
        return bytesRead;
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
        if (null == currentResponse) {
            isEndOfStream = true;
        }
        return (!isEndOfStream ? 1 : -1);
    }

    private void closeCurrentResponse() throws IOException {
        if (!isEndOfStream) {
            currentResponse.getContent().close();
        }
    }

    private void nextResponse() {
        currentResponse = orderedResponses.poll();
    }

    private void resetDigest() throws IOException {
        try {
            currentDigest = MessageDigest.getInstance(currentDigest.getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

    private void verifyDigest() throws IOException {
        String encodedDigest = Base64.getEncoder().encodeToString(currentDigest.digest());
        System.out.println("digest is : " + encodedDigest);
        if (Objects.isNull(currentResponse.getHead())
                || !currentResponse.getHead().getChecksum().equals(encodedDigest)) {
            throw new IOException("Corrupted stream, verification of checksum failed");
        }
    }
}
