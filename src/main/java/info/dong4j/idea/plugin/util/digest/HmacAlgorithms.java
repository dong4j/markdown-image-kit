//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package info.dong4j.idea.plugin.util.digest;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 23:45
 * @since 1.1.0
 */
public enum HmacAlgorithms {
    /** Hmac md 5 hmac algorithms */
    HMAC_MD5("HmacMD5"),
    /** Hmac sha 1 hmac algorithms */
    HMAC_SHA_1("HmacSHA1"),
    /** Hmac sha 256 hmac algorithms */
    HMAC_SHA_256("HmacSHA256"),
    /** Hmac sha 384 hmac algorithms */
    HMAC_SHA_384("HmacSHA384"),
    /** Hmac sha 512 hmac algorithms */
    HMAC_SHA_512("HmacSHA512");

    /** Algorithm */
    private final String algorithm;

    /**
     * Hmac algorithms
     *
     * @param algorithm algorithm
     * @since 1.1.0
     */
    HmacAlgorithms(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * To string
     *
     * @return the string
     * @since 1.1.0
     */
    @Override
    public String toString() {
        return this.algorithm;
    }
}
