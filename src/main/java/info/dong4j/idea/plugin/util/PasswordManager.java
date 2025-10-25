package info.dong4j.idea.plugin.util;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.ide.passwordSafe.PasswordSafe;

import org.jetbrains.annotations.NotNull;

import lombok.experimental.UtilityClass;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.18 13:32
 * @since 1.6.0
 */
@UtilityClass
public class PasswordManager {

    /**
     * Gets password *
     *
     * @param credentialAttributes credential attributes
     * @return the password
     * @since 1.6.0
     */
    public String getPassword(CredentialAttributes credentialAttributes) {
        String password = PasswordSafe.getInstance().getPassword(credentialAttributes);
        return StringUtils.defaultIfEmpty(password, "");
    }

    /**
     * Sets password *
     *
     * @param credentialAttributes credential attributes
     * @param password             password
     * @since 1.6.0
     */
    public void setPassword(CredentialAttributes credentialAttributes,
                            String password) {
        PasswordSafe.getInstance().setPassword(credentialAttributes,
                                               StringUtils.isNotBlank(password) ? password : "");
    }

    /**
     * Build credential attributes
     *
     * @param serviceName service name
     * @param key         key
     * @param clazz       clazz
     * @return the credential attributes
     * @since 1.6.0
     */
    @NotNull
    public CredentialAttributes buildCredentialAttributes(String serviceName, String key, Class<?> clazz) {
        return new CredentialAttributes(serviceName,
                                        key,
                                        clazz);
    }

}
