package info.dong4j.idea.plugin.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 微博图床上传工具类</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-17 22:11
 */
public final class WeiboUploadUtils extends UploadUtils {
    private static final String PID_PATTERN = "^[a-zA-Z0-9]{32}$";
    private static final String URL_PATTERN = "^(https?://[a-z]{2}d.sinaimg.cn/)(large|bmiddle|mw1024|mw690|small|square|thumb180|thumbnail)(/[a-z0-9]{32}.(jpg|gif))$";

    /**
     * 通过 pid 解析出 url
     *
     * @param pid   the pid
     * @param size  the size
     * @param https the https
     * @return the image url
     */
    @NotNull
    public static String getImageUrl(String pid, String size, boolean https) {
        pid = pid.trim();
        Pattern p = Pattern.compile(PID_PATTERN);
        Matcher m = p.matcher(pid);

        if (m.matches()) {
            System.out.println("匹配了");
            CRC32 crc32 = new CRC32();
            crc32.update(pid.getBytes());
            return (https ? "https" : "http") + "://" + (https ? "ws" : "ww")
                   + ((crc32.getValue() & 3) + 1) + ".sinaimg.cn/" + size
                   + "/" + pid + "." + (pid.charAt(21) == 'g' ? "gif" : "jpg");
        }
        String url = pid;
        Pattern p1 = Pattern.compile(URL_PATTERN);
        Matcher m1 = p1.matcher(url);
        if (m1.find()) {
            return m.group(1) + size + m.group(3);
        }
        return "";
    }
}
