package icons;

import com.intellij.openapi.util.IconLoader;

import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

/**
 * <p>Description: MIK Plugin Icons</p>
 *
 * @author dong4j
 * @version 2.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2022.10.24
 * @since 2.0.0
 */
public class MikIcons {
    private static final String ICON_FOLDER = "/icons/";

    /** MIK ICON */
    public static final Icon MIK = load("mikIcon.svg");

    /** 图片上传 */
    public static final Icon UPLOAD = load("upload.svg");
    /** 图片压缩 */
    public static final Icon COMPRESS = load("compress.svg");
    /** 图床迁移 */
    public static final Icon MIGRATION = load("migration.svg");
    /** 标签替换 */
    public static final Icon LABEL = load("label.svg");
    
    /** ALIYUN_OSS */
    public static final Icon ALIYUN_OSS = load("aliyun.svg");
    /** QINIU_OSS */
    public static final Icon QINIU_OSS = load("qiniu.svg");
    /** TENCENT */
    public static final Icon TENCENT = load("tencent.svg");
    /** BAIDU */
    public static final Icon BAIDU = load("baidu.svg");
    /** SM_MS */
    public static final Icon SM_MS = load("sm_ms.svg");
    /** GITHUB */
    public static final Icon GITHUB = load("github.svg");
    /** GITEE */
    public static final Icon GITEE = load("gitee.svg");
    /** CUSTOM */
    public static final Icon CUSTOM = load("custom.svg");
    /** PICLIST */
    public static final Icon PICLIST = load("piclist.svg");

    /** QINGCLOUD */
    public static final Icon QINGCLOUD = load("qingcloud.svg");
    /** UCLOUD */
    public static final Icon UCLOUD = load("ucloud.svg");
    /** WANGYI */
    public static final Icon WANGYI = load("wangyi.svg");
    /** WEIBO */
    public static final Icon WEIBO = load("weibo.svg");
    /** JINGDONG */
    public static final Icon JINGDONG = load("jingdong.svg");
    /** IMGUR */
    public static final Icon IMGUR = load("imgur.svg");

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
