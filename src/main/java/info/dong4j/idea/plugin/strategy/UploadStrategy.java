package info.dong4j.idea.plugin.strategy;

import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
     * Gets test field text.
     *
     * @param jPanel the j panel
     * @return the test field text
     */
    @NotNull
    default List<String> getTestFieldText(JPanel jPanel) {
        // 保存认证信息, 这个顺序是确定的
        List<String> textList = new ArrayList<>();
        Component[] components = jPanel.getComponents();
        for (Component c : components) {
            if (c instanceof JTextField) {
                JTextField textField = (JTextField) c;
                textList.add(textField.getText());
            }
        }
        return textList;
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
