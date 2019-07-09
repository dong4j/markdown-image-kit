package info.dong4j.idea.plugin.sdk.qcloud.cos.transfer;

import java.io.*;

/**
 * Multiple file download of an entire virtual directory.
 */
public interface  MultipleFileDownload extends Transfer {

    /**
     * Returns the key prefix of the virtual directory being downloaded.
     */
    String getKeyPrefix();

    /**
     * Returns the name of the bucket from which files are downloaded.
     */
    String getBucketName();

    /**
     * Cancels this download.
     *
     * @throws IOException
     */
    void abort() throws IOException;
}
