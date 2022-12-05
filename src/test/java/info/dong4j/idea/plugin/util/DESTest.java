package info.dong4j.idea.plugin.util;

import org.junit.Test;

/**
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.18 06:47
 * @since 1.1.0
 */
public class DESTest {
    /**
     * Test
     *
     * @since 1.1.0
     */
    @Test
    public void test() {
        String mi = DES.encrypt("", "zxcvbnm");
        System.out.println(mi);
        System.out.println("aaa  " + DES.decrypt("", "zxcvbnm"));
    }
}