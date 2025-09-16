package io.github.jpmorganchase.fusion.digest;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@ToString
public class PartChecker {

    public static final String DEFAULT_DIGEST_ALGO = "SHA-256";

    MessageDigest digest;

    String digestAlgo;

    public PartChecker(String digestAlgo) {
        this.digestAlgo = digestAlgo;
    }

    public void update(int bytesRead) throws IOException {
        if (Objects.isNull(digest)) {
            init();
        }
        digest.update(Integer.valueOf(bytesRead).byteValue());
    }

    public void verify(String checksum) throws IOException {
        String encodedDigest = Base64.getEncoder().encodeToString(digest.digest());
        if (!Objects.isNull(checksum) && !checksum.equals(encodedDigest)) {
            log.error(
                    "Corrupted stream encountered, failed to verify checksum [{}] against calculated checksum [{}]",
                    checksum,
                    encodedDigest);
            throw new IOException("Corrupted stream, verification of checksum failed");
        }
    }

    private void init() throws IOException {
        try {
            digest = MessageDigest.getInstance(digestAlgo);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Invalid digest algorithm provided", e);
        }
    }

    public static class PartCheckerBuilder {
        public PartChecker build() {
            if (digestAlgo == null) {
                digestAlgo = DEFAULT_DIGEST_ALGO;
            }
            return new PartChecker(digestAlgo);
        }
    }
}
