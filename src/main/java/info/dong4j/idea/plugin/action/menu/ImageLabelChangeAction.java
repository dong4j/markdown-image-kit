package info.dong4j.idea.plugin.action.menu;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.action.intention.IntentionActionBase;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.handler.FinalChainHandler;
import info.dong4j.idea.plugin.chain.handler.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.handler.ParseMarkdownFileHandler;
import info.dong4j.idea.plugin.chain.handler.WriteToDocumentHandler;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ActionUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局替换标签操作类:在目录, 文件和编辑器中生效
 * <p>
 * 该类用于实现全局替换标签的功能，主要处理在特定项目中对Markdown图像标签的替换操作。支持根据配置的标签类型进行替换，并提供相应的处理链。
 * <p>
 * 该类继承自AnAction，用于在IDE中注册为可执行操作，支持更新状态和执行动作。
 * <p>
 * 智能判断功能：在编辑器中通过鼠标右键触发时，会先判断当前光标所在行是否为有效的Markdown图片标签，
 * 如果是则仅处理当前标签，否则处理整个文件。
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
     * 处理更新事件，设置操作可用状态，并显示标签替换图标和提示信息
     * 动态显示当前配置的替换标签类型
     *
     * @param event 事件对象，包含操作上下文信息
     * @since 0.0.1
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        // 检查全局开关
        info.dong4j.idea.plugin.settings.MikState state =
            info.dong4j.idea.plugin.settings.MikPersistenComponent.getInstance().getState();
        if (!state.isEnablePlugin()) {
            event.getPresentation().setEnabled(false);
            return;
        }
        
        // 调用基础的可用性检查
        ActionUtils.isAvailable(true, event, MikIcons.LABEL, MarkdownContents.MARKDOWN_TYPE_NAME);

        // 动态设置菜单标题
        Presentation presentation = event.getPresentation();
        ImageMarkEnum markEnum = IntentionActionBase.getState().getImageMarkEnum();
        if (markEnum != null) {
            presentation.setText(MikBundle.message("mik.action.menu.label.title", markEnum.getText()));
            presentation.setDescription(MikBundle.message("mik.action.menu.label.description", markEnum.getText()));
        } else {
            presentation.setText(MikBundle.message("mik.action.menu.label.default"));
            presentation.setDescription(MikBundle.message("mik.action.menu.label.description.default"));
        }
    }

    /**
     * 处理动作事件，用于解析和替换Markdown文件中的图片标签
     * <p>
     * 该方法接收一个动作事件，根据项目信息初始化处理流程，包括解析Markdown文件、
     * 判断是否需要替换图片标签以及最终写入处理结果。
     * <p>
     * 智能判断：如果是在编辑器中通过鼠标右键触发，会先判断当前光标所在行是否为有效的Markdown图片标签，
     * 如果是则仅处理当前标签，否则处理整个文件。
     *
     * @param event 动作事件对象，包含触发动作的相关信息
     * @since 0.0.1
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        Project project = event.getProject();
        if (project == null) {
            return;
        }

        EventData data = new EventData()
            .setAction("ImageLabelChangeAction")
            .setActionEvent(event)
            .setProject(project);

        // 智能判断：检查光标所在行是否为有效的图片标签
        Map<Document, List<MarkdownImage>> waitingProcessMap = ActionUtils.checkAndGetSingleImageTag(event, project);
        if (waitingProcessMap != null && !waitingProcessMap.isEmpty()) {
            // 如果找到单个图片标签，直接设置到 EventData 中，跳过文件解析步骤
            data.setWaitingProcessMap(waitingProcessMap);
            log.debug("检测到光标所在行为有效的图片标签，仅处理当前标签");
        } else {
            log.debug("未检测到光标所在行为有效的图片标签，将处理整个文件");
        }

        ActionManager actionManager = new ActionManager(data)
            // 解析 markdown 文件（如果 waitingProcessMap 已设置，则跳过解析）
            .addHandler(new ParseMarkdownFileHandler())
            // 全部标签转换
            .addHandler(new ImageLabelChangeHandler())
            // 写入标签
            .addHandler(new WriteToDocumentHandler())
            .addHandler(new FinalChainHandler());

        new ActionTask(project,
                       MikBundle.message("mik.action.change.process"),
                       actionManager).queue();
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
