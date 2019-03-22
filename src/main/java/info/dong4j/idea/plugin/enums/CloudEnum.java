package info.dong4j.idea.plugin.enums;

import org.jetbrains.annotations.Contract;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 枚举顺序不能改变
 * 后期扩展时, 只需要实现具体的上传逻辑, 需要在此处添加具体的 OssClient</p>
 *
 * @author dong4j
 * @date 2019-03-17 00:15
 * @email sjdong3@iflytek.com
 */
public enum CloudEnum {
    /** @see info.dong4j.idea.plugin.client.WeiboOssClient */
    WEIBO_CLOUD(0, "微博", "info.dong4j.idea.plugin.client.WeiboOssClient"),
    /** @see info.dong4j.idea.plugin.client.AliyunOssClient */
    ALIYUN_CLOUD(1, "阿里云", "info.dong4j.idea.plugin.client.AliyunOssClient"),
    /** @see info.dong4j.idea.plugin.client.QiniuOssClient */
    QINIU_CLOUD(2, "七牛云", "info.dong4j.idea.plugin.client.QiniuOssClient"),
    WANGYI_CLOUD(3, "网易云", ""),
    BAIDU_CLOUD(4, "百度云", ""),
    JINGDONG_CLOUD(5, "京东云", ""),
    YOUPAI_CLOUD(6, "又拍云", ""),
    SM_MS_CLOUD(7, "sm.ms", ""),
    IMGUR_CLOUD(8, "Imgur", ""),
    U_CLOUD(9, "Ucloud", ""),
    QING_CLOUD(10, "QIngCloud", ""),
    CUSTOMIZE(11, "自定义", "");

    public int index;
    public String title;
    public String className;

    CloudEnum(int index, String title, String className) {
        this.index = index;
        this.title = title;
        this.className = className;
    }

    @Contract(pure = true)
    public int getIndex() {
        return index;
    }

    @Contract(pure = true)
    public String getTitle() {
        return title;
    }

    @Contract(pure = true)
    public String getClassName() {
        return className;
    }
}
