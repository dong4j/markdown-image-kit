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
public class UploadResp {
    /** Code */
    private String code;
    /** Data */
    private info.dong4j.idea.plugin.weibo.entity.upload.Data data;
}
