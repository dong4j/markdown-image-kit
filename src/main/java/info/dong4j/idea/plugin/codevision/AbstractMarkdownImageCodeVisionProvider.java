package info.dong4j.idea.plugin.codevision;

import com.intellij.codeInsight.codeVision.CodeVisionAnchorKind;
import com.intellij.codeInsight.codeVision.CodeVisionEntry;
import com.intellij.codeInsight.codeVision.CodeVisionProvider;
import com.intellij.codeInsight.codeVision.CodeVisionRelativeOrdering;
import com.intellij.codeInsight.codeVision.CodeVisionState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.util.MarkdownUtils;

import org.intellij.plugins.markdown.lang.psi.impl.MarkdownImage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象的 Markdown 图片代码视觉提供者类
 * <p> 该抽象类为 Markdown 文件中的图片元素提供代码视觉功能, 负责识别图片标记并生成相应的代码视觉条目. 它实现了 CodeVisionProvider 接口, 提供基础的 ID, 名称, 可用性判断以及上下文预计算等功能, 具体实现由子类完成.
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2025.12.16
 * @since 1.0.0
 */
@Slf4j
@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractMarkdownImageCodeVisionProvider implements CodeVisionProvider<AbstractMarkdownImageCodeVisionProvider.Context> {

    /**
     * 获取提供者的唯一标识符
     * <p> 返回该代码视觉提供者的唯一 ID, 用于标识不同的提供者实现
     *
     * @return 提供者的唯一标识符
     */
    protected abstract @NotNull String getProviderId();

    /**
     * 获取提供者的名称
     * <p> 返回当前代码视觉提供者的名称.
     *
     * @return 提供者的名称
     */
    protected abstract @NotNull String getProviderName();

    /**
     * 为指定的 Markdown 图像创建代码视图条目
     * <p> 根据提供的上下文和 Markdown 图像信息生成对应的代码视图条目列表.
     *
     * @param context       上下文信息, 包含项目, 虚拟文件等
     * @param markdownImage 要处理的 Markdown 图像对象
     * @return 生成的代码视图条目列表
     */
    protected abstract @NotNull List<CodeVisionEntry> createEntriesForImage(@NotNull Context context,
                                                                            @NotNull info.dong4j.idea.plugin.entity.MarkdownImage markdownImage);

    /**
     * 返回提供者的唯一标识符
     * <p> 该方法调用抽象方法 {@link #getProviderId()} 以获取提供者的 ID.
     *
     * @return 提供者的唯一标识符
     */
    @Override
    public @NotNull String getId() {
        return getProviderId();
    }

    /**
     * 获取提供者的名称
     * <p> 返回当前代码视觉提供者的名称.
     *
     * @return 提供者的名称
     */
    @Override
    public @NotNull String getName() {
        return getProviderName();
    }

    /**
     * 检查插件是否对指定项目可用
     * <p> 获取插件状态并判断插件是否已启用.
     *
     * @param project 目标项目
     * @return 如果插件已启用则返回 true, 否则返回 false
     */
    @Override
    public boolean isAvailableFor(@NotNull Project project) {
        MikState state = MikPersistenComponent.getInstance().getState();
        return state.isEnablePlugin();
    }

    /**
     * 获取默认的代码视图锚点类型
     * <p> 返回用于定位代码视图的默认锚点类型, 该类型决定了代码视图在编辑器中的显示位置.
     *
     * @return 默认的锚点类型, 返回 {@link CodeVisionAnchorKind#Top}
     */
    @Override
    public @NotNull CodeVisionAnchorKind getDefaultAnchor() {
        return CodeVisionAnchorKind.Top;
    }

    /**
     * 获取相对于其他代码视图的排序规则
     * <p> 返回一个排序规则列表, 指定此代码视图在其他视图中的相对位置.
     *
     * @return 包含相对排序规则的列表, 当前实现返回一个包含 {@link CodeVisionRelativeOrdering.CodeVisionRelativeOrderingFirst} 实例的列表
     */
    @Override
    public @NotNull List<CodeVisionRelativeOrdering> getRelativeOrderings() {
        return Collections.singletonList(CodeVisionRelativeOrdering.CodeVisionRelativeOrderingFirst.INSTANCE);
    }

    /**
     * 在 UI 线程上预计算上下文信息
     * <p> 根据编辑器内容获取项目信息, 并检查是否满足插件启用条件, 最终返回上下文对象.
     *
     * @param editor 编辑器实例
     * @return 预计算的上下文对象, 如果无法获取则返回空上下文
     */
    @Override
    public @NotNull Context precomputeOnUiThread(@NotNull Editor editor) {
        Project project = editor.getProject();
        if (project == null) {
            return Context.EMPTY;
        }

        return ReadAction.compute(() -> {
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            if (psiFile == null) {
                return Context.EMPTY;
            }

            VirtualFile virtualFile = psiFile.getVirtualFile();
            if (virtualFile == null || !MarkdownUtils.isMardownFile(virtualFile)) {
                return Context.EMPTY;
            }

            MikState state = MikPersistenComponent.getInstance().getState();
            if (!state.isEnablePlugin()) {
                return Context.EMPTY;
            }

            return new Context(project, virtualFile, true);
        });
    }

    /**
     * 计算编辑器中的代码视图状态
     * <p> 根据给定的编辑器和上下文信息, 分析 Markdown 图像标记并生成对应的代码视图条目.
     * 如果上下文不可用或未找到有效图像标记, 则返回空的视图状态.
     *
     * @param editor  当前编辑器实例
     * @param context 上下文信息, 包含项目, 虚拟文件和可用性状态
     * @return 包含代码视图条目的视图状态, 若无有效条目则返回空列表
     */
    @Override
    public @NotNull CodeVisionState computeCodeVision(@NotNull Editor editor, @NotNull Context context) {
        if (!context.available) {
            return new CodeVisionState.Ready(Collections.emptyList());
        }

        Document document = editor.getDocument();
        return ReadAction.compute(() -> {
            PsiFile psiFile = PsiDocumentManager.getInstance(context.project).getPsiFile(editor.getDocument());
            if (psiFile == null) {
                return new CodeVisionState.Ready(Collections.emptyList());
            }

            List<Pair<TextRange, CodeVisionEntry>> entries = new ArrayList<>();
            for (MarkdownImage imageElement : PsiTreeUtil.findChildrenOfType(psiFile, MarkdownImage.class)) {
                TextRange imageRange = imageElement.getTextRange();
                int line = document.getLineNumber(imageRange.getStartOffset());
                TextRange lineRange = TextRange.create(document.getLineStartOffset(line), document.getLineEndOffset(line));
                String lineText = document.getText(lineRange);

                if (MarkdownUtils.illegalImageMark(context.project, lineText)) {
                    continue;
                }

                info.dong4j.idea.plugin.entity.MarkdownImage markdownImage =
                    MarkdownUtils.analysisImageMark(context.virtualFile, lineText, line);
                if (markdownImage == null || markdownImage.getLocation() == null) {
                    continue;
                }

                List<CodeVisionEntry> imageEntries = createEntriesForImage(context, markdownImage);
                if (!imageEntries.isEmpty()) {
                    for (CodeVisionEntry entry : imageEntries) {
                        entries.add(new Pair<>(lineRange, entry));
                    }
                }
            }

            if (entries.isEmpty()) {
                return new CodeVisionState.Ready(Collections.emptyList());
            }
            return new CodeVisionState.Ready(entries);
        });
    }

    /**
     * 上下文类
     * <p> 用于封装与项目, 虚拟文件以及可用性相关的上下文信息, 通常用于在不同环境中传递和处理操作所需的环境状态.
     *
     * @author dong4j
     * @version 1.0.0
     * @email "mailto:dong4j@gmail.com"
     * @date 2025.12.16
     * @since 1.0.0
     */
    public static final class Context {
        /**
         * 空的上下文实例
         * <p> 表示没有项目, 文件或可用性的默认上下文
         */
        private static final Context EMPTY = new Context(null, null, false);
        /**
         * 当前项目上下文
         * <p> 表示与当前操作相关的项目信息
         */
        final Project project;
        /**
         * 当前虚拟文件对象
         * <p> 表示与上下文关联的文件信息
         */
        final VirtualFile virtualFile;
        /** 表示当前上下文是否可用 */
        final boolean available;

        /**
         * 构造一个 Context 对象
         * <p> 用于初始化 Context 类的实例, 包含项目, 虚拟文件以及可用性状态
         *
         * @param project     项目对象, 可为 null
         * @param virtualFile 虚拟文件对象, 可为 null
         * @param available   表示该上下文是否可用的布尔值
         */
        private Context(@Nullable Project project, @Nullable VirtualFile virtualFile, boolean available) {
            this.project = project;
            this.virtualFile = virtualFile;
            this.available = available;
        }
    }
}
