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
public class WbpUploadResponse implements UploadResponse {
    /** Result status */
    private ResultStatus resultStatus;
    /** Message */
    private String message;
    /** Image info */
    private ImageInfo imageInfo;

    /**
     * Gets result *
     *
     * @return the result
     * @since 0.0.1
     */
    @Override
    public ResultStatus getResult() {
        return this.resultStatus;
    }

    /**
     * Sets result *
     *
     * @param rs rs
     * @since 0.0.1
     */
    @Override
    public void setResult(ResultStatus rs) {
        this.resultStatus = rs;
    }

    /**
     * Gets message *
     *
     * @return the message
     * @since 0.0.1
     */
    @Override
    public String getMessage() {
        return this.message;
    }

    /**
     * Sets message *
     *
     * @param message message
     * @since 0.0.1
     */
    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets image info *
     *
     * @return the image info
     * @since 0.0.1
     */
    @Override
    public ImageInfo getImageInfo() {
        return this.imageInfo;
    }

    /**
     * Sets image info *
     *
     * @param imageInfo image info
     * @since 0.0.1
     */
    @Override
    public void setImageInfo(ImageInfo imageInfo) {
        this.imageInfo = imageInfo;
    }
}
