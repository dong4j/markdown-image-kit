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
public class AliyunOssState extends OssState {
    private String endpoint = "";
    private String accessKey = "";
    private String accessSecretKey = "";
    private String bucketName = "";
    private String filedir = "";
    /** todo-dong4j : (2019年03月17日 01:49) [使用 index 保存] */
    private String suffix = "日期-文件名";
    /** 查看时压缩 */
    private boolean compressAtLookup = false;
    /** Aliyun OSS 图片压缩配置 */
    private String styleName = "";
}