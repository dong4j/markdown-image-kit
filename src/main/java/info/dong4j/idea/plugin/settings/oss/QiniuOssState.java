package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.enums.ZoneEnum;
import info.dong4j.idea.plugin.settings.OssState;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 七牛云对象存储状态类
 * <p>
 * 该类用于封装七牛云对象存储服务的相关配置信息，包括访问端点、访问密钥、秘密密钥和存储桶名称等。
 * 继承自 OssState 类，用于区分不同云服务商的存储状态配置。
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class QiniuOssState extends OssState {
    /** 服务端点地址 */
    private String endpoint = "";
    /** Access key */
    private String accessKey = "";
    /** 访问密钥，用户用于身份验证的私密密钥 */
    private String accessSecretKey = "";
    /** 存储桶名称 */
    private String bucketName = "";
    /** 区域索引，表示当前区域对应的枚举值索引 */
    private int zoneIndex = ZoneEnum.EAST_CHINA.index;
}
