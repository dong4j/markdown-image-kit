package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.settings.OssState;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 微博OSS状态实体类
 * <p>
 * 用于表示微博平台与OSS服务交互时的状态信息，包含用户名、密钥和Cookies等关键数据。
 * 该类继承自OssState，扩展了微博特有的状态字段。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2019.03.19
 * @since 0.0.1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class WeiboOssState extends OssState {
    /** 用户名 */
    private String username = "";
    /** Secretkey 用于加密或身份验证的密钥 */
    private String secretkey = "";
    /** Cookies */
    private String cookies = "";
}
