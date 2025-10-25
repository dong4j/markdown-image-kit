package info.dong4j.idea.plugin.task;

import com.intellij.openapi.project.Project;

import info.dong4j.idea.plugin.chain.ActionManager;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: 右键,toolbar 上传到指定 OSS</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public class ActionTask extends MikTaskBase {

    /**
     * Action task
     *
     * @param project project
     * @param title   title
     * @param manager manager
     * @since 0.0.1
     */
    public ActionTask(@Nullable Project project,
                      @Nls(capitalization = Nls.Capitalization.Title) @NotNull String title,
                      ActionManager manager) {
        super(project, title, manager);
    }
}
