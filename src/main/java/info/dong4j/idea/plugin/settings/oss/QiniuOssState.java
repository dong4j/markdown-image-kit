package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.enums.ZoneEnum;
import info.dong4j.idea.plugin.settings.OssState;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.19 19:55
 * @since 0.0.1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class QiniuOssState extends OssState {
    /** Endpoint */
    private String endpoint = "";
    /** Access key */
    private String accessKey = "";
    /** Access secret key */
    private String accessSecretKey = "";
    /** Bucket name */
    private String bucketName = "";
    /** Zone index */
    private int zoneIndex = ZoneEnum.EAST_CHINA.index;
}
