package info.dong4j.idea.plugin.settings.oss;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 百度云对象存储服务状态类
 * <p>
 * 该类用于表示百度云对象存储服务（BOS）操作的状态信息，继承自通用的OSS状态类，提供与百度云BOS相关的状态描述和错误信息。
 * <p>
 * 该类通过 {@code @Data} 注解提供 Lombok 的自动 getter、setter、toString 等方法，通过 {@code @NoArgsConstructor} 提供无参构造方法，
 * 并通过 {@code @EqualsAndHashCode} 注解确保对象的 equals 和 hashCode 方法正确实现。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BaiduBosState extends AbstractExtendOssState {

}
