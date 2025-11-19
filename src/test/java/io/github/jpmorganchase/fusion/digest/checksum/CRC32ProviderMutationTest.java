package io.github.jpmorganchase.fusion.digest.checksum;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import org.junit.jupiter.api.Test;

class CRC32ProviderMutationTest {

    @Test
    void testUpdateAndGetDigestMatchesReferenceImplementation() {
        // Use a standard CRC32 test string with non-trivial high bits
        byte[] data = "123456789".getBytes(StandardCharsets.UTF_8);

        // Your provider
        CRC32Provider provider = new CRC32Provider();
        for (byte b : data) {
            provider.update(b);
        }
        String actualDigest = provider.getDigest();

        // Reference implementation using CRC32 directly
        Checksum checksum = new CRC32();
        for (byte b : data) {
            checksum.update(b);
        }
        long value = checksum.getValue();

        // Expected bytes using big-endian layout (same logic as your longToBytes)
        byte[] expectedBytes =
                new byte[] {(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value};
        String expectedDigest = Base64.getEncoder().encodeToString(expectedBytes);

        // Then
        assertEquals(expectedDigest, actualDigest);
    }
}
