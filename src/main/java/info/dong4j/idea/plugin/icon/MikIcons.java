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

package info.dong4j.idea.plugin.icon;

import com.intellij.openapi.util.IconLoader;

import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

/**
 * <p>Company: no company</p>
 * <p>Description: {@link com.intellij.icons.AllIcons}</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.13 21:10
 * @since 0.0.1
 */
public class MikIcons {
    /** ALIYUN_OSS */
    public static final Icon ALIYUN_OSS = IconLoader.getIcon("/icons/aliyun.png");
    /** QINIU_OSS */
    public static final Icon QINIU_OSS = IconLoader.getIcon("/icons/qiniu.png");
    /** WEIBO_OSS */
    public static final Icon WEIBO_OSS = IconLoader.getIcon("/icons/weibo.png");
    /** COMPRESS */
    public static final Icon COMPRESS = IconLoader.getIcon("/icons/compress.png");
    /** MOVE */
    public static final Icon MOVE = IconLoader.getIcon("/icons/move.png");
    /** WANGYI */
    public static final Icon WANGYI = IconLoader.getIcon("/icons/wangyi.png");
    /** BAIDU */
    public static final Icon BAIDU = IconLoader.getIcon("/icons/baidu.png");
    /** IMGUR */
    public static final Icon IMGUR = IconLoader.getIcon("/icons/imgur.png");
    /** SM_MS */
    public static final Icon SM_MS = IconLoader.getIcon("/icons/sm_ms.png");
    /** YOUPAI */
    public static final Icon YOUPAI = IconLoader.getIcon("/icons/youpai.png");
    /** JINGDONG */
    public static final Icon JINGDONG = IconLoader.getIcon("/icons/jingdong.png");
    /** GITHUB */
    public static final Icon GITHUB = IconLoader.getIcon("/icons/github.png");

    /**
     * Load
     *
     * @param path path
     * @return the icon
     * @since 0.0.1
     */
    @NotNull
    private static Icon load(String path) {
        return IconLoader.getIcon(path, MikIcons.class);
    }
}
