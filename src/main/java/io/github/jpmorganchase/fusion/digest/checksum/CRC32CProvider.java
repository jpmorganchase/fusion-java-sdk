package io.github.jpmorganchase.fusion.digest.checksum;

import java.util.Base64;
import java.util.zip.Checksum;
import java.util.zip.checksum;
import software.amazon.awssdk.crt.checksums.CRC32C;


public class CRC32CProvider implements DigestProvider {

    private final Checksum checksum;

    public CRC32CProvider()
    {
        this.checksum = newCRC32C();
    }

    @Override
    public String getDigest(){
        long checksumValue = checksum.getValue();
        return Base64.getEncoder().encodeToString(longToBytes(checksumValue));
    }

    private byte[] longToBytes(long value)
    {
        return new byte[] {(byte) (value >>> 24), (byte) (value>>> 16), (byte) (value >>> 8), (byte) value};
    }
}
