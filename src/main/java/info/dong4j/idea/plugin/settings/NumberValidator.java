package info.dong4j.idea.plugin.settings;

import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 限制输入数字的位数</p>
 *
 * @author dong4j
 * @date 2019-03-14 20:00
 * @email sjdong3@iflytek.com
 */
public class NumberValidator extends PlainDocument {

    private static final long serialVersionUID = 1L;

    private int limit;

    public NumberValidator(int limit) {
        super();
        this.limit = limit;
    }

    @Override
    public void insertString(int offset, String str, AttributeSet attr)
        throws javax.swing.text.BadLocationException {
        if (str == null) {
            return;
        }
        if ((getLength() + str.length()) <= limit) {
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