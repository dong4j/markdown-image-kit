package icons;

import com.intellij.openapi.util.IconLoader;

import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

/**
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
