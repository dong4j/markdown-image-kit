package info.dong4j.idea.plugin.sdk.qcloud.cos.model;

import info.dong4j.idea.plugin.sdk.qcloud.cos.internal.CosServiceRequest;

import java.io.*;

public class DeleteBucketRequest extends CosServiceRequest implements Serializable {
    /**
     * The name of the Qcloud COS bucket to delete.
     */
    private String bucketName;

    /**
     * Constructs a new {@link DeleteBucketRequest}, ready to be executed to delete the specified
     * bucket.
     *
     * @param bucketName The name of the Qcloud COS bucket to delete.
     */
    public DeleteBucketRequest(String bucketName) {
        setBucketName(bucketName);
    }

    /**
     * Sets the name of the Qcloud COS bucket to delete.
     *
     * @param bucketName The name of the Qcloud COS bucket to delete.
     *
     * @see DeleteBucketRequest#getBucketName()
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Gets the name of the Qcloud COS bucket to delete.
     *
     * @return The name of the Qcloud COS bucket to delete.
     *
     * @see DeleteBucketRequest#setBucketName(String)
     */
    public String getBucketName() {
        return bucketName;
    }
}
