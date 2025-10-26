package info.dong4j.idea.plugin.enums;

import info.dong4j.idea.plugin.MikBundle;

import org.jetbrains.annotations.Contract;

/**
 * 云存储服务枚举类
 * <p>
 * 定义了支持的云存储服务类型，每个枚举值对应一个具体的云服务提供商，包含其索引、显示名称和对应的客户端类。
 * 枚举顺序不能改变，用于确保服务类型的唯一性和稳定性。后期扩展时，只需实现具体的上传逻辑，并在此处添加对应的OssClient。
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 0.0.1
 */
public enum CloudEnum {
    /** sm.ms 云服务配置信息 */
    SM_MS_CLOUD(-1, "sm.ms", "info.dong4j.idea.plugin.client.SmmsClient"),
    /** 代表阿里云存储服务的枚举项 */
    ALIYUN_CLOUD(0, MikBundle.message("oss.title.aliyun"), "info.dong4j.idea.plugin.client.AliyunOssClient"),
    /** 七牛云配置信息 */
    QINIU_CLOUD(1, MikBundle.message("oss.title.qiniu"), "info.dong4j.idea.plugin.client.QiniuOssClient"),
    /** 腾讯云 OSS 客户端配置信息 */
    TENCENT_CLOUD(2, MikBundle.message("oss.title.tencent"), "info.dong4j.idea.plugin.client.TencentOssClient"),
    /** 百度云存储客户端类，用于与百度对象存储服务进行交互 */
    BAIDU_CLOUD(3, MikBundle.message("oss.title.baidu"), "info.dong4j.idea.plugin.client.BaiduBosClient"),
    /** GitHub 云服务类型枚举值 */
    GITHUB(4, "GitHub", "info.dong4j.idea.plugin.client.GithubClient"),
    /** Gitee 云服务枚举值 */
    GITEE(5, "Gitee", "info.dong4j.idea.plugin.client.GiteeClient"),
    /** 自定义云存储类型枚举值，表示用户自定义的云存储配置 */
    CUSTOMIZE(6, MikBundle.message("oss.title.custom"), "info.dong4j.idea.plugin.client.CustomOssClient"),
    /** PicList 图床服务 */
    PICLIST(7, "PicList/PicGo", "info.dong4j.idea.plugin.client.PicListClient");
    /** 索引位置 */
    public final int index;
    /** 标题信息 */
    public final String title;
    /** 图床特征 */
    public final String feature;

    /**
     * Cloud 枚举的构造函数
     * <p>
     * 初始化一个 CloudEnum 实例，用于表示云类型的不同枚举值
     *
     * @param index   枚举的索引值
     * @param title   枚举的标题名称
     * @param feature 枚举的特性描述
     * @since 0.0.1
     */
    @Contract(pure = true)
    CloudEnum(int index, String title, String feature) {
        this.index = index;
        this.title = title;
        this.feature = feature;
    }

    /**
     * 获取当前索引值
     * <p>
     * 返回该对象维护的索引值
     *
     * @return 当前索引值
     * @since 0.0.1
     */
    @Contract(pure = true)
    public int getIndex() {
        return this.index;
    }

    /**
     * 获取标题信息
     * <p>
     * 返回当前对象的标题属性值
     *
     * @return 标题信息
     * @since 0.0.1
     */
    @Contract(pure = true)
    public String getTitle() {
        return this.title;
    }

    /**
     * 获取功能特征值
     * <p>
     * 返回当前对象存储的功能特征字符串值
     *
     * @return 功能特征值
     * @since 0.0.1
     */
    @Contract(pure = true)
    public String getFeature() {
        return this.feature;
    }
}
