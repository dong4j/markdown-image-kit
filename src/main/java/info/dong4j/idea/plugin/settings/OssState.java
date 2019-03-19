package info.dong4j.idea.plugin.settings;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-19 18:59
 */
@Data
public abstract class OssState {
    private boolean passedTest = false;
    private Map<String, String> oldAndNewAuthInfo = new HashMap<>(4);
}
