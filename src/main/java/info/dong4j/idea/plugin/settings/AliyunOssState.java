package info.dong4j.idea.plugin.settings;

import lombok.Data;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-13 14:21
 */
@Data
public class AliyunOssState {
    private String endpoint = "";
    private String accessKey = "";
    private String accessSecretKey = "";
    private String bucketName = "";
    private String filedir = "";
    private String suffix = "日期-文件名";
}
