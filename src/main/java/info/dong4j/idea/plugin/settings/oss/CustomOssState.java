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
public class CustomOssState extends OssState {
    /** 上传文件的 API 地址 */
    private String api = "http://127.0.0.1:8080/upload";
    /** 请求唯一标识符，用于关联请求与响应 */
    private String requestKey = "";
    /** 响应的 URL 路径 */
    private String responseUrlPath = "";
    /** HTTP 请求方法，例如 POST、GET 等，默认值为 POST */
    private String httpMethod = "POST";
}
