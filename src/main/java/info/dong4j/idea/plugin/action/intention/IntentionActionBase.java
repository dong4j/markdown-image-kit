package info.dong4j.idea.plugin.action.intention;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.MarkdownUtils;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.extern.slf4j.Slf4j;

/**
 * 基础意图操作类
 * <p>
 * 该类作为所有意图操作的基类，提供与 OSS 客户端相关的基础方法，包括获取默认客户端、客户端名称、客户端类型等。主要用于支持 Markdown 图片上传功能的意图操作。
 * <p>
 * 该类通过继承 PsiElementBaseIntentionAction 实现 IntelliJ IDEA 的意图操作机制，用于在 Markdown 文件中识别并处理图片上传相关的意图。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public abstract class IntentionActionBase extends PsiElementBaseIntentionAction {
    /**
     * 获取当前组件的状态信息
     * <p>
     * 在需要时按需获取服务实例，而不是在类初始化时获取
     *
     * @return 当前组件的状态信息
     */
    public static MikState getState() {
        return MikPersistenComponent.getInstance().getState();
    }

    /**
     * 获取消息内容
     * <p>
     * 根据客户端名称获取对应的消息内容
     *
     * @param clientName 客户端名称
     * @return 消息内容
     * @since 0.0.1
     */
    abstract String getMessage(String clientName);

    /**
     * 使用设置的默认 client
     * <p>
     * 通过云类型获取对应的 OSS 客户端实例
     *
     * @return OSS 客户端对象
     * @since 0.0.1
     */
    protected OssClient getClient() {
        return ClientUtils.getClient(this.getCloudType());
    }

    /**
     * 获取设置的默认客户端名称
     * <p>
     * 返回当前配置中设置的默认客户端名称。
     *
     * @return 默认客户端名称
     * @since 0.0.1
     */
    protected String getName() {
        return this.getCloudType().title;
    }

    /**
     * 获取设置的默认客户端类型，如果设置的不可用，则使用 sm.ms
     * <p>
     * 通过检查设置的客户端类型是否可用，若不可用则返回默认的 sm.ms 客户端类型
     *
     * @return 默认的云服务类型枚举
     * @since 0.0.1
     */
    protected CloudEnum getCloudType() {
        CloudEnum cloudEnum = OssState.getCloudType(getState().getCloudType());
        return OssState.getStatus(cloudEnum) ? cloudEnum : CloudEnum.SM_MS_CLOUD;
    }

    /**
     * 获取对应的文本信息
     * <p>
     * 根据当前云类型获取对应的文本内容
     *
     * @return 文本内容
     * @since 0.0.1
     */
    @Nls
    @NotNull
    @Override
    public String getText() {
        return this.getMessage(this.getCloudType().title);
    }

    /**
     * 获取家庭名称
     * <p>
     * 返回当前对象对应的家庭名称文本
     *
     * @return 家庭名称
     * @since 0.0.1
     */
    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return this.getText();
    }

    /**
     * 判断当前光标位置是否位于Markdown文件中
     * <p>
     * 通过检查光标所在行是否包含非法图片标记，以及文件是否为Markdown文件来判断
     *
     * @param project 项目对象
     * @param editor  编辑器对象
     * @param element 光标所在的PsiElement对象
     * @return 如果当前光标位置位于Markdown文件中，返回true；否则返回false
     * @since 0.0.1
     */
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor,
                               @NotNull PsiElement element) {


        if (MarkdownUtils.illegalImageMark(project, this.getLineText(editor))) {
            return false;
        }

        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (virtualFile == null) {
            return false;
        }

        return MarkdownUtils.isMardownFile(virtualFile);
    }

    /**
     * 获取光标所在行的文本内容
     * <p>
     * 通过获取光标位置所在的行号，计算该行的起始和结束偏移量，最终提取出该行的文本内容。
     *
     * @param editor 编辑器实例，用于获取文档和光标位置信息
     * @return 光标所在行的文本内容
     * @since 0.0.1
     */
    private String getLineText(@NotNull Editor editor) {
        int documentLine = editor.getDocument().getLineNumber(editor.getCaretModel().getOffset());
        int linestartoffset = editor.getDocument().getLineStartOffset(documentLine);
        int lineendoffset = editor.getDocument().getLineEndOffset(documentLine);

        log.trace("documentLine = {}, linestartoffset = {}, lineendoffset = {}", documentLine, linestartoffset, lineendoffset);

        String text = editor.getDocument().getText(new TextRange(linestartoffset, lineendoffset));
        log.trace("text = {}", text);
        return text;
    }

    /**
     * 获取Markdown格式的图片信息
     * <p>
     * 根据当前编辑器内容分析并返回Markdown图片信息
     *
     * @param editor 编辑器实例
     * @return Markdown图片信息，可能为null
     * @since 0.0.1
     */
    @Nullable
    MarkdownImage getMarkdownImage(Editor editor) {
        Document document = editor.getDocument();
        int documentLine = document.getLineNumber(editor.getCaretModel().getOffset());
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);

        return MarkdownUtils.analysisImageMark(virtualFile, this.getLineText(editor), documentLine);
    }
}
