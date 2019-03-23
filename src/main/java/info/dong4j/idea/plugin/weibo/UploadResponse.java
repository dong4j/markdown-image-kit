package info.dong4j.idea.plugin.weibo;

import info.dong4j.idea.plugin.weibo.entity.ImageInfo;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018 -06-14 22:31
 */
public interface UploadResponse {

    /**
     * Gets result.
     *
     * @return the result
     */
    ResultStatus getResult();

    /**
     * Sets result.
     *
     * @param rs the rs
     */
    void setResult(ResultStatus rs);

    /**
     * Gets message.
     *
     * @return the message
     */
    String getMessage();

    /**
     * Sets message.
     *
     * @param message the message
     */
    void setMessage(String message);

    /**
     * Gets image info.
     *
     * @return the image info
     */
    ImageInfo getImageInfo();

    /**
     * Sets image info.
     *
     * @param imageInfo the image info
     */
    void setImageInfo(ImageInfo imageInfo);

    /**
     * The enum Result status.
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
