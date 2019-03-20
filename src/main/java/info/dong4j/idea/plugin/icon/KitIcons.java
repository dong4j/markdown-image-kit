package info.dong4j.idea.plugin.icon;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: {@link com.intellij.icons.AllIcons}</p>
 *
 * @author dong4j
 * @date 2019-03-13 21:10
 * @email sjdong3@iflytek.com
 */
public class KitIcons {
    private static Icon load(String path) {
        return IconLoader.getIcon(path, KitIcons.class);
    }

    public static final Icon ALIYUN_OSS = IconLoader.getIcon("/icons/aliyun.png");
    public static final Icon QINIU_OSS = IconLoader.getIcon("/icons/qiniu.png");
    public static final Icon WEIBO_OSS = IconLoader.getIcon("/icons/weibo.png");
    public static final Icon COMPRESS = IconLoader.getIcon("/icons/compress.png");
    public static final Icon MOVE = IconLoader.getIcon("/icons/move.png");
}