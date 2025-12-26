package info.dong4j.idea.plugin.action.intention;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.handler.FinalChainHandler;
import info.dong4j.idea.plugin.chain.handler.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.handler.WriteToDocumentHandler;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.task.ActionTask;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import icons.MikIcons;

/**
 * 替换标签意图操作类
 * <p>
 * 该类用于处理图像标签替换意图相关的操作，主要负责在特定条件下触发图像标签的替换逻辑，支持在编辑器中执行图像标记类型的转换操作。
 * <p>
 * 该类继承自 IntentionActionBase，实现了意图操作的通用方法，包括判断意图是否可用、获取提示信息以及执行意图操作。
 * <p>
 * 在执行意图时，会检查当前选中的 Markdown 图像是否为远程图像，如果是则进行替换操作，否则直接返回。
 * <p>
 * 该类使用了事件驱动的设计模式，通过构建事件数据和处理链来完成图像标签的替换流程。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
public class ImageLabelChangeIntetionAction extends IntentionActionBase {
    /**
     * 获取图标
     * <p>
     * 返回标签替换的图标
     *
     * @param flags 图标标志
     * @return 图标对象
     * @since 2.2.0
     */
    @Override
    public Icon getIcon(@IconFlags int flags) {
        return MikIcons.LABEL;
    }

    /**
     * 获取指定客户端名称的消息内容
     * <p>
     * 根据传入的客户端名称返回对应的消息内容
     *
     * @param clientName 客户端名称
     * @return 消息内容
     * @since 0.0.1
     */
    @NotNull
    @Override
    String getMessage(String clientName) {
        // 安全地获取状态，避免在类初始化阶段访问服务
        MikState state = getStateSafely();
        if (state == null) {
            // 如果服务不可用（可能在类初始化阶段），返回默认消息
            return "正在初始化...";
        }
        ImageMarkEnum imageMarkEnum = state.getImageMarkEnum();
        return MikBundle.message("mik.intention.change.message", imageMarkEnum.getText());
    }

    /**
     * 判断当前元素是否可用
     * <p>
     * 检查项目和编辑器上下文，结合状态是否为转换为HTML标签来判断元素是否可用
     *
     * @param project 项目对象
     * @param editor  编辑器对象
     * @param element 要检查的元素
     * @return 元素是否可用
     * @since 0.0.1
     */
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return super.isAvailable(project, editor, element) && getState().isChangeToHtmlTag();
    }

    /**
     * 执行图像移动操作
     * <p>
     * 根据当前编辑器中的Markdown图像信息，执行图像移动的处理逻辑。如果图像位置为本地，则直接返回；否则，创建等待处理的映射关系，并通过后台任务执行图像标签替换和最终处理流程。
     *
     * @param project 项目对象，用于获取当前编辑环境
     * @param editor  编辑器对象，用于获取当前文档和光标位置
     * @param element 当前选中的Psi元素，用于定位图像信息
     * @throws IncorrectOperationException 当操作不正确时抛出异常
     */
    @Override
    public void execute(@NotNull Project project, Editor editor, @NotNull PsiElement element)
        throws IncorrectOperationException {

        MarkdownImage markdownImage = this.getMarkdownImage(editor);
        if (markdownImage.getLocation() == ImageLocationEnum.LOCAL) {
            return;
        }

        // 手动设置为 null, 后面才能替换
        markdownImage.setImageMarkType(null);
        final Map<Document, List<MarkdownImage>> waitingForMoveMap = createProcessData(editor, markdownImage);

        EventData data = new EventData()
            .setAction("ImageLabelChangeIntetionAction")
            .setProject(project)
            .setWaitingProcessMap(waitingForMoveMap);

        ActionManager actionManager = new ActionManager(data)
            .addHandler(new ImageLabelChangeHandler())
            // 写入标签
            .addHandler(new WriteToDocumentHandler())
            .addHandler(new FinalChainHandler());

        // 开启后台任务
        try {
            new ActionTask(project, MikBundle.message("mik.action.change.process", this.getName()),
                           actionManager).queue();
        } catch (Exception ignored) {
        }
    }
}
