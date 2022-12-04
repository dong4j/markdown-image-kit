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
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.18 12:21
 * @since 1.6.0
 */
@Slf4j
public class PasswordManagerTest extends LightPlatformCodeInsightFixtureTestCase {

    /**
     * Storge
     *
     * @param username username
     * @param password password
     * @since 1.6.0
     */
    public void storge(String username, String password) {
        CredentialAttributes credentialAttributes = this.createCredentialAttributes(CredentialAttributesKt.generateServiceName("weibo:",
                                                                                                                               username));
        Credentials credentials = new Credentials(username, password);
        PasswordSafe.getInstance().set(credentialAttributes, credentials);
    }

    /**
     * Gets password *
     *
     * @param username username
     * @return the password
     * @since 1.6.0
     */
    public String getPassword(String username) {
        CredentialAttributes credentialAttributes = this.createCredentialAttributes(username);
        return PasswordSafe.getInstance().getPassword(credentialAttributes);
    }

    /**
     * Create credential attributes
     *
     * @param key key
     * @return the credential attributes
     * @since 1.6.0
     */
    private CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("weibo:", key));
    }

    /**
     * Test 1
     *
     * @since 1.6.0
     */
    public void test_1() {
        this.storge("aaaaa", "bbbbb");
        System.out.println(this.getPassword("aaaaa"));

        CredentialAttributes credentialAttributes = PasswordManager.buildCredentialAttributes(PasswordManagerTest.class.getName(),
                                                                                              "special_key",
                                                                                              PasswordManagerTest.class);


        PasswordManager.setPassword(credentialAttributes, "aaaaa");
        System.out.println(PasswordManager.getPassword(credentialAttributes));
    }
}
