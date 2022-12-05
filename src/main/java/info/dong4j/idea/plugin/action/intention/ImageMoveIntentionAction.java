package info.dong4j.idea.plugin.action.intention;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.MarkdownFileFilter;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.task.ActionTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: 将已上传的图片迁移到当前 OSS 或者替换标签</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public final class ImageMoveIntentionAction extends IntentionActionBase {

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
        return MikBundle.message("mik.intention.move.message", clientName);
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
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element)
        throws IncorrectOperationException {
        MarkdownImage markdownImage = this.getMarkdownImage(editor);
        if (markdownImage == null) {
            return;
        }

        OssClient client = this.getClient();
        if (markdownImage.getLocation().name().equals(ImageLocationEnum.LOCAL.name())
            // 如果当前标签所在的图床与设置的图床一样则不处理
            || markdownImage.getPath().contains(client.getCloudType().feature)) {
            return;
        }

        Map<Document, List<MarkdownImage>> waitingForMoveMap = new HashMap<>(1) {
            private static final long serialVersionUID = -3664108591483424778L;

            {
                this.put(editor.getDocument(), new ArrayList<>(1) {
                    private static final long serialVersionUID = 5608817905987286437L;

                    {
                        this.add(markdownImage);
                    }
                });
            }
        };

        EventData data = new EventData()
            .setProject(project)
            .setClient(client)
            .setClientName(this.getName())
            .setWaitingProcessMap(waitingForMoveMap);

        // http://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html
        PropertiesComponent propComp = PropertiesComponent.getInstance();
        propComp.setValue(MarkdownFileFilter.FILTER_KEY, "");

        // 开启后台任务
        try {
            new ActionTask(project,
                           MikBundle.message("mik.action.move.process", this.getName()),
                           ActionManager.buildMoveImageChain(data))
                .queue();
        } catch (Exception ignored) {

        }
    }
}
