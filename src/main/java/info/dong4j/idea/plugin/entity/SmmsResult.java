package info.dong4j.idea.plugin.entity;

import lombok.Data;

/**
 * SmmsResult 类
 * <p>
 * 用于封装 Smms（可能指某种图片上传或处理服务）的返回结果数据结构，包含状态码、数据信息、消息等字段。
 * 数据信息通过嵌套的 DataBean 类进行详细描述，包含图片的宽度、高度、文件名、存储名、大小、路径、哈希值、时间戳和删除链接等属性。
 * <p>
 * 该类使用 Lombok 的 @Data 注解简化了 getter、setter、toString 等方法的编写，适用于快速构建和处理 API 响应数据。
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14
 * @since 0.0.1
 */
@Data
public class SmmsResult {
    /** 业务操作代码 */
    private String code;
    /** 数据对象，用于封装接口返回的数据内容 */
    private DataBean data;
    /** 消息内容 */
    private String message;
    private String images;

    /**
     * 数据 bean 类
     * <p>
     * 用于封装数据相关的属性，包括宽度、高度、文件名、存储名、大小、路径、哈希值、时间戳、URL 和删除标识等信息。
     * 该类使用 Lombok 的 @Data 注解，自动生成 getter、setter、toString、equals 和 hashCode 方法。
     *
     * @author dong4j
     * @version 0.0.1
     * @email "mailto:dong4j@gmail.com"
     * @date 2021.02.14
     * @since 0.0.1
     */
    @Data
    public static class DataBean {
        /** 宽度 */
        private int width;
        /** 图片高度 */
        private int height;
        /** 文件名 */
        private String filename;
        /** 店铺名称 */
        private String storename;
        /** 数据集合的大小 */
        private int size;
        /** 路径信息 */
        private String path;
        /** 唯一标识符的哈希值 */
        private String hash;
        /** 时间戳，表示请求或操作的时间 */
        private int timestamp;
        /** 请求的 URL 地址 */
        private String url;
        /** 删除标识 */
        private String delete;
    }
}
