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

package icons;

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
    private static final String ICON_FOLDER = "/icons/";

    /** ALIYUN_OSS */
    public static final Icon ALIYUN_OSS = load("aliyun.svg");
    /** QINIU_OSS */
    public static final Icon QINIU_OSS = load("qiniu.svg");
    /** WEIBO_OSS */
    public static final Icon WEIBO_OSS = load("weibo.svg");
    /** TENCENT */
    public static final Icon TENCENT = load("tencent.svg");
    /** WANGYI */
    public static final Icon WANGYI = load("wangyi.svg");
    /** BAIDU */
    public static final Icon BAIDU = load("baidu.svg");
    /** IMGUR */
    public static final Icon IMGUR = load("imgur.svg");
    /** SM_MS */
    public static final Icon SM_MS = load("sm_ms.svg");
    /** YOUPAI */
    public static final Icon YOUPAI = load("youpai.svg");
    /** UCLOUD */
    public static final Icon UCLOUD = load("ucloud.svg");
    /** QING_CLOUD */
    public static final Icon QINGCLOUD = load("qingcloud.svg");
    /** JINGDONG */
    public static final Icon JINGDONG = load("jingdong.svg");
    /** GITHUB */
    public static final Icon GITHUB = load("github.svg");
    /** GITEE */
    public static final Icon GITEE = load("gitee.svg");
    /** CUSTOM */
    public static final Icon CUSTOM = load("custom.svg");

    /**
     * Load
     *
     * @param iconFilename icon filename
     * @return the icon
     * @since 0.0.1
     */
    @NotNull
    private static Icon load(String iconFilename) {
        return IconLoader.getIcon(ICON_FOLDER + iconFilename, MikIcons.class);
    }
}
