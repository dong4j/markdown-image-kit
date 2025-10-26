//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package info.dong4j.idea.plugin.util.digest;

/**
 * HMAC 算法枚举类
 * <p>
 * 定义常用的 HMAC 加密算法类型，用于指定加密过程中使用的 HMAC 算法。
 * 包含 HMAC_MD5、HMAC_SHA_1、HMAC_SHA_256、HMAC_SHA_384 和 HMAC_SHA_512 等算法。
 * 每个枚举值对应一个具体的算法名称，可用于加密或签名操作。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.14
 * @since 1.1.0
 */
public enum HmacAlgorithms {
    /** Hmac MD5 算法，用于生成消息认证码 */
    HMAC_MD5("HmacMD5"),
    /** Hmac SHA 1 算法，用于生成 HMAC-SHA1 哈希值 */
    HMAC_SHA_1("HmacSHA1"),
    /** HMAC-SHA256 算法，用于生成 Hmac SHA 256 加密结果 */
    HMAC_SHA_256("HmacSHA256"),
    /** HMAC SHA384 算法名称 */
    HMAC_SHA_384("HmacSHA384"),
    /** HMAC-SHA-512 算法，用于生成安全的哈希值 */
    HMAC_SHA_512("HmacSHA512");
    /** 加密算法类型 */
    private final String algorithm;

    /**
     * 构造一个Hmac算法对象
     * <p>
     * 根据指定的算法名称初始化Hmac算法实例
     *
     * @param algorithm 算法名称，用于标识使用的Hmac算法
     * @since 1.1.0
     */
    HmacAlgorithms(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * 返回对象的字符串表示形式
     * <p>
     * 该方法重写 Object 类的 toString 方法，返回算法名称字符串
     *
     * @return 算法名称字符串
     * @since 1.1.0
     */
    @Override
    public String toString() {
        return this.algorithm;
    }
}
