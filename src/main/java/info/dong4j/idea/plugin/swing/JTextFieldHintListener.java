package info.dong4j.idea.plugin.swing;

import com.intellij.ui.JBColor;

import info.dong4j.idea.plugin.util.StringUtils;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.14 10:38
 * @since 0.0.1
 */
public class JTextFieldHintListener implements FocusListener {
    /** Hint text */
    private final String hintText;
    /** Text field */
    private final JTextField textField;

    /**
     * J text field hint listener
     *
     * @param jTextField j text field
     * @param hintText   hint text
     * @since 0.0.1
     */
    public JTextFieldHintListener(JTextField jTextField, String hintText) {
        this.textField = jTextField;
        this.hintText = hintText;
        init(jTextField, hintText);
    }

    /**
     * 焦点获得
     *
     * @param e e
     * @since 0.0.1
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
     * 焦点失去
     *
     * @param e e
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
     * Init
     *
     * @param jTextField j text field
     * @param hintText   hint text
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
     * 获取真实的 text
     *
     * @param jTextField j text field
     * @param hintText   hint text
     * @return the string
     * @since 1.4.0
     */
    public static String getRealText(JTextField jTextField, String hintText) {
        return jTextField.getText().trim().equals(hintText) ? "" : jTextField.getText().trim();
    }
}
