package info.dong4j.idea.plugin.settings.oss;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * GitHub 云存储状态类
 * <p>
 * 用于表示 GitHub 云存储操作的状态信息，继承自通用的 OpenOssState 抽象类，提供与 GitHub 云存储相关的状态数据。
 * 包括分支信息等关键属性。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.16
 * @since 1.3.0
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GithubOssState extends AbstractOpenOssState {
    /** 分支名称，默认为 "main" */
    protected String branch = "main";
}
