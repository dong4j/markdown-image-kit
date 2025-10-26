package info.dong4j.idea.plugin.settings.oss;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 阿里云OSS状态实体类
 * <p>
 * 该类用于封装阿里云对象存储服务（OSS）相关状态信息，继承自AbstractExtendOssState抽象类，提供OSS操作状态的详细描述和扩展功能。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2019.03.19
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AliyunOssState extends AbstractExtendOssState {

}
