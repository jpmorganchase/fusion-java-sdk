package io.github.jpmorganchase.fusion.digest;

import io.github.jpmorganchase.fusion.digest.checksum.DigestProvider;
import io.github.jpmorganchase.fusion.digest.checksum.DigestProviderService;
import java.io.IOException;
import java.security.MessageDigest;
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
        if (Objects.isNull(expectedChecksum) || expectedChecksum.isEmpty()) {
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
        digestProvider = digestProviderService.getDigestProvider(digestAlgo);
    }

    public static class PartCheckerBuilder {

        DigestProviderService digestProviderService = new DigestProviderService();

        public PartChecker build() {
            if (digestAlgo == null) {
                digestAlgo = DEFAULT_DIGEST_ALGO;
            }
            return new PartChecker(digestAlgo, digestProviderService);
        }
    }
}
