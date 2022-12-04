/*
 * MIT License
 *
 * Copyright (c) 2022 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
