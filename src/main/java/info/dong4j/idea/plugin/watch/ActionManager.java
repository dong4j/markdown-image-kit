package info.dong4j.idea.plugin.watch;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-22 18:50
 */
public class ActionManager {
    private List<IActionHandler> handlersChain = new LinkedList<>();

    public ActionManager addHandler(IActionHandler handler){
        handlersChain.add(handler);
        return this;
    }

    public void invoke(){
        for(IActionHandler handler : handlersChain){
            if(handler.isEnabled()){
                handler.execute();
            }
        }
    }
}
