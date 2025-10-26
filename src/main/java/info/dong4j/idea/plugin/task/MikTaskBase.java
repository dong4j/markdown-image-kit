package info.dong4j.idea.plugin.task;

import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;

import info.dong4j.idea.plugin.chain.ActionManager;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.extern.slf4j.Slf4j;

/**
 * 任务基类
 * <p>
 * 提供任务执行的基础框架，包含任务运行、取消、成功、完成和异常处理等核心逻辑。该类继承自 Task.Backgroundable，
 * 用于封装通用任务行为，支持通过 ActionManager 管理回调逻辑。
 * <p>
 * 该类主要负责任务执行过程中的状态控制和回调通知，适用于需要统一处理任务生命周期的场景。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public abstract class MikTaskBase extends Task.Backgroundable {
    /** 任务操作管理器，用于管理任务相关的操作和行为 */
    private final ActionManager manager;

    /**
     * 初始化 MikTaskBase 对象
     * <p>
     * 用于创建 MikTaskBase 实例，传入项目、标题和操作管理器进行初始化
     *
     * @param project 项目对象，可为空
     * @param title   任务标题，不为空且首字母大写
     * @param manager 操作管理器，不为空
     * @since 0.0.1
     */
    MikTaskBase(@Nullable Project project,
                @Nls(capitalization = Nls.Capitalization.Title) @NotNull String title,
                ActionManager manager) {
        super(project, title);
        this.manager = manager;
    }

    /**
     * 执行任务运行逻辑
     * <p>
     * 该方法用于执行任务的具体运行流程，包括更新进度指示器状态、调用任务管理器执行任务
     * 以及确保在执行完成后恢复进度指示器状态。
     *
     * @param indicator 进度指示器，用于显示任务执行进度和状态
     */
    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        indicator.pushState();
        indicator.setIndeterminate(false);
        try {
            indicator.setFraction(0.0);
            this.manager.invoke(indicator);
        } finally {
            indicator.setFraction(1.0);
            indicator.popState();
        }
    }

    /**
     * 取消操作回调方法
     * <p>
     * 当取消操作发生时调用此方法，用于执行相应的清理或处理逻辑。
     *
     * @since 0.0.1
     */
    @Override
    public void onCancel() {
        log.trace("cancel callback");
    }

    /**
     * 处理任务成功的回调逻辑
     * <p>
     * 当任务执行成功时，记录日志并通知所有注册的回调处理器
     *
     * @since 0.0.1
     */
    @Override
    public void onSuccess() {
        log.trace("success callback");
        for (TaskCallback callback : this.manager.getCallbacks()) {
            callback.onSuccess();
        }
    }

    /**
     * 操作完成后的回调方法
     * <p>
     * 当某个操作执行完毕时触发此方法，用于执行清理或后续处理逻辑
     *
     * @since 0.0.1
     */
    @Override
    public void onFinished() {
        log.trace("finished callback");
    }

    /**
     * 处理异常抛出事件
     * <p>
     * 当发生异常时调用此方法，记录异常信息到日志中
     *
     * @param throwable 抛出的异常对象
     * @since 0.0.1
     */
    @Override
    public void onThrowable(@NotNull Throwable throwable) {
        super.onThrowable(throwable);
        log.trace("", throwable);
    }
}
