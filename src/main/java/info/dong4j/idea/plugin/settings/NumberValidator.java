package info.dong4j.idea.plugin.settings;

import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;

/**
 * <p>Description: 限制输入数字的位数</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.14 20:00
 * @since 0.0.1
 */
public class NumberValidator extends PlainDocument {

    /** serialVersionUID */
    private static final long serialVersionUID = 1L;

    /** Limit */
    private final int limit;

    /**
     * Number validator
     *
     * @param limit limit
     * @since 0.0.1
     */
    public NumberValidator(int limit) {
        super();
        this.limit = limit;
    }

    /**
     * Insert string
     *
     * @param offset offset
     * @param str    str
     * @param attr   attr
     * @since 0.0.1
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
