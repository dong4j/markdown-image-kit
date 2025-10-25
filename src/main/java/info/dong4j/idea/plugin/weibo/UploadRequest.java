package info.dong4j.idea.plugin.weibo;

import java.io.File;
import java.io.IOException;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2018.06.14 22:31
 * @update dong4j
 * @since 0.0.1
 */
public interface UploadRequest {

    /**
     * Upload upload response.
     *
     * @param image the image
     * @return the upload response
     * @throws IOException the io exception
     * @since 0.0.1
     */
    UploadResponse upload(File image) throws IOException;
}
