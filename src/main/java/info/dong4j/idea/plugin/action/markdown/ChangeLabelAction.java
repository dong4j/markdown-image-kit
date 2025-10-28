package info.dong4j.idea.plugin.action.markdown;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.action.intention.IntentionActionBase;
import info.dong4j.idea.plugin.chain.ActionHandlerAdapter;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.FinalChainHandler;
import info.dong4j.idea.plugin.chain.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.ReplaceToDocument;
import info.dong4j.idea.plugin.chain.ResolveMarkdownFileHandler;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ActionUtils;
import info.dong4j.idea.plugin.util.ParserUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局替换标签操作类
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
public final class ChangeLabelAction extends AnAction {
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
                .setActionEvent(event)
                .setProject(project);

            ActionManager actionManager = new ActionManager(data)
                // 解析 markdown 文件
                .addHandler(new ResolveMarkdownFileHandler())
                // 全部标签转换
                .addHandler(new ActionHandlerAdapter() {
                    /**
                     * 获取操作替换动作的标签文本
                     * <p>
                     * 返回与 "mik.action.replace.label" 关键字关联的国际化消息文本
                     *
                     * @return 操作替换动作的标签文本
                     */
                    @Override
                    public String getName() {
                        return MikBundle.message("mik.action.replace.label");
                    }

                    /**
                     * 处理Markdown图片的标签替换逻辑
                     * <p>
                     * 根据配置判断是否需要替换图片标签。如果图片位置为本地类型，则不进行替换；如果未开启标签替换开关，则替换为原始标签；否则根据配置的标签类型进行替换。
                     *
                     * @param data          事件数据
                     * @param imageIterator 图片迭代器
                     * @param markdownImage 当前Markdown图片对象
                     */
                    @Override
                    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
                        // 如果是本地类型, 则不替换
                        if (markdownImage.getLocation().equals(ImageLocationEnum.LOCAL)) {
                            return;
                        }

                        // 如果没有勾选 标签替换开关, 则全部替换为原始标签
                        if (!IntentionActionBase.getState().isChangeToHtmlTag()) {
                            markdownImage.setFinalMark(ParserUtils.parse2(ImageMarkEnum.ORIGINAL.code,
                                                                          markdownImage.getTitle(),
                                                                          markdownImage.getPath()));
                            return;
                        }

                        ImageMarkEnum currentMarkType = markdownImage.getImageMarkType();
                        if (!IntentionActionBase.getState().getTagType().equals(currentMarkType.text)) {
                            ImageLabelChangeHandler.change(markdownImage);
                        }
                    }
                })
                // 写入标签
                .addHandler(new ReplaceToDocument())
                .addHandler(new FinalChainHandler());

            new ActionTask(project,
                           MikBundle.message("mik.action.change.process"),
                           actionManager).queue();
        }
    }
}
