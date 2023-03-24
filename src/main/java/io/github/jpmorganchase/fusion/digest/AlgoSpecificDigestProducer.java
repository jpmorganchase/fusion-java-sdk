package io.github.jpmorganchase.fusion.digest;

import io.github.jpmorganchase.fusion.api.ApiInputValidationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import lombok.Builder;
import lombok.SneakyThrows;

/**
 * Produces a digest for the provided {@code InputStream} returning a {@code DigestDescriptor}.
 *
 * <p>If no {@code digestAlgorithm} is specified, defaults to "SHA-256".</p>
 *
 */
@Builder
public class AlgoSpecificDigestProducer implements DigestProducer {

    private static final String SHA_256_ALGO = "SHA-256";

    private final String digestAlgorithm;

    @SneakyThrows(NoSuchAlgorithmException.class)
    @Override
    public DigestDescriptor execute(InputStream data) {

        assertInputStream(data);

        DigestInputStream dis = new DigestInputStream(data, MessageDigest.getInstance(digestAlgorithm));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buf = new byte[8192];
        int length;
        while (true) {
            try {
                if ((length = dis.read(buf)) == -1) break;
            } catch (IOException e) {
                throw new ApiInputValidationException("Failed to read data from input", e);
            }
            baos.write(buf, 0, length);
        }

        String myChecksum =
                Base64.getEncoder().encodeToString(dis.getMessageDigest().digest());

        return DigestDescriptor.builder()
                .checksum(myChecksum)
                .size(baos.size())
                .content(baos.toByteArray())
                .build();
    }

    private void assertInputStream(InputStream data) {
        if (Objects.isNull(data)) {
            throw new ApiInputValidationException("Failed to read data from input");
        }
    }

    public static class AlgoSpecificDigestProducerBuilder {

        protected String digestAlgorithm = SHA_256_ALGO;

        public AlgoSpecificDigestProducerBuilder sha256() {
            this.digestAlgorithm = SHA_256_ALGO;
            return this;
        }
    }
}
