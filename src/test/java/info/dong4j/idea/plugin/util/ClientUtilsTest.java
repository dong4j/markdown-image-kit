package info.dong4j.idea.plugin.util;

import info.dong4j.idea.plugin.enums.CloudEnum;

import junit.framework.TestCase;

/**
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.17 16:45
 * @since 1.4.0
 */
public class ClientUtilsTest extends TestCase {

    public void test() {
        ClientUtils.getClient(CloudEnum.GITEE);
    }
}
