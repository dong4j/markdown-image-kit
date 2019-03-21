package info.dong4j.idea.plugin.singleton;

import info.dong4j.idea.plugin.enums.SuffixEnum;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.util.CharacterUtils;
import info.dong4j.idea.plugin.util.EnumsUtils;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-20 11:52
 */
public interface OssClient {
    /** 用于反射调用时的缓存 */
    Map<String, Object> UPLOADER = new ConcurrentHashMap<>(12);
    /** 重命名文件的前缀 */
    String PREFIX = "MIK-";

    /**
     * 统一处理 fileName
     *
     * @param fileName the file name
     * @return the string
     */
    default String processFileName(String fileName) {
        ImageManagerState state = ImageManagerPersistenComponent.getInstance().getState();
        if (state.isRename()) {
            int sufixIndex = state.getSuffixIndex();
            Optional<SuffixEnum> sufix = EnumsUtils.getEnumObject(SuffixEnum.class, e -> e.getIndex() == sufixIndex);
            SuffixEnum suffixEnum = sufix.orElse(SuffixEnum.FILE_NAME);
            switch (suffixEnum) {
                case FILE_NAME:
                    return fileName;
                case DATE_FILE_NAME:
                    return DateFormatUtils.format(new Date(), "yyyy-MM-dd-") + fileName;
                case RANDOM:
                    return PREFIX + CharacterUtils.getRandomString(6) + fileName.substring(fileName.lastIndexOf("."));
                default:
                    return fileName;
            }
        }
        return fileName;
    }

    /**
     * 需要设置 JTextField 的 name 属性
     *
     * @param jPanel the jpanel
     * @return the test field text
     */
    @NotNull
    default Map<String, String> getTestFieldText(JPanel jPanel) {
        Map<String, String> fieldMap = new HashMap<>(10);
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
     * The constant getName.
     *
     * @return the name
     */
    String getName();

    /**
     * Upload string.
     *
     * @param file the file
     * @return the string
     */
    String upload(File file);

    /**
     * Upload string.
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    String upload(InputStream inputStream, String fileName);

    /**
     * "Upload Test" 按钮反射调用
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     */
    String upload(InputStream inputStream, String fileName, JPanel jPanel);

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
