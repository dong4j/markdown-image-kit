package info.dong4j.idea.plugin.weibo;


import info.dong4j.idea.plugin.util.WeiboUploadUtils;
import info.dong4j.idea.plugin.weibo.entity.ImageInfo;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018 -06-14 22:31
 */
public class ImageInfoBuilder {
    private String[] sizeArr = new String[] {"large", "mw1024", "mw690", "bmiddle", "small", "thumb180", "thumbnail", "square"};
    private ImageInfo imageInfo;

    /**
     * Instantiates a new Image info builder.
     */
    ImageInfoBuilder() {
        imageInfo = new ImageInfo();
    }

    /**
     * Sets image info.
     *
     * @param picId  the pic id
     * @param width  the width
     * @param height the height
     * @param size   the size
     * @return the image info
     */
    ImageInfoBuilder setImageInfo(String picId, int width, int height, int size) {
        imageInfo.setPid(picId);
        imageInfo.setWidth(width);
        imageInfo.setHeight(height);
        imageInfo.setSize(size);
        return this;
    }

    /**
     * Build image info.
     *
     * @return the image info
     */
    ImageInfo build() {
        this.imageInfo.setLarge(WeiboUploadUtils.getImageUrl(imageInfo.getPid(), sizeArr[0], true));
        this.imageInfo.setMiddle(WeiboUploadUtils.getImageUrl(imageInfo.getPid(), sizeArr[1], true));
        this.imageInfo.setSmall(WeiboUploadUtils.getImageUrl(imageInfo.getPid(), sizeArr[6], true));
        return this.imageInfo;
    }
}
