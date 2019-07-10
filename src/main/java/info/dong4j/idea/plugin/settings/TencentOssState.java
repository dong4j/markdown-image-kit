package info.dong4j.idea.plugin.settings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>Company: no company</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email dong4j@gmail.com
 * @since 2019-07-08 16:44
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class TencentOssState extends OssState{
    private String accessKey = "";
    private String secretKey = "";
    private String bucketName = "";
    private String regionName = "";
}
