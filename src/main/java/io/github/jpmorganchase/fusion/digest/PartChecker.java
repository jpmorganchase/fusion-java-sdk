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

    private DigestProvider digestProvider;
    private String digestAlgo;
    private boolean skipChecksum;
    private DigestProviderService digestProviderService;



    public PartChecker(String digestAlgo, DigestProviderService digestProviderService) {
        this.digestAlgo = digestAlgo;
        this.digestProviderService = digestProviderService;
    }

    public void update(int bytesRead) throws IOException {
        if (Objects.isNull(digestProvider)) {
            init();
        }
        digestProvider.update(Integer.valueOf(bytesRead).byteValue());
    }

    public void verify(String expectedChecksum) throws IOException {
        String encodedDigest = Base64.getEncoder().encodeToString(digest.digest());
        if (skipChecksum) {
            return;
        }

        String calculatedChecksum = digestProvider.getDigest();


        if (!Objects.equals(expectedChecksum, calculatedChecksum)) {
            log.error(
                    "Corrupted stream encountered, failed to verify checksum [{}] against calculated checksum [{}]",
                    expectedChecksum,
                    calculatedChecksum);
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

        DigestProviderService digestProviderService = new DigestProviderService();
        public PartChecker build() {
            if (digestAlgo == null) {
                digestAlgo = DEFAULT_DIGEST_ALGO;
            }
            return new PartChecker(digestAlgo, skipChecksum, digestProviderService);
        }
    }
}
