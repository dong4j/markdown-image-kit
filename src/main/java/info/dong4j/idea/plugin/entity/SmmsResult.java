package info.dong4j.idea.plugin.entity;

import lombok.Data;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Data
public class SmmsResult {

    /** Code */
    private String code;
    /** Data */
    private DataBean data;
    /** Message */
    private String message;

    /**
     * <p>Description: </p>
     *
     * @author dong4j
     * @version 0.0.1
     * @email "mailto:dong4j@gmail.com"
     * @date 2021.02.14 18:40
     * @since 0.0.1
     */
    @Data
    public static class DataBean {
        /** Width */
        private int width;
        /** Height */
        private int height;
        /** Filename */
        private String filename;
        /** Storename */
        private String storename;
        /** Size */
        private int size;
        /** Path */
        private String path;
        /** Hash */
        private String hash;
        /** Timestamp */
        private int timestamp;
        /** Url */
        private String url;
        /** Delete */
        private String delete;
    }
}
