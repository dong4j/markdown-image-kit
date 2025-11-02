package info.dong4j.idea.plugin.console;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import org.jetbrains.annotations.NotNull;

/**
 * MIK Console Tool Window Factory
 * <p>
 * 创建 MIK Console 工具窗口的工厂类
 *
 * @author dong4j
 * @version 2.1.0
 * @date 2025.11.02
 * @since 2.1.0
 */
public class MikConsoleToolWindowFactory implements ToolWindowFactory, DumbAware {
    /**
     * 创建工具窗口内容
     *
     * @param project    项目实例
     * @param toolWindow 工具窗口
     */
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 获取项目级别的 MikConsoleView 服务实例
        MikConsoleView mikConsoleView = MikConsoleView.getInstance(project);
        ConsoleView consoleView = mikConsoleView.initConsole();

        // 创建内容
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(consoleView.getComponent(), "", false);
        toolWindow.getContentManager().addContent(content);

        // 输出欢迎信息
        mikConsoleView.print("======================================================");
        mikConsoleView.print("欢迎使用 Markdown Image Kit");
        mikConsoleView.print("有问题或需求可上交友平台反馈: ");
        mikConsoleView.print("  https://github.com/dong4j/markdown-image-kit/issues");
        mikConsoleView.print("任务处理日志将显示在这里");
        mikConsoleView.print("======================================================");
    }
}

