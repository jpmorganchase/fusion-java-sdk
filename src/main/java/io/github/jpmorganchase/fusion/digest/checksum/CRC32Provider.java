package io.github.jpmorganchase.fusion.digest.checksum;

import java.util.Base64;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class CRC32Provider implements DigestProvider {

    private final Checksum checksum;

    public CRC32Provider() {
        this.checksum = new CRC32();
    }

    @Override
    public void update(byte singleByte) {
        checksum.update(singleByte);
    }

    @Override
    public String getDigest() {
        long checksumValue = checksum.getValue();
        return Base64.getEncoder().encodeToString(longToBytes(checksumValue));
    }

    private byte[] longToBytes(long value) {
        return new byte[] {
                (byte) (value >>> 56),
                (byte) (value >>> 48),
                (byte) (value >>> 40),
                (byte) (value >>> 32),
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };
    }
}
