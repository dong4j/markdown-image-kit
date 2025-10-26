package info.dong4j.idea.plugin.enums;

/**
 * 标签插入位置枚举
 * <p>
 * 定义标签插入的不同场景和位置，用于标识标签在不同操作中插入的位置。
 * 包括右键图片上传后插入到剪贴板、paste 操作时插入到文档以及通过 quickfix 快捷键插入等场景。
 *
 * @author dong4j
 * @version 0.0.1
 * @email mailto:dong4j@gmail.com
 * @date 2019.03.26
 * @since 0.0.1
 */
public enum InsertEnum {
    /** 右键图片直接上传后插入到 clipboard */
    CLIPBOADR,
    /** 粘贴操作时使用的文档 */
    DOCUMENT,
    /** 快捷修复意图标记，用于标识需要快速修复的代码位置 */
    INTENTION
}
