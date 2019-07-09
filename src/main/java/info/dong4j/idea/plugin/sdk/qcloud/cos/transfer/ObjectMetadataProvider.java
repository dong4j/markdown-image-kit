package info.dong4j.idea.plugin.sdk.qcloud.cos.transfer;

import info.dong4j.idea.plugin.sdk.qcloud.cos.model.ObjectMetadata;

import java.io.*;

/**
 * This is the callback interface which is used by TransferManager.uploadDirectory and
 * TransferManager.uploadFileList. The callback is invoked for each file that is uploaded by
 * <code>TransferManager</code> and given an opportunity to specify the metadata for each file.
 */
public interface ObjectMetadataProvider {

    /*
     * This method is called for every file that is uploaded by <code>TransferManager</code>
     * and gives an opportunity to specify the metadata for the file.
     *
     * @param file
     *          The file being uploaded.
     *
     * @param metadata
     *          The default metadata for the file. You can modify this object to specify
     * your own metadata.
     */
    void provideObjectMetadata(final File file, final ObjectMetadata metadata);

}