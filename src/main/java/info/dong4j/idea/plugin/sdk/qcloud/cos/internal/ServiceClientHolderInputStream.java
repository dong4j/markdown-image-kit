package info.dong4j.idea.plugin.sdk.qcloud.cos.internal;

import info.dong4j.idea.plugin.sdk.qcloud.cos.COSClient;

import java.io.*;

/**
 * This wrapper input stream holds a reference to the service client. This is
 * mainly done to avoid the COSClient object being garbage
 * collected when the client reads data from the input stream.
 *
 */
public class ServiceClientHolderInputStream extends SdkFilterInputStream {


    @SuppressWarnings("unused")
    private COSClient client;

    public ServiceClientHolderInputStream(InputStream in,
            COSClient client) {
        super(in);
        this.client = client;
    }
}
