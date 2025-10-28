package info.dong4j.idea.plugin.settings;

import org.junit.Test;

/**
 * 移动到其他OSS设置对话框测试类
 * <p>
 * 用于测试 {@link MoveToOtherOssSettingsDialog} 对话框的显示和基本交互功能，包括窗口打包、设置可见性等操作。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2019.03.27
 * @since 1.1.0
 */
public class MoveToOtherOssSettingsDialogTest {
    /**
     * 测试移动到其他OSS设置对话框的显示功能
     * <p>
     * 测试场景：创建对话框实例并模拟其显示过程
     * 预期结果：对话框应正确打包并显示，程序随后退出
     */
    @Test
    public void test() {
        MoveToOtherOssSettingsDialog dialog = new MoveToOtherOssSettingsDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
