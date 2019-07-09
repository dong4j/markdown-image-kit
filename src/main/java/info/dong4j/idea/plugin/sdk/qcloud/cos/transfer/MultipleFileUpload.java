package info.dong4j.idea.plugin.sdk.qcloud.cos.transfer;

import java.util.Collection;

/**
 * Multiple file download of an entire virtual directory.
 */
public interface  MultipleFileUpload extends Transfer {

    /**
     * Returns the key prefix of the virtual directory being uploaded.
     */
    String getKeyPrefix();

    /**
     * Returns the name of the bucket to which files are uploaded.
     */
    String getBucketName();

    /**
     * Returns a collection of sub transfers associated with the multi file upload.
     */
    Collection<? extends Upload> getSubTransfers();

}