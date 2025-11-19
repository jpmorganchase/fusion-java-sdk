package io.github.jpmorganchase.fusion.digest.checksum;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.Checksum;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.crt.checksums.CRC64NVME;

class CRC64NVMEProviderTest {

    @Test
    void testUpdateAndGetDigestMatchesRawCrtImplementation() {
        // Given
        byte[] data = "foobar".getBytes(StandardCharsets.UTF_8);

        CRC64NVMEProvider provider = new CRC64NVMEProvider();
        for (byte b : data) {
            provider.update(b);
        }
        String actualDigest = provider.getDigest();

        // Compute expected digest using the raw AWS CRT checksum + our own longToBytes logic
        Checksum checksum = new CRC64NVME();
        for (byte b : data) {
            checksum.update(b);
        }
        long value = checksum.getValue();

        byte[] expectedBytes = new byte[] {
            (byte) (value >>> 56),
            (byte) (value >>> 48),
            (byte) (value >>> 40),
            (byte) (value >>> 32),
            (byte) (value >>> 24),
            (byte) (value >>> 16),
            (byte) (value >>> 8),
            (byte) value
        };
        String expectedDigest = Base64.getEncoder().encodeToString(expectedBytes);

        // Then
        assertEquals(expectedDigest, actualDigest);
    }
}
