package io.github.jpmorganchase.fusion.digest.checksum;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class DigestProviderServiceTest {

    private final DigestProviderService service = new DigestProviderService();

    @Test
    void testSha256ReturnsShaProvider() throws IOException {
        DigestProvider provider = service.getDigestProvider("SHA-256");
        assertThat(provider, instanceOf(SHAProvider.class));
    }

    @Test
    void testSha1ReturnsShaProvider() throws IOException {
        DigestProvider provider = service.getDigestProvider("SHA-1");
        assertThat(provider, instanceOf(SHAProvider.class));
    }

    @Test
    void testCrc32ReturnsCrc32Provider() throws IOException {
        DigestProvider provider = service.getDigestProvider("CRC32");
        assertThat(provider, instanceOf(CRC32Provider.class));
    }

    @Test
    void testCrc32CReturnsCrc32CProvider() throws IOException {
        DigestProvider provider = service.getDigestProvider("CRC32C");
        assertThat(provider, instanceOf(CRC32CProvider.class));
    }

    @Test
    void testCrc64NvmeReturnsCrc64NvmeProvider() throws IOException {
        DigestProvider provider = service.getDigestProvider("CRC64NVME");
        assertThat(provider, instanceOf(CRC64NVMEProvider.class));
    }

    @Test
    void testInvalidAlgorithmThrowsIOException() {
        assertThrows(IOException.class, () -> service.getDigestProvider("FOO-BAR"));
    }
}
