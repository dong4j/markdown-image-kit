package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.settings.OssState;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 抽象类，用于表示 OpenOss 状态信息的基类
 * <p>
 * 该类封装了与 OpenOss 相关的基本状态参数，如仓库地址、分支、Token、文件目录等。
 * 作为抽象类，它为具体的状态实现类提供通用字段和结构，便于扩展和复用。
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.17
 * @since 1.4.0
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractOpenOssState extends OssState {
    /** 仓库信息，用于存储或展示相关仓库名称 */
    private String repos = "";
    /** 分支名称 */
    protected String branch = "";
    /** Token 值 */
    private String token = "";
    /** 文件目录路径，用于存储或读取文件的相对路径 */
    private String filedir = "";
    /** 自定义端点地址，用于指定特定的服务接口地址 */
    private String customEndpoint = "";
    /** 是否使用自定义端点 */
    private Boolean isCustomEndpoint = false;
}
