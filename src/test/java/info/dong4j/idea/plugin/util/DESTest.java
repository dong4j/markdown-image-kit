package info.dong4j.idea.plugin.util;

import org.junit.Test;

/**
 * <p>Company: no company</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-18 06:47
 * @email dong4j@gmail.com
 */
public class DESTest {
    @Test
    public void test() {
        String mi = DES.encrypt("", "zxcvbnm");
        System.out.println(mi);
        System.out.println("aaa  "+DES.decrypt("", "zxcvbnm"));
    }
}