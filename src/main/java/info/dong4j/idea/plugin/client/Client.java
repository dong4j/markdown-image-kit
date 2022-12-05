package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Description: 通过此注解标识不同的 oss 客户端 </p>
 * {@link info.dong4j.idea.plugin.util.ClientUtils}
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.22 17:17
 * @since 0.0.1
 */
@Target( {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Client {
    /**
     * Value
     *
     * @return the cloud enum
     * @since 0.0.1
     */
    CloudEnum value();
}
