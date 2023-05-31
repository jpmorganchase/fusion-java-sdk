package io.github.jpmorganchase.fusion.digest;

import java.io.InputStream;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DigestDescriptor {

    private final byte[] rawChecksum;
    private final String checksum;
    private final int size;
    private final byte[] content;
    private final InputStream data;
}
