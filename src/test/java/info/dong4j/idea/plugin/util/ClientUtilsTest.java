package info.dong4j.idea.plugin.util;

import info.dong4j.idea.plugin.enums.CloudEnum;

import junit.framework.TestCase;

/**
 * 客户端工具类测试类
 * <p>
 * 用于测试 ClientUtils 工具类中 getClient 方法的功能，验证不同云平台枚举值对应的客户端实例创建逻辑。
 *
 * @author dong4j
 * @version 1.0.0
 * @email mailto:dong4j@gmail.com
 * @date 2021.02.17
 * @since 1.4.0
 */
public class ClientUtilsTest extends TestCase {

    /**
     * 测试方法，用于演示如何通过 CloudEnum 获取客户端实例
     * <p>
     * 该方法调用 ClientUtils.getClient 方法，并传入 CloudEnum.GITEE 枚举值作为参数
     */
    public void test() {
        ClientUtils.getClient(CloudEnum.GITEE);
    }
}
