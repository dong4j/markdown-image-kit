package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.settings.OssState;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.19 19:56
 * @since 0.0.1
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class WeiboOssState extends OssState {
    /** Username */
    private String username = "";
    /** Secretkey */
    private String secretkey = "";
    /** Cookies */
    private String cookies = "";
}
