package info.dong4j.idea.plugin.settings;

import com.intellij.ui.JBColor;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @date 2019-03-14 10:38
 * @email sjdong3@iflytek.com
 */
public class JTextFieldHintListener implements FocusListener {
    private String hintText;
    private JTextField textField;

    JTextFieldHintListener(JTextField jTextField, String hintText) {
        this.textField = jTextField;
        this.hintText = hintText;
        // 默认直接显示
        jTextField.setText(hintText);
        jTextField.setForeground(JBColor.GRAY);
    }

    @Override
    public void focusGained(FocusEvent e) {
        // 获取焦点时，清空提示内容
        String temp = textField.getText();
        if (temp.equals(hintText)) {
            textField.setText("");
            textField.setForeground(JBColor.BLACK);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        // 失去焦点时，没有输入内容，显示提示内容
        String temp = textField.getText();
        if ("".equals(temp)) {
            textField.setForeground(JBColor.GRAY);
            textField.setText(hintText);
        }
    }
}