package info.dong4j.idea.plugin.weibo;

import info.dong4j.idea.plugin.weibo.entity.ImageInfo;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018-06-14 22:31
 */
public class WbpUploadResponse implements UploadResponse {
    private ResultStatus resultStatus;
    private String message;
    private ImageInfo imageInfo;

    @Override
    public ResultStatus getResult() {
        return this.resultStatus;
    }

    @Override
    public void setResult(ResultStatus rs) {
        this.resultStatus = rs;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public ImageInfo getImageInfo() {
        return this.imageInfo;
    }

    @Override
    public void setImageInfo(ImageInfo imageInfo) {
        this.imageInfo = imageInfo;
    }
}
