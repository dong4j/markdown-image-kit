package info.dong4j.idea.plugin.enums;

/**
 * 上传方式枚举
 * <p>
 * 定义了系统中支持的上传方式，用于标识不同的上传来源或触发方式。
 * 包括测试按钮触发、右键菜单上传、以及剪贴板监听上传等场景。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
enum UploadWayEnum {
    /** 测试按钮 */
    FROM_TEST,
    /** 右键上传动作的标识符 */
    FROM_ACTION,
    /** 粘贴操作来源标识，用于标识剪贴板事件的触发来源 */
    FROM_PASTE
}
