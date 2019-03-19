package info.dong4j.idea.plugin.settings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @date 2019-03-19 19:56
 * @email sjdong3@iflytek.com
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class WeiboOssState extends OssState{
    private String userName = "";
    private String password = "";
    private String cookies = "";
}