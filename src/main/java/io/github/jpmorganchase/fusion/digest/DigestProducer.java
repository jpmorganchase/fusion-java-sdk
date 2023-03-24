package io.github.jpmorganchase.fusion.digest;

import java.io.InputStream;

public interface DigestProducer {

    DigestDescriptor execute(InputStream data);
}
