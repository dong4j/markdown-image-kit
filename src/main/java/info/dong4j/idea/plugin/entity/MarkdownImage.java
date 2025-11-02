package info.dong4j.idea.plugin.entity;

import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;

import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

/**
 * Markdown 图片信息实体类
 * <p>
 * 用于存储和表示 Markdown 文档中图片的相关信息，包括文件名、图片名、类型、原始行文本、行号、偏移量、图片标题、图片地址、图片位置等属性。
 * 该类支持序列化，适用于在不同系统间传输 Markdown 图片数据。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@Data
public class MarkdownImage implements Serializable {
    /** 序列化版本号，用于确保类的兼容性 */
    @Serial
    private static final long serialVersionUID = -533088989259774894L;
    /** 对应的文档名称 */
    private String filename;
    /** 图片名称 */
    private String imageName;
    /** 图片文件扩展名 */
    private String extension;
    /** 原始行文本 */
    private String originalLineText;
    /** 原始的 markdown 图片标记 */
    private String originalMark;
    /** 行数 */
    private int lineNumber;
    /** 行第一个字符的偏移量 */
    private int lineStartOffset;
    /** 行最后一个字符的偏移量 */
    private int lineEndOffset;
    /** 图片标题 */
    private String title;
    /** 图片地址，支持本地全路径或网络地址（http/https） */
    private String path;
    /** 图片在页面中的位置类型 */
    private ImageLocationEnum location;
    /** 图像标注类型，用于标识文本标签的样式或形式 */
    private ImageMarkEnum imageMarkType;
    /** 图片文件流 */
    private InputStream inputStream;
    /** 最终需要插入到文本的标签 */
    private String finalMark;
    /** 保存当前标签对应的虚拟文件对象 */
    private VirtualFile virtualFile;
    /** 源文件的绝对路径，用于粘贴文件时保存原始文件路径 */
    private String sourceFilePath;
    /** 标记图片是否为图片流（true）还是文件（false） */
    private boolean isImageStream = true;
}
