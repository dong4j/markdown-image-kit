package info.dong4j.idea.plugin.weibo.entity;

import lombok.Data;

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
@Data
public class ImageInfo {
    /** 照片id */
    private String pid;
    /** 宽度 */
    private Integer width;
    /** 长度 */
    private Integer height;
    /** 大小 */
    private Integer size;
    /** 原图url */
    private String large;
    /** 中等尺寸 */
    private String middle;
    /** 缩略图 */
    private String small;
}
