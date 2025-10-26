package info.dong4j.idea.plugin.enums;

import org.jetbrains.annotations.Contract;

/**
 * 七牛云存储区域枚举
 * <p>
 * 用于表示七牛云存储的不同区域，每个区域包含索引、名称和对应的主机地址，用于区分不同地区的存储服务。
 * 支持华东、华北、华南、北美和东南亚等区域。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public enum ZoneEnum {
    // 区域名称：z0 华东  z1 华北  z2 华南  na0 北美  as0 东南亚
    /** 华东区域枚举值，对应区域名称为“华东”，用于标识华东地区的上传地址 */
    EAST_CHINA(0, "华东", "upload.qiniup.com"),
    /** 北方地区枚举值，对应华北区域的上传域名 */
    NORT_CHINA(1, "华北", "upload-z1.qiniup.com"),
    /** 华南地区枚举值，对应区域服务器地址为 upload-na0.qiniup.com */
    SOUTH_CHINA(2, "华南", "upload-na0.qiniup.com"),
    /** 北美区域枚举值，对应上传域名 upload-as0.qiniup.com */
    NORTH_AMERIA(3, "北美", "upload-as0.qiniup.com"),
    /** 东南亚区域枚举值，对应七牛云存储的区域域名 */
    SOUTHEAST_ASIA(4, "东南亚", "upload-as0.qiniup.com");
    /** 索引值，表示当前元素在集合或列表中的位置 */
    public final int index;
    /** 名称 */
    public final String name;
    /** 主机地址，用于标识服务所在的区域或节点 */
    public final String host;

    /**
     * 构造函数，用于创建 ZoneEnum 实例
     * <p>
     * 初始化 ZoneEnum 对象的索引、名称和主机信息
     *
     * @param index 索引值
     * @param name  名称
     * @param host  主机地址
     * @since 0.0.1
     */
    ZoneEnum(int index, String name, String host) {
        this.index = index;
        this.name = name;
        this.host = host;
    }

    /**
     * 获取当前对象的索引值
     * <p>
     * 返回该对象内部维护的索引属性值
     *
     * @return 索引值
     * @since 0.0.1
     */
    @Contract(pure = true)
    public int getIndex() {
        return this.index;
    }

    /**
     * 获取名称
     * <p>
     * 返回当前对象的名称属性值
     *
     * @return 名称
     * @since 0.0.1
     */
    @Contract(pure = true)
    public String getName() {
        return this.name;
    }

    /**
     * 获取主机信息
     * <p>
     * 返回当前主机的名称或标识信息
     *
     * @return 主机信息
     * @since 0.0.1
     */
    @Contract(pure = true)
    public String getHost() {
        return this.host;
    }
}
