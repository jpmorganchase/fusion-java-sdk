package io.github.jpmorganchase.fusion.digest;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DigestDescriptor {

    private final String checksum;
    private final int size;
    private final byte[] content;
}
