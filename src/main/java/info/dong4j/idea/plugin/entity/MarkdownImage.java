package info.dong4j.idea.plugin.entity;

import info.dong4j.idea.plugin.enums.MarkdownImageLocation;

import java.io.*;

import lombok.Data;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-14 21:00
 */
@Data
public class MarkdownImage implements Serializable {
    private static final long serialVersionUID = -533088989259774894L;

    private String fileName;
    /** 原始文本 */
    private String originalLineText;
    /** 行数 */
    private int lineNumber;
    /** 行第一个字符偏移量 */
    private int lineStartOffset;
    /** 行最后一个字符偏移量 */
    private int lineEndOffset;
    /** 图片标题 */
    private String title;
    /** 图片地址 (如果是本地, 就是 fileName, 如果是网络, 就是 http/https 地址) */
    private String path;
    /** 图片位置 */
    private MarkdownImageLocation location = MarkdownImageLocation.LOCAL;
    /** 上传后的 url */
    private String uploadedUrl;
}
