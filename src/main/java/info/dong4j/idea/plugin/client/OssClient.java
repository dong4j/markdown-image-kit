package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * <p>Description: oss client 实现步骤:
 * 1. 初始化配置: 从持久化配置中初始化 client
 * 2. 静态内部类获取 client 单例
 * 3. 实现 OssClient 接口
 * 4. 自定义 upload 逻辑</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
public interface OssClient {
    /** 用于反射调用时的缓存 <className, client>, 容量为实现类个数 */
    Map<CloudEnum, OssClient> INSTANCES = new ConcurrentHashMap<>(12);

    /**
     * The constant getName.
     *
     * @return the name
     * @since 0.0.1
     */
    default String getName(){
        return this.getCloudType().title;
    }

    /**
     * Gets cloud type.
     *
     * @return the cloud type
     * @since 0.0.1
     */
    CloudEnum getCloudType();

    /**
     * 全部通过此接口上传
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     * @since 0.0.1
     */
    String upload(InputStream inputStream, String fileName) throws Exception;

    /**
     * "Upload Test" 按钮反射调用
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     * @throws Exception exception
     * @since 0.0.1
     */
    String upload(InputStream inputStream, String fileName, JPanel jPanel) throws Exception;

    /**
     * 需要设置 JTextField 的 name 属性
     *
     * @param jPanel the jpanel
     * @return the test field text
     * @since 0.0.1
     */
    @NotNull
    default Map<String, String> getTestFieldText(JPanel jPanel) {
        Map<String, String> fieldMap = new HashMap<>(8);
        Component[] components = jPanel.getComponents();
        for (Component c : components) {
            if (c instanceof JTextField textField) {
                fieldMap.put(textField.getName(), textField.getText());
            } else if (c instanceof JCheckBox checkBox) {
                fieldMap.put(checkBox.getName(), checkBox.isSelected() + "");
            }
        }
        return fieldMap;
    }
}
