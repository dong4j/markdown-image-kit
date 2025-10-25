package info.dong4j.idea.plugin.weibo;

import info.dong4j.idea.plugin.weibo.entity.ImageInfo;

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
public interface UploadResponse {

    /**
     * Gets result.
     *
     * @return the result
     * @since 0.0.1
     */
    ResultStatus getResult();

    /**
     * Sets result.
     *
     * @param rs the rs
     * @since 0.0.1
     */
    void setResult(ResultStatus rs);

    /**
     * Gets message.
     *
     * @return the message
     * @since 0.0.1
     */
    String getMessage();

    /**
     * Sets message.
     *
     * @param message the message
     * @since 0.0.1
     */
    void setMessage(String message);

    /**
     * Gets image info.
     *
     * @return the image info
     * @since 0.0.1
     */
    ImageInfo getImageInfo();

    /**
     * Sets image info.
     *
     * @param imageInfo the image info
     * @since 0.0.1
     */
    void setImageInfo(ImageInfo imageInfo);

    /**
     * The enum Result status.
     *
     * @author dong4j
     * @version 0.0.1
     * @email "mailto:dong4j@gmail.com"
     * @date 2021.02.14 18:40
     * @since 0.0.1
     */
    enum ResultStatus {
        /**
         * Success result status.
         */
        SUCCESS,
        /**
         * Failed result status.
         */
        FAILED
    }
}
