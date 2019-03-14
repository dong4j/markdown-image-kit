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
    /** 是否替换标签*/
    private boolean changeToHtmlTag = false;
    /** 替换的标签类型 */
    private String tagType = "";
    /** 替换的标签类型 code */
    private String tagTypeCode = "";
    /** 是否压缩图片 */
    private boolean compress = false;
    /** 上传前压缩 */
    private boolean compressBeforeUpload = false;
    /** 压缩比例 */
    private int compressBeforeUploadOfPercent = 80;
    /** 查看时压缩*/
    private boolean compressAtLookup = false;
    /** Aliyun OSS 图片压缩配置*/
    private String styleName = "";
    /** 图床迁移*/
    private boolean transport = false;
    /** 图片备份*/
    private boolean backup = false;
}
