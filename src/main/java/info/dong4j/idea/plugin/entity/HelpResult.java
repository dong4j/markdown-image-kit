package info.dong4j.idea.plugin.entity;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

/**
 * 帮助结果类
 * <p>
 * 用于封装帮助信息的返回结果，包含状态码和对应帮助链接
 * 该类通常用于系统中提供用户帮助或错误提示的场景
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Data
public class HelpResult implements Serializable {
    /** 序列化版本号，用于兼容性校验 */
    @Serial
    private static final long serialVersionUID = 2341371341825471102L;
    /** 用于存储或表示某种代码值 */
    private String code;
    /** 存储请求的目标URL */
    private String url;
}
