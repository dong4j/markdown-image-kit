/*
 * MIT License
 *
 * Copyright (c) 2020 dong4j <dong4j@gmail.com>
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
 *
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
 * @date 2019-03-13 21:10
 * @email dong4j@gmail.com
 */
public class MikIcons {
    public static final Icon ALIYUN_OSS = IconLoader.getIcon("/icons/aliyun.png");
    public static final Icon QINIU_OSS = IconLoader.getIcon("/icons/qiniu.png");
    public static final Icon WEIBO_OSS = IconLoader.getIcon("/icons/weibo.png");
    public static final Icon COMPRESS = IconLoader.getIcon("/icons/compress.png");
    public static final Icon MOVE = IconLoader.getIcon("/icons/move.png");
    public static final Icon WANGYI = IconLoader.getIcon("/icons/wangyi.png");
    public static final Icon BAIDU = IconLoader.getIcon("/icons/baidu.png");
    public static final Icon IMGUR = IconLoader.getIcon("/icons/imgur.png");
    public static final Icon SM_MS = IconLoader.getIcon("/icons/sm_ms.png");
    public static final Icon YOUPAI = IconLoader.getIcon("/icons/youpai.png");
    public static final Icon JINGDONG = IconLoader.getIcon("/icons/jingdong.png");

    @NotNull
    private static Icon load(String path) {
        return IconLoader.getIcon(path, MikIcons.class);
    }
}
