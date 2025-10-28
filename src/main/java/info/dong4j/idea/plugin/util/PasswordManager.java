package info.dong4j.idea.plugin.util;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ide.passwordSafe.PasswordSafe;

import org.jetbrains.annotations.NotNull;

import lombok.experimental.UtilityClass;

/**
 * 密码管理工具类
 * <p>
 * 提供与密码相关的操作，包括密码的获取和设置。该类封装了密码存储逻辑，通过 {@link PasswordSafe} 实现密码的安全读写，并对密码字符串进行空值处理。
 * <p>
 * 该类为工具类，建议通过静态方法调用，无需实例化。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.18
 * @since 1.6.0
 */
@UtilityClass
public class PasswordManager {
    /**
     * 获取密码
     * <p>
     * 根据提供的凭证属性获取对应的密码值，若密码为空则返回空字符串
     *
     * @param credentialAttributes 凭证属性对象，用于指定密码相关的参数
     * @return 密码值，若为空则返回空字符串
     * @since 1.6.0
     */
    public String getPassword(CredentialAttributes credentialAttributes) {
        String password = PasswordSafe.getInstance().getPassword(credentialAttributes);
        return StringUtils.defaultIfEmpty(password, "");
    }

    /**
     * 设置密码
     * <p>
     * 根据提供的凭证属性和密码，设置密码值。如果密码为空字符串，则设置为空字符串。
     *
     * @param credentialAttributes 凭证属性
     * @param password             密码
     * @since 1.6.0
     */
    public void setPassword(CredentialAttributes credentialAttributes,
                            String password) {
        PasswordSafe.getInstance().setPassword(credentialAttributes,
                                               StringUtils.isNotBlank(password) ? password : "");
    }

    /**
     * 构建凭证属性对象
     * <p>
     * 根据服务名称、密钥和类信息创建并返回一个凭证属性对象
     *
     * @param serviceName 服务名称
     * @param key         密钥
     * @return 凭证属性对象
     * @since 1.6.0
     */
    @NotNull
    public CredentialAttributes buildCredentialAttributes(String serviceName, String key) {
        return new CredentialAttributes(serviceName, key);
    }

}
