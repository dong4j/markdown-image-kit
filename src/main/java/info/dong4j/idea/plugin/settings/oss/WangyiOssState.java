package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.settings.OssState;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 网易云存储状态类
 * <p>
 * 该类用于表示网易云存储操作的状态信息，继承自通用的OssState类，提供与网易云存储相关的状态描述和处理逻辑。
 * 包含必要的状态字段和操作方法，用于在存储过程中传递和处理状态信息。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class WangyiOssState extends OssState {

}
