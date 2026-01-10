package info.dong4j.idea.plugin.action.intention;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

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

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

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
public abstract class IntentionActionBase extends PsiElementBaseIntentionAction implements Iconable {
    /**
     * 获取当前组件的状态信息
     * <p>
     * 在需要时按需获取服务实例，而不是在类初始化时获取
     *
     * @return 当前组件的状态信息，如果服务不可用则返回 null
     */
    @Deprecated
    public static MikState getState() {
        return MikPersistenComponent.getInstance().getState();
    }

    /**
     * 安全地获取当前组件的状态信息
     * <p>
     * 在类初始化阶段，服务可能尚未可用，此方法会检查服务是否可用后再获取状态
     *
     * @return 当前组件的状态信息，如果服务不可用则返回 null
     */
    @Nullable
    protected static MikState getStateSafely() {
        try {
            Application application = ApplicationManager.getApplication();
            if (application == null || application.isDisposed()) {
                return null;
            }
            return MikPersistenComponent.getInstance().getState();
        } catch (Exception e) {
            // 在类初始化阶段，服务可能尚未可用，返回 null
            log.debug("无法获取服务状态，可能处于类初始化阶段: {}", e.getMessage());
            return null;
        }
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
     * 获取图标
     * <p>
     * 子类可以重写此方法来提供自定义图标
     *
     * @param flags 图标标志
     * @return 图标对象，默认返回 null
     * @since 2.2.0
     */
    @Nullable
    @Override
    public Icon getIcon(@IconFlags int flags) {
        return null;
    }

    /**
     * 生成意图操作预览
     * <p>
     * 由于该意图操作涉及网络请求、文件上传等副作用操作，不适合在预览模式下执行。
     * 返回 EMPTY 表示不提供预览功能。
     *
     * @param project 项目对象
     * @param editor  编辑器对象
     * @param file    当前文件
     * @return 返回 EMPTY 表示不支持预览
     * @since 2.2.0
     */
    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project,
                                                         @NotNull Editor editor,
                                                         @NotNull PsiFile file) {
        // 返回 EMPTY 表示不支持预览功能
        // 这样可以避免在预览模式下访问设置、执行网络请求等产生副作用的操作
        return IntentionPreviewInfo.EMPTY;
    }

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
        return this.getCloudType().getTitle();
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
        // 如果处于预览模式，返回默认值，避免访问设置和状态
        if (IntentionPreviewUtils.isIntentionPreviewActive()) {
            return CloudEnum.SM_MS_CLOUD;
        }
        // 安全地获取状态，避免在类初始化阶段访问服务
        MikState state = getStateSafely();
        if (state == null) {
            // 如果服务不可用（可能在类初始化阶段），返回默认值
            return CloudEnum.SM_MS_CLOUD;
        }
        CloudEnum cloudEnum = OssState.getCloudType(state.getDefaultCloudType());
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
        // 如果处于预览模式，返回简单的文本，避免访问设置和状态
        if (IntentionPreviewUtils.isIntentionPreviewActive()) {
            return "Configure image";
        }
        // 安全地获取云类型，避免在类初始化阶段访问服务
        try {
            CloudEnum cloudType = this.getCloudType();
            return this.getMessage(cloudType.getTitle());
        } catch (Exception e) {
            // 如果服务不可用（可能在类初始化阶段），返回默认文本
            log.debug("无法获取云类型，返回默认文本: {}", e.getMessage());
            return "Configure image";
        }
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
        // 如果处于预览模式，返回简单的文本，避免访问设置和状态
        if (IntentionPreviewUtils.isIntentionPreviewActive()) {
            return "Configure image";
        }
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

        // 如果处于预览模式，则直接返回 false，避免在预览阶段执行会产生副作用的操作
        if (IntentionPreviewUtils.isPreviewElement(element)) {
            return false;
        }

        // 检查全局开关，安全地获取状态
        MikState state = getStateSafely();
        if (state == null || !state.isEnablePlugin()) {
            return false;
        }

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
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element)
        throws IncorrectOperationException {

        // 如果处于预览模式，则直接返回，不执行任何会产生副作用的操作, 只有在真实执行意图操作时才执行完整的处理流程
        if (IntentionPreviewUtils.isPreviewElement(element)) {
            return;
        }

        MarkdownImage markdownImage = this.getMarkdownImage(editor);
        if (markdownImage == null) {
            return;
        }

        execute(project, editor, element);
    }

    /**
     * 执行操作
     * <p>
     * 在给定的项目, 编辑器和 PSI 元素上下文中执行特定操作.
     *
     * @param project 当前项目
     * @param editor  当前编辑器实例
     * @param element 要操作的 PSI 元素
     * @throws NullPointerException 如果参数为 null(由 {@code @NotNull} 注解保证 )
     */
    public abstract void execute(@NotNull Project project, Editor editor, @NotNull PsiElement element);

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

        log.debug("documentLine = {}, linestartoffset = {}, lineendoffset = {}", documentLine, linestartoffset, lineendoffset);

        String text = editor.getDocument().getText(new TextRange(linestartoffset, lineendoffset));
        log.debug("text = {}", text);
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
    MarkdownImage getMarkdownImage(Editor editor) {
        Document document = editor.getDocument();
        int documentLine = document.getLineNumber(editor.getCaretModel().getOffset());
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);

        return MarkdownUtils.analysisImageMark(virtualFile, this.getLineText(editor), documentLine);
    }

    /**
     * 创建包含文档和Markdown图片的处理数据映射
     * <p>
     * 根据传入的编辑器和匹配的Markdown图片，创建一个映射关系，其中键为文档对象，值为包含该Markdown图片的列表。
     *
     * @param editor         编辑器对象，用于获取文档
     * @param matchImageMark 匹配的Markdown图片对象，将被添加到文档对应的列表中
     * @return 包含文档和Markdown图片的映射关系
     */
    @NotNull
    static Map<Document, List<MarkdownImage>> createProcessData(Editor editor, MarkdownImage matchImageMark) {
        return new HashMap<>(1) {
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
    }
}
