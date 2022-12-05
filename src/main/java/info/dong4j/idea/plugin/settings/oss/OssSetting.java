package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.settings.OssState;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Description:  </p>
 *
 * @param <T> parameter
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.17 13:40
 * @since 1.4.0
 */
public interface OssSetting<T extends OssState> {

    /**
     * Init
     *
     * @param state state
     * @since 0.0.1
     */
    void init(T state);

    /**
     * Is modified
     *
     * @param state state
     * @return the boolean
     * @since 1.4.0
     */
    boolean isModified(@NotNull T state);

    /**
     * Apply
     *
     * @param state state
     * @since 0.0.1
     */
    void apply(@NotNull T state);

    /**
     * Reset
     *
     * @param state state
     * @since 1.4.0
     */
    void reset(T state);
}
