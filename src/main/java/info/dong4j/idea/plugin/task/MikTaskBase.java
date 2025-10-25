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
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public abstract class MikTaskBase extends Task.Backgroundable {

    /**
     * Instantiates a new Mik task base.
     *
     * @param project the project
     * @param title the title
     */
    private final ActionManager manager;

    /**
     * Mik task base
     *
     * @param project project
     * @param title   title
     * @param manager manager
     * @since 0.0.1
     */
    MikTaskBase(@Nullable Project project,
                @Nls(capitalization = Nls.Capitalization.Title) @NotNull String title,
                ActionManager manager) {
        super(project, title);
        this.manager = manager;
    }


    /**
     * Run
     *
     * @param indicator indicator
     * @since 0.0.1
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
     * On cancel
     *
     * @since 0.0.1
     */
    @Override
    public void onCancel() {
        log.trace("cancel callback");
    }

    /**
     * On success
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
     * On finished
     *
     * @since 0.0.1
     */
    @Override
    public void onFinished() {
        log.trace("finished callback");
    }

    /**
     * On throwable
     *
     * @param throwable throwable
     * @since 0.0.1
     */
    @Override
    public void onThrowable(@NotNull Throwable throwable) {
        super.onThrowable(throwable);
        log.trace("", throwable);
    }
}
