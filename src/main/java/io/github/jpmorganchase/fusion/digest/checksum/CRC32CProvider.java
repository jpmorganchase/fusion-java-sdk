package io.github.jpmorganchase.fusion.digest.checksum;

import java.util.Base64;
import software.amazon.awssdk.crt.checksums.CRC32C;

public class CRC32CProvider implements DigestProvider {

    private final CRC32C crc32c;

    public CRC32CProvider() {
        this.crc32c = new CRC32C();
    }

    @Override
    public void update(byte singleByte) {
        crc32c.update(singleByte);
    }

    @Override
    public String getDigest() {
        int value = crc32c.getValue();

        byte[] bytes = new byte[] {
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };

        return Base64.getEncoder().encodeToString(bytes);
    }
}
