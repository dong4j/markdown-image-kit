package info.dong4j.idea.plugin.util;

import org.junit.Test;

/**
 * DES 加密测试类
 * <p>
 * 用于测试 DES 加密算法的加密和解密功能，包含一个测试方法用于验证加密和解密的正确性。
 * </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2019.03.18
 * @since 1.1.0
 */
public class DESTest {
    /**
     * 测试 DES 加密与解密功能
     * <p>
     * 测试场景：对空字符串进行加密和解密操作
     * 预期结果：加密后的字符串应能正确解密回原始值
     * <p>
     * 注意：该测试仅验证基本加密解密流程，未涉及特殊字符或复杂数据的处理
     */
    @Test
    public void test() {
        String mi = DES.encrypt("", "zxcvbnm");
        System.out.println(mi);
        System.out.println("aaa  " + DES.decrypt("", "zxcvbnm"));
    }
}