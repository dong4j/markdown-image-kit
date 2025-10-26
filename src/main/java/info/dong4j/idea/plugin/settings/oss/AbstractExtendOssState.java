package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.settings.OssState;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Oss 状态抽象类
 * <p>
 * 提供 OSS（对象存储服务）相关配置信息的抽象结构，用于封装 Endpoint、Access Key、Access Secret Key、Bucket 名称、文件目录等基础属性。
 * 该类作为其他具体 OSS 状态类的基类，支持自定义 Endpoint 的功能。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.1.0
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractExtendOssState extends OssState {
    /** 服务端点地址 */
    private String endpoint = "";
    /** Access key */
    private String accessKey = "";
    /** 访问密钥，用于身份验证和请求签名 */
    private String accessSecretKey = "";
    /** 存储桶名称 */
    private String bucketName = "";
    /** 文件目录路径，用于存储或读取文件的相对路径 */
    private String filedir = "";
    /** 自定义端点地址 */
    private String customEndpoint = "";
    /** 是否使用自定义端点 */
    private Boolean isCustomEndpoint = false;
}
