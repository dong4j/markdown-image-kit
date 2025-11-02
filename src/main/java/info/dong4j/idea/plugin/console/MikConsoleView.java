package info.dong4j.idea.plugin.console;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

/**
 * MIK Console View
 * <p>
 * 管理插件的控制台视图，用于显示任务处理的详细日志信息
 *
 * @author dong4j
 * @version 2.1.0
 * @date 2025.11.02
 * @since 2.1.0
 */
@Slf4j
@Service(Service.Level.PROJECT)
public final class MikConsoleView {
    /** 工具窗口 ID */
    public static final String TOOL_WINDOW_ID = "MIK Console";
    /** 日期时间格式化 */
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    /** 控制台视图 */
    private ConsoleView consoleView;
    /** 项目实例 */
    private final Project project;

    /**
     * 创建 MikConsoleView 实例
     *
     * @param project 项目实例
     */
    public MikConsoleView(@NotNull Project project) {
        this.project = project;
    }

    /**
     * 初始化控制台视图
     *
     * @return ConsoleView 实例
     */
    public ConsoleView initConsole() {
        if (consoleView == null) {
            consoleView = TextConsoleBuilderFactory.getInstance()
                .createBuilder(project)
                .getConsole();
        }
        return consoleView;
    }

    /**
     * 获取控制台视图
     *
     * @return ConsoleView 实例
     */
    public ConsoleView getConsoleView() {
        if (consoleView == null) {
            initConsole();
        }
        return consoleView;
    }

    /**
     * 打印普通信息
     *
     * @param message 消息内容
     */
    public void print(String message) {
        print(message, ConsoleViewContentType.NORMAL_OUTPUT);
    }

    /**
     * 打印成功信息（绿色）
     *
     * @param message 消息内容
     */
    public void printSuccess(String message) {
        print(message, ConsoleViewContentType.LOG_INFO_OUTPUT);
    }

    /**
     * 打印错误信息（红色）
     *
     * @param message 消息内容
     */
    public void printError(String message) {
        print(message, ConsoleViewContentType.ERROR_OUTPUT);
    }

    /**
     * 打印警告信息（黄色）
     *
     * @param message 消息内容
     */
    public void printWarning(String message) {
        print(message, ConsoleViewContentType.LOG_WARNING_OUTPUT);
    }

    /**
     * 打印消息到控制台
     *
     * @param message     消息内容
     * @param contentType 内容类型
     */
    private void print(String message, ConsoleViewContentType contentType) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                ConsoleView console = getConsoleView();
                if (console != null) {
                    String timestamp = TIME_FORMAT.format(new Date());
                    console.print("[" + timestamp + "] " + message + "\n", contentType);

                    // 自动显示工具窗口
                    showToolWindow();
                }
            } catch (Exception e) {
                log.trace("输出到控制台失败", e);
            }
        });
    }

    /**
     * 显示工具窗口
     */
    private void showToolWindow() {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
                ToolWindow toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
                if (toolWindow != null && !toolWindow.isVisible()) {
                    toolWindow.show(null);
                }
            } catch (Exception e) {
                log.trace("显示工具窗口失败", e);
            }
        });
    }

    /**
     * 清空控制台
     */
    public void clear() {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (consoleView != null) {
                consoleView.clear();
            }
        });
    }

    /**
     * 释放资源
     */
    public void dispose() {
        if (consoleView != null) {
            consoleView.dispose();
            consoleView = null;
        }
    }

    /**
     * 获取项目的 MikConsoleView 实例
     *
     * @param project 项目实例
     * @return MikConsoleView 实例
     */
    @NotNull
    public static MikConsoleView getInstance(@NotNull Project project) {
        return project.getService(MikConsoleView.class);
    }

    // ==================== 静态工具方法 ====================

    /**
     * 检查是否启用控制台日志
     *
     * @return 如果启用返回 true，否则返回 false
     */
    private static boolean isConsoleLogEnabled() {
        try {
            return info.dong4j.idea.plugin.settings.MikPersistenComponent.getInstance().getState().isEnableConsoleLog();
        } catch (Exception e) {
            log.trace("获取控制台日志开关失败，默认启用", e);
            return true;
        }
    }

    /**
     * 输出普通信息到控制台（静态方法）
     *
     * @param project 项目实例（可为 null）
     * @param message 消息内容
     */
    public static void printMessage(Project project, @NotNull String message) {
        if (project == null || !isConsoleLogEnabled()) {
            return;
        }
        try {
            getInstance(project).print(message);
        } catch (Exception e) {
            log.trace("输出到控制台失败", e);
        }
    }

    /**
     * 输出成功信息到控制台（静态方法）
     *
     * @param project 项目实例（可为 null）
     * @param message 消息内容
     */
    public static void printSuccessMessage(Project project, @NotNull String message) {
        if (project == null || !isConsoleLogEnabled()) {
            return;
        }
        try {
            getInstance(project).printSuccess(message);
        } catch (Exception e) {
            log.trace("输出到控制台失败", e);
        }
    }

    /**
     * 输出错误信息到控制台（静态方法）
     *
     * @param project 项目实例（可为 null）
     * @param message 消息内容
     */
    public static void printErrorMessage(Project project, @NotNull String message) {
        if (project == null || !isConsoleLogEnabled()) {
            return;
        }
        try {
            getInstance(project).printError(message);
        } catch (Exception e) {
            log.trace("输出到控制台失败", e);
        }
    }

    /**
     * 输出警告信息到控制台（静态方法）
     *
     * @param project 项目实例（可为 null）
     * @param message 消息内容
     */
    public static void printWarningMessage(Project project, @NotNull String message) {
        if (project == null || !isConsoleLogEnabled()) {
            return;
        }
        try {
            getInstance(project).printWarning(message);
        } catch (Exception e) {
            log.trace("输出到控制台失败", e);
        }
    }

    /**
     * 智能输出信息到控制台（根据消息内容自动选择类型）
     *
     * @param project 项目实例（可为 null）
     * @param message 消息内容
     */
    public static void printSmart(Project project, @NotNull String message) {
        if (project == null || !isConsoleLogEnabled()) {
            return;
        }
        try {
            MikConsoleView consoleView = getInstance(project);
            // 根据消息内容选择合适的输出类型
            if (message.contains("✗") || message.contains("失败") || message.contains("错误")) {
                consoleView.printError(message);
            } else if (message.contains("✓") || message.contains("完成") || message.contains("成功")) {
                consoleView.printSuccess(message);
            } else if (message.contains("警告")) {
                consoleView.printWarning(message);
            } else {
                consoleView.print(message);
            }
        } catch (Exception e) {
            log.trace("输出到控制台失败", e);
        }
    }
}

