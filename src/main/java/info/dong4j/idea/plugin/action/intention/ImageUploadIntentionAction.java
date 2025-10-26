package info.dong4j.idea.plugin.action.intention;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.task.ActionTask;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 图像上传意图操作类
 * <p>
 * 用于处理图像上传的意图操作，主要负责在用户触发上传操作时，识别并处理Markdown图像标签，执行上传流程。
 * 该类继承自IntentionActionBase，实现了特定的上传逻辑，包括获取提示信息、执行上传操作等。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public final class ImageUploadIntentionAction extends IntentionActionBase {
    /**
     * 获取上传提示信息
     * <p>
     * 根据客户端名称返回对应的上传提示信息
     *
     * @param clientName 客户端名称
     * @return 上传提示信息
     * @since 0.0.1
     */
    @NotNull
    @Override
    String getMessage(String clientName) {
        return MikBundle.message("mik.intention.upload.message", clientName);
    }

    /**
     * 执行上传图片的后台任务
     * <p>
     * 该方法用于检测当前编辑器中的Markdown图片标记，若图片来源为网络，则触发上传流程。
     * 通过创建后台任务，将图片信息和相关上下文传递给上传处理链进行处理。
     *
     * @param project 项目对象，用于获取项目上下文信息
     * @param editor  编辑器对象，用于获取当前编辑器的文档信息
     * @param element 当前选中的PsiElement对象，用于定位图片标记
     * @throws IncorrectOperationException 当操作不正确时抛出异常
     */
    @Override
    public void invoke(@NotNull Project project,
                       Editor editor,
                       @NotNull PsiElement element) throws IncorrectOperationException {

        MarkdownImage matchImageMark = this.getMarkdownImage(editor);
        if (matchImageMark == null) {
            return;
        }

        if (ImageLocationEnum.NETWORK.name().equals(matchImageMark.getLocation().name())) {
            return;
        }

        Map<Document, List<MarkdownImage>> waitingForMoveMap = new HashMap<>(1) {
            /** 序列化版本号，用于确保类的兼容性 */
            @Serial
            private static final long serialVersionUID = -1445021799207331254L;

            {
                this.put(editor.getDocument(), new ArrayList<>(1) {
                    /** 序列化版本号，用于确保类的兼容性 */
                    @Serial
                    private static final long serialVersionUID = 4482739561378065459L;

                    {
                        this.add(matchImageMark);
                    }
                });
            }
        };

        EventData data = new EventData()
            .setProject(project)
            .setClientName(this.getName())
            .setClient(this.getClient())
            .setWaitingProcessMap(waitingForMoveMap);

        // 开启后台任务
        try {
            new ActionTask(project,
                           MikBundle.message("mik.action.upload.process", this.getName()),
                           ActionManager.buildUploadChain(data))
                .queue();
        } catch (Exception ignored) {
        }
    }
}