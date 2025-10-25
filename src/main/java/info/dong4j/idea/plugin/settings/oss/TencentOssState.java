package info.dong4j.idea.plugin.settings.oss;

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
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class TencentOssState extends OssState {
    /** Access key */
    private String accessKey = "";
    /** Secret key */
    private String secretKey = "";
    /** Bucket name */
    private String bucketName = "";
    /** Region name */
    private String regionName = "";
}
