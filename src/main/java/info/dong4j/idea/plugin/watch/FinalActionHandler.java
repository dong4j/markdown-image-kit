package info.dong4j.idea.plugin.watch;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-22 19:50
 */
@Slf4j
public class FinalActionHandler extends BaseActionHandler{
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean execute() {
        log.info("上传操作");
        return false;
    }
}
