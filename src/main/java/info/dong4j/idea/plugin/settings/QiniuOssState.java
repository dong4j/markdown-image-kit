package info.dong4j.idea.plugin.settings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @date 2019-03-19 19:55
 * @email sjdong3@iflytek.com
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class QiniuOssState extends OssState{
    private String endpoint = "";
    private String accessKey = "";
    private String accessSecretKey = "";
    private String bucketName = "";
    private String filedir = "";
    private String suffix = "日期-文件名";
    /** 查看时压缩 */
    private boolean compressAtLookup = false;
    /** Aliyun OSS 图片压缩配置 */
    private String styleName = "";
    private String url = "";
}