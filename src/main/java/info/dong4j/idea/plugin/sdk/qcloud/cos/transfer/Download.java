package info.dong4j.idea.plugin.sdk.qcloud.cos.transfer;

import info.dong4j.idea.plugin.sdk.qcloud.cos.exception.PauseException;
import info.dong4j.idea.plugin.sdk.qcloud.cos.model.ObjectMetadata;

import java.io.*;

/**
 * Represents an asynchronous download from Qcloud COS.
 * <p>
 * See {@link TransferManager} for more information about creating transfers.
 * </p>
 *
 * @see TransferManager#download(info.dong4j.idea.plugin.sdk.qcloud.cos.model.GetObjectRequest,
 *      java.io.File)
 */
public interface Download extends Transfer {

    /**
     * Returns the ObjectMetadata for the object being downloaded.
     *
     * @return The ObjectMetadata for the object being downloaded.
     */
    ObjectMetadata getObjectMetadata();

    /**
     * The name of the bucket where the object is being downloaded from.
     *
     * @return The name of the bucket where the object is being downloaded from.
     */
    String getBucketName();

    /**
     * The key under which this object was stored in Qcloud COS.
     *
     * @return The key under which this object was stored in Qcloud COS.
     */
    String getKey();

    /**
     * Cancels this download.
     *
     * @throws IOException
     */
    void abort() throws IOException;

    /**
     * Pause the current download operation and returns the information that can
     * be used to resume the download at a later time.
     *
     * Resuming a download would not perform ETag check as range get is
     * performed for downloading the object's remaining contents.
     *
     * Resuming a download for an object encrypted using
     * {@link CryptoMode#StrictAuthenticatedEncryption} would result in
     * CosClientException as authenticity cannot be guaranteed for a range
     * get operation.
     *
     * @throws PauseException
     *             If any errors were encountered while trying to pause the
     *             download.
     */
    PersistableDownload pause() throws PauseException;
}