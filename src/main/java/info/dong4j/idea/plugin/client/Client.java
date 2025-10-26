package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 客户端注解
 * <p>
 * 用于标识不同的 OSS 客户端类型，通过该注解可以区分和配置不同的客户端实现。
 * 通常与 {@link info.dong4j.idea.plugin.util.ClientUtils} 工具类配合使用，实现客户端的动态选择和管理。
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@Target( {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Client {
    /**
     * 获取云枚举值
     * <p>
     * 返回当前对象对应的云枚举值
     *
     * @return 云枚举
     * @since 0.0.1
     */
    CloudEnum value();
}
