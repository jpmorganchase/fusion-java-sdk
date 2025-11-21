package io.github.jpmorganchase.fusion.digest.checksum;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class DigestProviderIntegrationTest {

    private final DigestProviderService service = new DigestProviderService();

    @Test
    void testSha256DigestForFoobar() throws IOException {
        DigestProvider provider = service.getDigestProvider("SHA-256");
        byte[] data = "foobar".getBytes(StandardCharsets.UTF_8);
        for (byte b : data) {
            provider.update(b);
        }
        String digest = provider.getDigest();


        assertThat(digest, equalTo("w6uP8Tcg6K2QR905Rms8iXTlksL6OD1KOWBxTK7wxPI="));
    }

    @Test
    void testSha1DigestForFoobar() throws IOException {
        DigestProvider provider = service.getDigestProvider("SHA-1");
        byte[] data = "foobar".getBytes(StandardCharsets.UTF_8);
        for (byte b : data) {
            provider.update(b);
        }
        String digest = provider.getDigest();


        assertThat(digest, equalTo("iEPX+SQWIR3p67lj/0zigSWTKHg="));
    }

    @Test
    void testCrc32DigestForFoobar() throws IOException {
        DigestProvider provider = service.getDigestProvider("CRC32");
        byte[] data = "foobar".getBytes(StandardCharsets.UTF_8);
        for (byte b : data) {
            provider.update(b);
        }
        String digest = provider.getDigest();


        assertThat(digest, notNullValue());
        org.junit.jupiter.api.Assertions.assertEquals(8, digest.length());
    }

    @Test
    void testCrc32CDigestForFoobar() throws IOException {
        DigestProvider provider = service.getDigestProvider("CRC32C");
        byte[] data = "foobar".getBytes(StandardCharsets.UTF_8);
        for (byte b : data) {
            provider.update(b);
        }
        String digest = provider.getDigest();


        assertThat(digest, notNullValue());
        org.junit.jupiter.api.Assertions.assertEquals(8, digest.length());
    }

    @Test
    void testCrc64NvmeDigestForFoobar() throws IOException {
        DigestProvider provider = service.getDigestProvider("CRC64NVME");
        byte[] data = "foobar".getBytes(StandardCharsets.UTF_8);
        for (byte b : data) {
            provider.update(b);
        }
        String digest = provider.getDigest();


        assertThat(digest, notNullValue());
        org.junit.jupiter.api.Assertions.assertEquals(12, digest.length());
    }
}
