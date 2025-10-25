package info.dong4j.idea.plugin.entity;

import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;

import java.io.InputStream;
import java.io.Serializable;

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
public class MarkdownImage implements Serializable {
    /** serialVersionUID */
    private static final long serialVersionUID = -533088989259774894L;

    /** 对应的 document name */
    private String fileName;
    /** 图片名 */
    private String imageName;
    /** 图片类型 */
    private String extension;
    /** 原始行文本 */
    private String originalLineText;
    /** markdown image mark */
    private String originalMark;
    /** 行数 */
    private int lineNumber;
    /** 行第一个字符偏移量 */
    private int lineStartOffset;
    /** 行最后一个字符偏移量 */
    private int lineEndOffset;
    /** 图片标题 */
    private String title;
    /** 图片地址 (本地全路径, 网路 http/https) */
    private String path;
    /** 图片位置 */
    private ImageLocationEnum location;
    /** 文本标签类型 */
    private ImageMarkEnum imageMarkType;
    /** 图片文件流 */
    private InputStream inputStream;
    /** 最终需要插入到文本的标签 */
    private String finalMark;
    /** 保存当前标签对应的 image file */
    private VirtualFile virtualFile;
}
