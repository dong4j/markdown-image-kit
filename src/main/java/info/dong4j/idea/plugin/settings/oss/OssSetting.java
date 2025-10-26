package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.settings.OssState;

import org.jetbrains.annotations.NotNull;

/**
 * OssSetting 接口
 * <p>
 * 定义对象存储设置相关的操作接口，用于初始化、判断是否修改、应用和重置对象存储状态。
 * 该接口通过泛型参数 T 绑定到具体的 OssState 实现类，提供统一的配置管理方式。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.17
 * @since 1.4.0
 */
public interface OssSetting<T extends OssState> {
    /**
     * 初始化方法，用于设置或准备状态信息
     * <p>
     * 该方法接收一个状态对象作为参数，用于初始化相关配置或数据。
     *
     * @param state 状态对象，用于初始化操作
     * @since 0.0.1
     */
    void init(T state);

    /**
     * 判断状态是否被修改
     * <p>
     * 根据传入的状态对象判断是否发生修改
     *
     * @param state 状态对象
     * @return 是否被修改
     * @since 1.4.0
     */
    boolean isModified(@NotNull T state);

    /**
     * 应用指定的状态
     * <p>
     * 将传入的状态应用到当前对象上，通常用于更新或处理状态相关的逻辑。
     *
     * @param state 要应用的状态对象
     * @since 0.0.1
     */
    void apply(@NotNull T state);

    /**
     * 重置对象状态为指定状态
     * <p>
     * 将当前对象的状态设置为传入的参数值
     *
     * @param state 要设置的新状态
     * @since 1.4.0
     */
    void reset(T state);
}
