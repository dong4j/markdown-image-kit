package info.dong4j.idea.plugin.weibo;

import info.dong4j.idea.plugin.weibo.entity.ImageInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

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
class ImageInfoBuilder {
    /** PID_PATTERN */
    private static final String PID_PATTERN = "^[a-zA-Z0-9]{32}$";
    /** URL_PATTERN */
    private static final String URL_PATTERN = "^(https?://[a-z]{2}d.sinaimg.cn/)" +
                                              "(large|bmiddle|mw1024|mw690|small|square|thumb180|thumbnail)(/[a-z0-9]{32}.(jpg|gif))$";
    /** Size arr */
    private final String[] sizeArr = new String[] {"large", "mw1024", "mw690", "bmiddle", "small", "thumb180", "thumbnail", "square"};
    /** Image info */
    private final ImageInfo imageInfo;

    /**
     * Instantiates a new Image info builder.
     *
     * @since 0.0.1
     */
    ImageInfoBuilder() {
        this.imageInfo = new ImageInfo();
    }

    /**
     * Sets image info.
     *
     * @param picId  the pic id
     * @param width  the width
     * @param height the height
     * @param size   the size
     * @return the image info
     * @since 0.0.1
     */
    ImageInfoBuilder setImageInfo(String picId, int width, int height, int size) {
        this.imageInfo.setPid(picId);
        this.imageInfo.setWidth(width);
        this.imageInfo.setHeight(height);
        this.imageInfo.setSize(size);
        return this;
    }

    /**
     * Build image info.
     *
     * @return the image info
     * @since 0.0.1
     */
    ImageInfo build() {
        this.imageInfo.setLarge(this.getImageUrl(this.imageInfo.getPid(), this.sizeArr[0], true));
        this.imageInfo.setMiddle(this.getImageUrl(this.imageInfo.getPid(), this.sizeArr[1], true));
        this.imageInfo.setSmall(this.getImageUrl(this.imageInfo.getPid(), this.sizeArr[6], true));
        return this.imageInfo;
    }

    /**
     * Gets image url *
     *
     * @param pid   pid
     * @param size  size
     * @param https https
     * @return the image url
     * @since 0.0.1
     */
    private String getImageUrl(String pid, String size, boolean https) {
        pid = pid.trim();
        Pattern p = Pattern.compile(PID_PATTERN);
        Matcher m = p.matcher(pid);

        if (m.matches()) {
            CRC32 crc32 = new CRC32();
            crc32.update(pid.getBytes());
            return (https ? "https" : "http") + "://" + (https ? "ws" : "ww")
                   + ((crc32.getValue() & 3) + 1) + ".sinaimg.cn/" + size
                   + "/" + pid + "." + (pid.charAt(21) == 'g' ? "gif" : "jpg");
        }
        String url = pid;
        Pattern p1 = Pattern.compile(URL_PATTERN);
        Matcher m1 = p1.matcher(url);
        if (m1.find()) {
            return m.group(1) + size + m.group(3);
        }
        return "";
    }
}
