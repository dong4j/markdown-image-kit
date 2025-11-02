package info.dong4j.idea.plugin.action.menu;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.FinalChainHandler;
import info.dong4j.idea.plugin.chain.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.ReplaceToDocument;
import info.dong4j.idea.plugin.chain.ResolveMarkdownFileHandler;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ActionUtils;

import org.jetbrains.annotations.NotNull;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局替换标签操作类(右键菜单)
 * <p>
 * 该类用于实现全局替换标签的功能，主要处理在特定项目中对Markdown图像标签的替换操作。支持根据配置的标签类型进行替换，并提供相应的处理链。
 * <p>
 * 该类继承自AnAction，用于在IDE中注册为可执行操作，支持更新状态和执行动作。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public final class ImageLabelChangeAction extends AnAction {
    /**
     * 更新操作
     * <p>
     * 处理更新事件，设置操作可用状态，并显示相关图标和提示信息
     *
     * @param event 事件对象，包含操作上下文信息
     * @since 0.0.1
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        ActionUtils.isAvailable(true, event, MikIcons.MIK, MarkdownContents.MARKDOWN_TYPE_NAME);
    }

    /**
     * 处理动作事件，用于解析和替换Markdown文件中的图片标签
     * <p>
     * 该方法接收一个动作事件，根据项目信息初始化处理流程，包括解析Markdown文件、
     * 判断是否需要替换图片标签以及最终写入处理结果。
     *
     * @param event 动作事件对象，包含触发动作的相关信息
     * @since 0.0.1
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        Project project = event.getProject();
        if (project != null) {

            EventData data = new EventData()
                .setAction("ImageLabelChangeAction")
                .setActionEvent(event)
                .setProject(project);

            ActionManager actionManager = new ActionManager(data)
                // 解析 menu 文件
                .addHandler(new ResolveMarkdownFileHandler())
                // 全部标签转换
                .addHandler(new ImageLabelChangeHandler())
                // 写入标签
                .addHandler(new ReplaceToDocument())
                .addHandler(new FinalChainHandler());

            new ActionTask(project,
                           MikBundle.message("mik.action.change.process"),
                           actionManager).queue();
        }
    }

    /**
     * 获取动作更新线程
     *
     * <p>指定 update 方法在后台线程中执行，避免阻塞事件调度线程(EDT)。
     * 提高 UI 响应性，防止界面卡顿。
     *
     * @return ActionUpdateThread.BGT 后台线程
     * @see ActionUpdateThread#BGT
     */
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        // 在后台线程中执行 update，避免阻塞 EDT
        return ActionUpdateThread.BGT;
    }

}
