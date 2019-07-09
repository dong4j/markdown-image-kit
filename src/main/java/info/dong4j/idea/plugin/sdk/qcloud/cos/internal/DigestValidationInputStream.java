package info.dong4j.idea.plugin.sdk.qcloud.cos.internal;

import info.dong4j.idea.plugin.sdk.qcloud.cos.exception.CosClientException;

import java.io.*;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Input stream extends SdkDigestInputStream, when you finish reading the stream, it
 * will validate whether the computed digest equals the one from the server
 * side.
 */
public class DigestValidationInputStream extends SdkDigestInputStream {

    private byte[] expectedHash;

    //Flag do we don't validate twice.  See validateDigest(()
    private boolean digestValidated = false;

    public DigestValidationInputStream(InputStream in, MessageDigest digest, byte[] serverSideHash) {
        super(in, digest);
        this.expectedHash = serverSideHash;
    }

    /**
     * @see InputStream#read()
     */
    @Override
    public int read() throws IOException {
        int ch = super.read();
        if (ch == -1) {
            validateDigest();
        }
        return ch;
    }

    /**
     * @see InputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        if (result == -1) {
            validateDigest();
        }
        return result;
    }

    public byte[] getDigestChecksum() {
        return digest.digest();
    }

    private void validateDigest() {
        /*
         * Some InputStream readers (e.g., java.util.Properties) read more than
         * once at the end of the stream. This class validates the digest once
         * -1 has been read so we must not validate twice.
         */
        if (expectedHash != null && !digestValidated ) {
            digestValidated = true;
            if (!Arrays.equals(digest.digest(), expectedHash)) {
                throw new CosClientException("Unable to verify integrity of data download.  "
                        + "Client calculated content hash didn't match hash calculated by Qcloud COS.  "
                        + "The data may be corrupt.");
            }
        }
    }

}
