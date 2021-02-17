/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.settings.OssState;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Company: 成都返空汇网络技术有限公司</p>
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
