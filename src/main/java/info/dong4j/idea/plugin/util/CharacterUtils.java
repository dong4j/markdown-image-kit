package info.dong4j.idea.plugin.util;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * 字符工具类
 * <p>
 * 提供生成随机字符串的实用方法，适用于需要随机字符生成的场景，如密码生成、验证码生成等。
 * <p>
 * 该类包含两个生成随机字符串的方法：
 * - getRandomString：生成包含大小写字母和数字的随机字符串
 * - getRandomString2：生成包含大小写字母和数字的随机字符串，但字符分布不同
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
public final class CharacterUtils {
    /**
     * 生成指定长度的随机字符串
     * <p>
     * 使用包含大小写字母和数字的字符集，生成指定长度的随机字符串
     *
     * @param length 随机字符串的长度
     * @return 生成的随机字符串
     * @since 0.0.1
     */
    @NotNull
    public static String getRandomString(int length) {
        //定义一个字符串（A-Z，a-z，0-9）即62位；
        String str = "zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
        //由Random生成随机数
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        //长度为几就循环几次
        for (int i = 0; i < length; ++i) {
            //产生0-61的数字
            int number = random.nextInt(62);
            //将产生的数字通过length次承载到sb中
            sb.append(str.charAt(number));
        }
        //将承载的字符转换成字符串
        return sb.toString();
    }

    /**
     * 生成指定长度的随机字符串
     * <p>
     * 该方法会根据给定的长度参数，生成包含大小写字母和数字的随机字符串。
     *
     * @param length 随机字符串的长度
     * @return 生成的随机字符串
     * @since 0.0.1
     */
    @NotNull
    public static String getRandomString2(int length) {
        //产生随机数
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        //循环length次
        for (int i = 0; i < length; i++) {
            //产生0-2个随机数，既与a-z，A-Z，0-9三种可能
            int number = random.nextInt(3);
            long result;
            switch (number) {
                //如果number产生的是数字0；
                case 0:
                    //产生A-Z的ASCII码
                    result = Math.round(Math.random() * 25 + 65);
                    //将ASCII码转换成字符
                    sb.append((char) result);
                    break;
                case 1:
                    //产生a-z的ASCII码
                    result = Math.round(Math.random() * 25 + 97);
                    sb.append((char) result);
                    break;
                case 2:
                    //产生0-9的数字
                    sb.append(new Random().nextInt(10));
                    break;
                default:
            }
        }
        return sb.toString();
    }
}
