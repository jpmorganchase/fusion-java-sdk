package io.github.jpmorganchase.fusion.digest.checksum;

import java.util.Base64;
import java.util.zip.CRC32;

public class CRC32Provider implements DigestProvider {

    private final CRC32 crc32;

    public CRC32Provider() {
        this.crc32 = new CRC32();
    }

    @Override
    public void update(byte singleByte) {
        crc32.update(singleByte);
    }

    @Override
    public String getDigest() {
        long value = crc32.getValue();

        byte[] bytes = new byte[] {
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };

        return Base64.getEncoder().encodeToString(bytes);
    }
}
