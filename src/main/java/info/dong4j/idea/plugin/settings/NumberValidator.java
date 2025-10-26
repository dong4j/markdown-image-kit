package info.dong4j.idea.plugin.settings;

import java.io.Serial;

import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;

/**
 * 数字输入验证器类
 * <p>
 * 该类用于限制用户在文本输入框中输入的数字位数，确保输入内容不超过指定的最大长度。
 * 实现了 {@link javax.swing.text.PlainDocument} 接口，通过重写 {@link #insertString} 方法
 * 过滤非数字字符，只允许输入数字。
 * </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2019.03.14
 * @since 0.0.1
 */
public class NumberValidator extends PlainDocument {
    /** 序列化版本号，用于确保类的兼容性 */
    @Serial
    private static final long serialVersionUID = 1L;
    /** 用于限制请求返回的数据条数 */
    private final int limit;

    /**
     * 数字校验器的构造方法，用于初始化最大限制值
     * <p>
     * 该构造方法设置数字校验器的最大允许值，用于后续的校验逻辑
     *
     * @param limit 最大允许值
     * @since 0.0.1
     */
    public NumberValidator(int limit) {
        super();
        this.limit = limit;
    }

    /**
     * 在指定位置插入字符串，仅允许插入数字字符
     * <p>
     * 该方法会检查传入的字符串是否包含数字字符，并仅插入数字部分。
     * 如果字符串为 null 或插入后超出限制，则不执行插入操作。
     *
     * @param offset 插入位置的偏移量
     * @param str    要插入的字符串
     * @param attr   插入时的属性集
     * @throws javax.swing.text.BadLocationException 如果插入位置无效
     */
    @Override
    public void insertString(int offset, String str, AttributeSet attr)
        throws javax.swing.text.BadLocationException {
        if (str == null) {
            return;
        }
        if ((this.getLength() + str.length()) <= this.limit) {
            char[] upper = str.toCharArray();
            int length = 0;
            for (int i = 0; i < upper.length; i++) {
                if (upper[i] >= '0' && upper[i] <= '9') {
                    upper[length++] = upper[i];
                }
            }
            // 插入数字
            super.insertString(offset, new String(upper, 0, length), attr);
        }
    }

}
