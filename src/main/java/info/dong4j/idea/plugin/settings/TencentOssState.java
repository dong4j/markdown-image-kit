package info.dong4j.idea.plugin.settings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
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
