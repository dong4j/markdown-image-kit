package info.dong4j.idea.plugin.enums;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.util.EnumsUtils;

import org.jetbrains.annotations.Contract;

import java.util.Optional;

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
    SM_MS_CLOUD(0, "sm.ms", "oss.title.sm-ms", "info.dong4j.idea.plugin.client.SmmsClient"),
    /** 代表阿里云存储服务的枚举项 */
    ALIYUN_CLOUD(1, "AliCloud", "oss.title.aliyun", "info.dong4j.idea.plugin.client.AliyunOssClient"),
    /** 七牛云配置信息 */
    QINIU_CLOUD(2, "Qiniu", "oss.title.qiniu", "info.dong4j.idea.plugin.client.QiniuOssClient"),
    /** 腾讯云 OSS 客户端配置信息 */
    TENCENT_CLOUD(3, "Tencent", "oss.title.tencent", "info.dong4j.idea.plugin.client.TencentOssClient"),
    /** 百度云存储客户端类，用于与百度对象存储服务进行交互 */
    BAIDU_CLOUD(4, "Baidu", "oss.title.baidu", "info.dong4j.idea.plugin.client.BaiduBosClient"),
    /** GitHub 云服务类型枚举值 */
    GITHUB(5, "GitHub", "oss.title.github", "info.dong4j.idea.plugin.client.GithubClient"),
    /** Gitee 云服务枚举值 */
    GITEE(6, "Gitee", "oss.title.gitee", "info.dong4j.idea.plugin.client.GiteeClient"),
    /** 自定义云存储类型枚举值，表示用户自定义的云存储配置 */
    CUSTOMIZE(7, "Custom", "oss.title.custom", "info.dong4j.idea.plugin.client.CustomOssClient"),
    /** PicList 图床服务 */
    PICLIST(8, "PicList/PicGo", "oss.title.piclist", "info.dong4j.idea.plugin.client.PicListClient");
    /** 索引位置 */
    public final int index;
    /** 标题信息（延迟加载，初始化为默认值，运行时可能更新为本地化文本） */
    private String title;
    /** 标题默认值（英文），用于类初始化时避免访问资源包 */
    private final String defaultTitle;
    /**
     * 资源包 key, 用于延迟加载本地化文本
     * <p> 如果为 null, 则使用默认标题 (defaultTitle).
     * 该字段用于在运行时动态获取本地化的标题信息, 避免在类初始化阶段访问资源包.
     */
    private final String titleKey;
    /** 图床特征 */
    public final String feature;

    /**
     * Cloud 枚举的构造函数
     * <p>
     * 初始化一个 CloudEnum 实例，用于表示云类型的不同枚举值
     *
     * @param index        枚举的索引值
     * @param defaultTitle 枚举的标题默认值（英文），用于类初始化时避免访问资源包
     * @param titleKey     资源包 key，用于延迟加载本地化文本，如果为 null 则使用 defaultTitle
     * @param feature      枚举的特性描述
     * @since 0.0.1
     */
    @Contract(pure = true)
    CloudEnum(int index, String defaultTitle, String titleKey, String feature) {
        this.index = index;
        this.defaultTitle = defaultTitle;
        this.titleKey = titleKey;
        this.feature = feature;
        // 初始化时使用默认值，避免在类初始化阶段访问资源包
        this.title = defaultTitle;
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
     * 返回当前对象的标题属性值。如果配置了资源包 key，则尝试获取本地化文本；
     * 如果资源包不可用或获取失败，则返回默认值（英文）。
     * 使用延迟加载机制，避免在类初始化阶段访问资源包。
     * 同时更新 title 字段，以保持向后兼容。
     *
     * @return 标题信息
     * @since 0.0.1
     */
    public String getTitle() {
        // 如果 title 仍然是默认值，尝试从资源包获取本地化文本
        if (this.title.equals(this.defaultTitle)) {
            try {
                String localizedTitle = MikBundle.message(this.titleKey);
                // 更新 title 字段，以便直接访问 title 时也能获取到本地化文本
                this.title = localizedTitle;
                return localizedTitle;
            } catch (Exception e) {
                // 如果资源包不可用（可能在类初始化阶段），保持默认值
                return this.title;
            }
        }

        // 如果已经更新为本地化文本，直接返回
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

    /**
     * 根据给定的云索引获取对应的枚举值
     * <p>
     * 该方法通过传入的索引查找对应的 CloudEnum 枚举对象，由于业务逻辑已确保索引有效，因此不会返回 null。
     *
     * @param cloudIndex 云索引值
     * @return 对应的 CloudEnum 枚举对象
     * @since 0.0.1
     */
    public static CloudEnum of(int cloudIndex) {
        Optional<CloudEnum> cloudType = EnumsUtils.getEnumObject(CloudEnum.class, e -> e.getIndex() == cloudIndex);
        return cloudType.orElse(null);
    }
}
