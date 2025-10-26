package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.settings.OssState;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * PicList 图床状态类
 * <p>
 * 用于存储 PicList 图床的配置信息，包括 API 地址、图床类型、配置名称、请求方式和密钥等参数。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.26
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class PicListOssState extends OssState {
    /** PicList API 接口地址，默认为本地地址 */
    private String api = "http://127.0.0.1:36677/upload";
    /** 图床类型（非必填），用于指定使用的图床 */
    private String picbed = "";
    /** 配置文件名称（非必填），用于指定配置 */
    private String configName = "";
    /** 密钥（非必填），用于接口鉴权 */
    private String key = "";
    /** PicList 命令行可执行文件路径 */
    private String exePath = "";
}
