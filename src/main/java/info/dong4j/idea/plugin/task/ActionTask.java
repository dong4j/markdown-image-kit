package info.dong4j.idea.plugin.task;

import com.intellij.openapi.project.Project;

import info.dong4j.idea.plugin.chain.ActionManager;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.extern.slf4j.Slf4j;

/**
 * 右键菜单或工具栏上传到指定 OSS 的任务类
 * <p>
 * 该类用于封装执行上传文件到 OSS 的具体任务逻辑，继承自 MikTaskBase，提供与项目、标题和操作管理器相关的初始化功能。
 * 主要用于在用户触发右键菜单或工具栏操作时，执行上传操作到指定的 OSS 服务。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public class ActionTask extends MikTaskBase {
    /**
     * 初始化一个 ActionTask 实例
     * <p>
     * 通过传入的项目、标题和管理器创建一个新的 ActionTask 对象
     *
     * @param project 项目对象，可为空
     * @param title   标题，非空且首字母大写
     * @param manager 管理器对象，用于管理操作
     * @since 0.0.1
     */
    public ActionTask(@Nullable Project project,
                      @Nls(capitalization = Nls.Capitalization.Title) @NotNull String title,
                      ActionManager manager) {
        super(project, title, manager);
    }
}
