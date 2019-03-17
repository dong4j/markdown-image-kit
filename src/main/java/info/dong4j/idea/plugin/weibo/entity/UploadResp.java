package info.dong4j.idea.plugin.weibo.entity;

import lombok.Data;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018-06-14 22:31
 */
@Data
public class UploadResp {
    private String code;
    private info.dong4j.idea.plugin.weibo.entity.upload.Data data;
}
