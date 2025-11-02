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

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 图片迁移意图操作类
 * <p>
 * 用于处理将已上传的图片迁移到当前 OSS 或者替换标签的意图操作。该类继承自 IntentionActionBase，
 * 提供了图片迁移的具体实现逻辑，包括获取提示信息、执行迁移操作等。
 * <p>
 * 该类主要在 Markdown 编辑器中使用，当用户触发图片迁移意图时，会调用该类的 invoke 方法执行迁移操作。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public final class ImageMigrationIntentionAction extends IntentionActionBase {
    /**
     * 获取移动意图提示信息
     * <p>
     * 根据客户端名称返回对应的移动意图提示信息
     *
     * @param clientName 客户端名称
     * @return 移动意图提示信息
     * @since 0.0.1
     */
    @NotNull
    @Override
    String getMessage(String clientName) {
        return MikBundle.message("mik.intention.move.message", clientName);
    }

    /**
     * 执行图片迁移操作
     * <p>
     * 根据当前编辑器中的Markdown图片信息，判断是否需要迁移图片至指定图床，并启动后台任务进行处理。
     *
     * @param project 项目对象，用于获取项目相关资源
     * @param editor  编辑器对象，用于获取当前编辑器文档和光标位置
     * @param element 当前选中的PsiElement对象，用于定位图片信息
     * @throws IncorrectOperationException 当操作不正确时抛出异常
     */
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element)
        throws IncorrectOperationException {
        MarkdownImage markdownImage = this.getMarkdownImage(editor);
        OssClient client = this.getClient();

        if (markdownImage == null
            || markdownImage.getLocation() == ImageLocationEnum.LOCAL
            || markdownImage.getPath().contains(client.getCloudType().feature)) {
            return;
        }

        final Map<Document, List<MarkdownImage>> waitingForMoveMap = createProcessData(editor, markdownImage);

        EventData data = new EventData()
            .setAction("ImageMigrationIntentionAction")
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
