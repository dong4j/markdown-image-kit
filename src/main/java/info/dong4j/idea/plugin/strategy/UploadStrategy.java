package info.dong4j.idea.plugin.strategy;

import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019 -03-17 02:10
 * @email sjdong3 @iflytek.com
 */
public interface UploadStrategy {
    /**
     * Upload string.
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    String upload(InputStream inputStream, String fileName);

    /**
     * 需要设置 JTextField 的 name 属性
     *
     * @param jPanel the j panel
     * @return the test field text
     */
    @NotNull
    default Map<String, String> getTestFieldText(JPanel jPanel) {
        Map<String, String> fieldMap = new HashMap<>(10);
        // 保存认证信息, 这个顺序是确定的
        Component[] components = jPanel.getComponents();
        for (Component c : components) {
            if (c instanceof JTextField) {
                JTextField textField = (JTextField) c;
                fieldMap.put(textField.getName(), textField.getText());
            }
        }
        return fieldMap;
    }

    /**
     * The enum Upload way enum.
     */
    enum UploadWayEnum {
        /** 测试按钮 */
        FROM_TEST,
        /** 右键上传 */
        FROM_ACTION,
        /** clipboard 监听 */
        FROM_PASTE
    }
}
