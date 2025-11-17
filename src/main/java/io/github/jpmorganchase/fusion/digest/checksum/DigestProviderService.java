package io.github.jpmorganchase.fusion.digest.checksum;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class DigestProviderService {

    public DigestProvider getDigestProvider(String digestAlgo) throws IOException {
        try {
            switch (digestAlgo) {
                case "CRC32":
                    return new CRC32Provider();
                case "CRC32C":
                    return new CRC32CProvider();
                case "CRC64NVME":
                    return new CRC64NVMEProvider();
                case "SHA-1":
                case "SHA-256":
                case "MD5":
                    return new SHAProvider(digestAlgo);
                default:
                    throw new IOException("Inavlid digest algorithm provided");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Invalid digest algorithm provided", e);
        }
    }
}
