package info.dong4j.idea.plugin.weibo;

import java.io.*;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018 -06-14 22:31
 */
public interface UploadRequest {

    /**
     * Upload upload response.
     *
     * @param image the image
     * @return the upload response
     * @throws IOException the io exception
     */
    UploadResponse upload(File image) throws IOException;
}
