package info.dong4j.idea.plugin.settings;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import lombok.Getter;

/**
 * 移动到其他 OSS 设置对话框
 * <p>
 * 该类用于创建一个图形化界面对话框，允许用户设置目标 OSS 的相关信息，如域名、云服务选择等。
 * 支持通过 ESC 键或关闭窗口取消操作，并提供基本的界面布局和交互逻辑。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2019.03.27
 * @since 0.0.1
 */
@Getter
public class MoveToOtherOssSettingsDialog extends JDialog {
    /** 序列化版本号，用于确保类的版本兼容性 */
    @Serial
    private static final long serialVersionUID = -8961973066677674627L;
    /** 内容面板，用于显示主要界面内容 */
    private JPanel contentPane;
    /** 域名输入框 */
    private JTextField domain;
    /** 云服务下拉框 */
    private JComboBox<?> cloudComboBox;
    /** 移动面板，用于显示和操作移动相关功能 */
    private JPanel movePanel;
    /** 域标签 */
    private JLabel domainLabel;
    /** 移动标签 */
    private JLabel moveLabel;
    /** 界面显示的消息内容 */
    private JLabel message;
    /** 提示标签，用于显示操作提示信息 */
    private JLabel tipLable;

    /**
     * 初始化并显示“移动到其他OSS设置”对话框
     * <p>
     * 设置对话框的界面内容、模态状态、关闭操作，并注册ESC键的关闭监听
     *
     * @since 0.0.1
     */
    public MoveToOtherOssSettingsDialog() {
        this.setContentPane(this.contentPane);
        this.setModal(true);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            /**
             * 处理窗口关闭事件，调用取消操作
             * <p>
             * 当窗口关闭时触发该方法，执行取消操作逻辑
             *
             * @param e 窗口事件对象
             */
            @Override
            public void windowClosing(WindowEvent e) {
                MoveToOtherOssSettingsDialog.this.onCancel();
            }
        });

        // call onCancel() on ESCAPE
        this.contentPane.registerKeyboardAction(e -> this.onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * 取消操作时的处理逻辑
     * <p>
     * 当用户触发取消操作时，执行相应的清理或关闭操作，例如释放资源或关闭窗口。
     *
     * @since 0.0.1
     */
    private void onCancel() {
        // add your code here if necessary
        this.dispose();
    }
}
