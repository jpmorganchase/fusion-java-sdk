package io.github.jpmorganchase.fusion.digest.checksum;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SHAProvider implements DigestProvider {

    private final MessageDigest digest;

    public SHAProvider(String algorithm) throws NoSuchAlgorithmException {
        this.digest = MessageDigest.getInstance(algorithm);
    }

    @Override
    public void update(byte singleByte) {
        digest.update(singleByte);
    }

    @Override
    public String getDigest() {
        return Base64.getEncoder().encodeToString(digest.digest());
    }
}
