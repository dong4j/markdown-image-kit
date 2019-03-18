package info.dong4j.idea.plugin.weibo;

import info.dong4j.idea.plugin.weibo.entity.ImageInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018 -06-14 22:31
 */
class ImageInfoBuilder {
    private static final String PID_PATTERN = "^[a-zA-Z0-9]{32}$";
    private static final String URL_PATTERN = "^(https?://[a-z]{2}d.sinaimg.cn/)(large|bmiddle|mw1024|mw690|small|square|thumb180|thumbnail)(/[a-z0-9]{32}.(jpg|gif))$";
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
        this.imageInfo.setLarge(getImageUrl(imageInfo.getPid(), sizeArr[0], true));
        this.imageInfo.setMiddle(getImageUrl(imageInfo.getPid(), sizeArr[1], true));
        this.imageInfo.setSmall(getImageUrl(imageInfo.getPid(), sizeArr[6], true));
        return this.imageInfo;
    }

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
