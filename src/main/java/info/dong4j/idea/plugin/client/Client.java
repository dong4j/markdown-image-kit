package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 通过此注解标识不同的 oss 客户端 </p>
 * {@link info.dong4j.idea.plugin.util.ClientUtils}
 *
 * @author dong4j
 * @date 2019-03-22 17:17
 * @email sjdong3@iflytek.com
 */
@Target( {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Client {
    CloudEnum value();
}
