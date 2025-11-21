package io.github.jpmorganchase.fusion.digest.checksum;

import java.io.IOException;

public interface DigestProvider {

    void update(byte singleByte);

    String getDigest() throws IOException;
}
