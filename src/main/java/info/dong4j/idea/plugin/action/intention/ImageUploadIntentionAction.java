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
 * <p>Description: alt + enter 处理单个标签上传, 完成后替换原有标签 </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public final class ImageUploadIntentionAction extends IntentionActionBase {

    /**
     * Gets message *
     *
     * @param clientName client name
     * @return the message
     * @since 0.0.1
     */
    @NotNull
    @Override
    String getMessage(String clientName) {
        return MikBundle.message("mik.intention.upload.message", clientName);
    }

    /**
     * Invoke
     *
     * @param project project
     * @param editor  editor
     * @param element element
     * @throws IncorrectOperationException incorrect operation exception
     * @since 0.0.1
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
            @Serial
            private static final long serialVersionUID = -1445021799207331254L;

            {
                this.put(editor.getDocument(), new ArrayList<>(1) {
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
        new ActionTask(project,
                       MikBundle.message("mik.action.upload.process", this.getName()),
                       ActionManager.buildUploadChain(data))
            .queue();
    }
}