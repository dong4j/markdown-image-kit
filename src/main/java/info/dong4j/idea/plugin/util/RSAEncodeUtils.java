package info.dong4j.idea.plugin.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @date 2019-03-17 23:22
 * @email sjdong3@iflytek.com
 */
public class RSAEncodeUtils {
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
                                            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final String RSA_ALGORITHM = "RSA";

    @NotNull
    public static String encode(String toEncode, String pubKey, String pubExp)
        throws NoSuchAlgorithmException, NoSuchPaddingException,
               InvalidKeySpecException, InvalidKeyException, BadPaddingException,
               IllegalBlockSizeException {

        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        BigInteger modulus = new BigInteger(pubKey, 16);
        BigInteger publicExponent = new BigInteger(pubExp, 16);
        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        PublicKey publicKey = keyFactory.generatePublic(rsaPublicKeySpec);
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encodeStr = cipher.doFinal(toEncode.getBytes());
        return bytesToHex(encodeStr);
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    private static String bytesToHex(byte[] bytes) {
        char[] buf = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes) {
            buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
            buf[index++] = HEX_CHAR[b & 0xf];
        }
        return new String(buf);
    }
}
