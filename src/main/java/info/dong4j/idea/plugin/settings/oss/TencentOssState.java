package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.settings.OssState;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 腾讯云对象存储服务（OSS）状态信息类
 * <p>
 * 该类用于封装腾讯云OSS服务的相关配置信息，包括访问密钥、密钥、存储桶名称和区域名称等核心参数。
 * 作为 {@link OssState} 的子类，继承其基础状态信息，并扩展了腾讯云OSS特有的配置项。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
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
    /** 存储桶名称 */
    private String bucketName = "";
    /** 区域名称 */
    private String regionName = "";
}
