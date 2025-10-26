package info.dong4j.idea.plugin.swing;

import com.intellij.ui.JBColor;

import info.dong4j.idea.plugin.util.StringUtils;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

/**
 * JTextFieldHintListener 类
 * <p>
 * 用于为 JTextField 添加提示文本（hint text）功能，当文本框获得焦点时清除提示文本，失去焦点时若为空则恢复提示文本。
 * 支持初始化和获取真实文本的功能，适用于需要输入提示的图形界面场景。
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 0.0.1
 */
public class JTextFieldHintListener implements FocusListener {
    /** 提示文本 */
    private final String hintText;
    /** 文本输入框，用于用户输入文本内容 */
    private final JTextField textField;

    /**
     * 初始化文本字段的提示监听器
     * <p>
     * 该构造函数用于创建一个文本字段的提示监听器，设置文本字段和提示文本，并初始化相关功能。
     *
     * @param jTextField 文本字段对象
     * @param hintText   提示文本内容
     * @since 0.0.1
     */
    public JTextFieldHintListener(JTextField jTextField, String hintText) {
        this.textField = jTextField;
        this.hintText = hintText;
        init(jTextField, hintText);
    }

    /**
     * 当组件获得焦点时触发，用于清空输入框中的提示内容
     * <p>
     * 如果输入框中的文本与提示内容相同，则清空文本并恢复默认字体颜色
     *
     * @param e 事件对象，包含焦点变化的相关信息
     */
    @Override
    public void focusGained(FocusEvent e) {
        // 获取焦点时，清空提示内容
        String temp = this.textField.getText();
        if (temp.equals(this.hintText)) {
            this.textField.setText("");
            this.textField.setForeground(JBColor.BLACK);
        }
    }

    /**
     * 处理文本框失去焦点事件
     * <p>
     * 当文本框失去焦点且未输入内容时，将文本框文字设置为提示内容，并显示为灰色字体
     *
     * @param e 焦点事件对象
     * @since 0.0.1
     */
    @Override
    public void focusLost(FocusEvent e) {
        // 失去焦点时，没有输入内容，显示提示内容
        String temp = this.textField.getText();
        if ("".equals(temp)) {
            this.textField.setForeground(JBColor.GRAY);
            this.textField.setText(this.hintText);
        }
    }

    /**
     * 初始化文本字段，设置默认提示文本和灰色字体颜色
     * <p>
     * 如果文本字段内容为空，则设置提示文本并将其字体颜色设置为灰色
     *
     * @param jTextField 要初始化的文本字段
     * @param hintText   提示文本内容
     * @since 1.4.0
     */
    public static void init(JTextField jTextField, String hintText) {
        if (StringUtils.isBlank(jTextField.getText().trim())) {
            // 默认直接显示
            jTextField.setText(hintText);
            jTextField.setForeground(JBColor.GRAY);
        }
    }

    /**
     * 获取真实的文本内容
     * <p>
     * 该方法用于获取文本字段中实际输入的文本内容，若输入内容与提示文本相同，则返回空字符串。
     *
     * @param jTextField 文本字段对象
     * @param hintText   提示文本
     * @return 实际输入的文本内容，若与提示文本相同则返回空字符串
     * @since 1.4.0
     */
    public static String getRealText(JTextField jTextField, String hintText) {
        return jTextField.getText().trim().equals(hintText) ? "" : jTextField.getText().trim();
    }
}
