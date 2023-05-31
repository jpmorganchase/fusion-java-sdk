package io.github.jpmorganchase.fusion.digest;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public interface DigestProducer {

    DigestDescriptor execute(InputStream data);

    DigestDescriptor execute(List<ByteBuffer> digests);
}
