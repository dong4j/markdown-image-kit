package info.dong4j.test;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import org.jetbrains.annotations.NotNull;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 扩展 IDEA 提供的菜单栏</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-11 20:45
 */
@Slf4j
public class HelloAction extends AnAction {
    public HelloAction() {
        super("Hello");
    }

    /**
     * 响应用户的点击事件
     *
     * @param event the event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        log.info(event.toString());
        Project project = event.getProject();
        Messages.showMessageDialog(project,
                                   "Hello world!",
                                   "Greeting",
                                   Messages.getInformationIcon());
    }
}
