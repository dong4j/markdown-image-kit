package info.dong4j.test;

import com.intellij.openapi.components.ServiceManager;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-11 21:42
 * @email sjdong3@iflytek.com
 */
public interface HelloApplicationText {
    static HelloApplicationText getInstance() {
        return ServiceManager.getService(HelloApplicationText.class);
    }
}
