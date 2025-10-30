package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.settings.OssState;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 自定义的OSS状态类
 * <p>
 * 该类继承自OssState，用于封装与OSS（对象存储服务）相关的自定义状态信息，包括API地址、请求键、响应URL路径和HTTP方法等。
 * 可用于配置和管理OSS上传或处理文件时的状态参数。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class SmmsOssState extends OssState {
    /** 认证 */
    private String token = "";
}
