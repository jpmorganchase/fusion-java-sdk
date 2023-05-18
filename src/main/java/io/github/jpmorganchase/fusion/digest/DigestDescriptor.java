package io.github.jpmorganchase.fusion.digest;

import lombok.Builder;
import lombok.Value;

import java.util.Base64;

@Value
@Builder
public class DigestDescriptor {

    private final byte[] rawChecksum;
    private final String checksum;
    private final int size;
    private final byte[] content;

}
