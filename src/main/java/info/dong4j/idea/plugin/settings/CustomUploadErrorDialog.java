package info.dong4j.idea.plugin.settings;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import lombok.Getter;

/**
 * 自定义上传错误对话框
 * <p>
 * 用于显示上传过程中发生的错误信息，并提供取消操作的功能。该对话框继承自 JDialog，支持通过 ESC 键或点击关闭按钮取消操作。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.17
 * @since 1.5.0
 */
@Getter
public class CustomUploadErrorDialog extends JDialog {
    /** 序列化版本号，用于确保类的兼容性 */
    private static final long serialVersionUID = 4813169387139926948L;
    /** 内容面板，用于显示主要界面内容 */
    private JPanel contentPane;
    /** 响应内容显示区域 */
    private JTextArea response;

    /**
     * 初始化自定义上传错误对话框
     * <p>
     * 设置对话框的布局、模态状态、关闭操作，并添加窗口监听器以处理关闭事件。同时注册键盘动作，使按下 ESC 键时触发取消操作。
     *
     * @since 1.5.0
     */
    public CustomUploadErrorDialog() {
        this.setContentPane(this.contentPane);
        this.setModal(true);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            /**
             * 处理窗口关闭事件，调用 onCancel 方法
             * <p>
             * 当窗口即将关闭时触发此方法，用于执行取消操作
             *
             * @param e 窗口事件对象
             */
            @Override
            public void windowClosing(WindowEvent e) {
                CustomUploadErrorDialog.this.onCancel();
            }
        });

        // call onCancel() on ESCAPE
        this.contentPane.registerKeyboardAction(e -> this.onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * 取消操作时的处理逻辑
     * <p>
     * 该方法在取消操作时被调用，通常用于清理资源或执行取消相关的逻辑。
     *
     * @since 1.5.0
     */
    private void onCancel() {
        // 必要时在此处添加您的代码
        this.dispose();
    }

}
