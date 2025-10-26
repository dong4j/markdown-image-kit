package info.dong4j.idea.plugin.settings.oss;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * GiteeOssState 类
 * <p>
 * 用于表示 Gitee 云服务对象存储的状态信息，继承自 AbstractOpenOssState 抽象类，提供与 Gitee 云存储相关的状态配置。
 * 包含分支信息等属性，用于控制对象存储操作的上下文环境。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.16
 * @since 1.4.0
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GiteeOssState extends AbstractOpenOssState {
    /** 分支名称，默认为 master */
    protected String branch = "master";
}
