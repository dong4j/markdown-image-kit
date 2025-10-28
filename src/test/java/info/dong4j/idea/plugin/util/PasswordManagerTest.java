package info.dong4j.idea.plugin.util;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;

import lombok.extern.slf4j.Slf4j;

/**
 * 密码管理测试类
 * <p>
 * 用于测试密码存储和获取功能，包含存储密码、获取密码以及测试方法。
 * 该类通过调用 PasswordSafe 和 PasswordManager 相关方法实现密码的存储与读取操作。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.18
 * @since 1.6.0
 */
@Slf4j
public class PasswordManagerTest {
    /**
     * 存储用户凭证信息
     * <p>
     * 将用户提供的用户名和密码存储到密码安全系统中
     *
     * @param username 用户名
     * @param password 密码
     * @since 1.6.0
     */
    public void storge(String username, String password) {
        CredentialAttributes credentialAttributes = this.createCredentialAttributes(CredentialAttributesKt.generateServiceName("weibo:",
                                                                                                                               username));
        Credentials credentials = new Credentials(username, password);
        PasswordSafe.getInstance().set(credentialAttributes, credentials);
    }

    /**
     * 根据用户名获取用户密码
     * <p>
     * 通过用户名查找对应的凭证属性，并使用密码安全工具获取密码
     *
     * @param username 用户名
     * @return 用户密码
     * @since 1.6.0
     */
    public String getPassword(String username) {
        CredentialAttributes credentialAttributes = this.createCredentialAttributes(username);
        return PasswordSafe.getInstance().getPassword(credentialAttributes);
    }

    /**
     * 创建凭证属性对象
     * <p>
     * 根据给定的 key 生成对应的凭证属性对象
     *
     * @param key 唯一标识符，用于生成服务名称
     * @return 凭证属性对象
     * @since 1.6.0
     */
    private CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("weibo:", key));
    }

    /**
     * 执行测试用例 1
     * <p>
     * 该方法用于测试密码相关功能，包括存储密码、获取密码以及构建和使用凭证属性。
     *
     * @since 1.6.0
     */
    public void test_1() {
        this.storge("aaaaa", "bbbbb");
        System.out.println(this.getPassword("aaaaa"));

        CredentialAttributes credentialAttributes = PasswordManager.buildCredentialAttributes(PasswordManagerTest.class.getName(),
                                                                                              "special_key");


        PasswordManager.setPassword(credentialAttributes, "aaaaa");
        System.out.println(PasswordManager.getPassword(credentialAttributes));
    }
}
