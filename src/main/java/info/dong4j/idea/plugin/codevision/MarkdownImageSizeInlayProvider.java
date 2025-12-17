package info.dong4j.idea.plugin.codevision;

import com.intellij.codeInsight.hints.declarative.HintColorKind;
import com.intellij.codeInsight.hints.declarative.HintFormat;
import com.intellij.codeInsight.hints.declarative.InlayHintsCollector;
import com.intellij.codeInsight.hints.declarative.InlayHintsProvider;
import com.intellij.codeInsight.hints.declarative.InlayPosition;
import com.intellij.codeInsight.hints.declarative.InlayTreeSink;
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition;
import com.intellij.codeInsight.hints.declarative.SharedBypassCollector;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.util.MarkdownUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.intellij.plugins.markdown.lang.psi.impl.MarkdownImage;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownLinkDestination;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

import kotlin.Unit;
import lombok.extern.slf4j.Slf4j;

/**
 * Markdown 图片大小内联提示提供者
 * <p> 用于在 Markdown 文件中为图片添加内联提示, 显示图片的大小信息. 该类实现了 InlayHintsProvider 接口, 负责识别 Markdown 图片并计算其文件大小, 然后在编辑器中显示为内联提示.
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2025.12.16
 * @since 1.0.0
 */
@Slf4j
public class MarkdownImageSizeInlayProvider implements InlayHintsProvider {
    private static final String PROVIDER_ID = "markdown.image.kit.inlay.image.size";
    /** 默认的提示格式, 用于显示内联提示信息 */
    private static final HintFormat FORMAT = HintFormat.Companion.getDefault().withColorKind(HintColorKind.TextWithoutBackground);
    /** 用于格式化数字, 保留两位小数 */
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    /**
     * 指示此提供程序是否在“dumb”模式下运行
     * <p> 返回 true 表示该提供程序可以在 IntelliJ IDEA 的“dumb”模式下运行, 即在索引未完成时也能提供内联提示
     *
     * @return 始终返回 true, 表示该提供程序是 dumb-aware 的
     */
    @Override
    public boolean isDumbAware() {
        return true;
    }

    /**
     * 创建用于显示内联提示的收集器
     * <p> 检查文件是否为 Markdown 文件并插件是否启用, 若不符合条件则返回 null. 否则创建一个收集器用于在 Markdown 图片元素上显示文件大小提示.
     *
     * @param file   当前编辑的文件
     * @param editor 当前编辑器实例
     * @return 如果文件是 Markdown 文件且插件已启用, 则返回 InlayHintsCollector 实例, 否则返回 null
     */
    @Override
    public @Nullable InlayHintsCollector createCollector(@NotNull PsiFile file, @NotNull Editor editor) {
        if (!isMarkdownFile(file)) {
            return null;
        }

        MikState state = MikPersistenComponent.getInstance().getState();
        if (!state.isEnablePlugin()) {
            return null;
        }

        Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        if (document == null) {
            return null;
        }

        return (SharedBypassCollector) (element, sink) -> collectFromElement(file, document, element, sink);
    }

    /**
     * 收集 Markdown 图片元素的内联提示信息
     * <p> 检查当前元素是否为 Markdown 图片, 如果是, 则解析图片路径并获取文件大小, 最后在编辑器中插入内联提示信息.</p>
     *
     * @param file     当前处理的 Psi 文件
     * @param document 与文件关联的文档对象
     * @param element  当前处理的 Psi 元素
     * @param sink     用于添加内联提示信息的收集器
     */
    private void collectFromElement(@NotNull PsiFile file,
                                    @NotNull Document document,
                                    @NotNull PsiElement element,
                                    @NotNull InlayTreeSink sink) {
        if (!(element instanceof MarkdownImage)) {
            return;
        }

        Project project = file.getProject();
        VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null || !MarkdownUtils.isMardownFile(virtualFile)) {
            return;
        }

        // 按行解析 Markdown 图片，复用 MarkdownUtils 逻辑
        int lineNumber = document.getLineNumber(element.getTextRange().getStartOffset());
        int lineStart = document.getLineStartOffset(lineNumber);
        int lineEnd = document.getLineEndOffset(lineNumber);
        String lineText = document.getText(TextRange.create(lineStart, lineEnd));

        if (MarkdownUtils.illegalImageMark(project, lineText)) {
            return;
        }

        info.dong4j.idea.plugin.entity.MarkdownImage markdownImage =
            MarkdownUtils.analysisImageMark(virtualFile, lineText, lineNumber);
        if (markdownImage == null || markdownImage.getLocation() != ImageLocationEnum.LOCAL) {
            return;
        }

        Path imagePath = resolveAbsolutePath(virtualFile, markdownImage);
        if (imagePath == null) {
            return;
        }

        Long size = readFileSize(imagePath);
        if (size == null) {
            return;
        }

        String humanReadable = formatSize(size);
        InlayPosition position = calculateDestinationPosition((MarkdownImage) element);
        if (position == null) {
            position = new InlineInlayPosition(element.getTextRange().getEndOffset(), true, 0);
        }
        sink.addPresentation(position,
                             null,
                             MikBundle.message("mik.inlay.image.size.tooltip", imagePath.getFileName(), humanReadable),
                             FORMAT,
                             builder -> {
                                 builder.text(humanReadable, null);
                                 return Unit.INSTANCE;
                             });
    }

    /**
     * 解析并返回图片的绝对路径
     * <p> 根据提供的 VirtualFile 和 MarkdownImage 对象解析图片的绝对路径. 如果图片路径无效或解析失败, 则返回 null.
     *
     * @param currentFile   当前文件的 VirtualFile 对象
     * @param markdownImage 包含图片路径信息的 MarkdownImage 对象
     * @return 解析后的图片绝对路径, 如果解析失败或路径无效则返回 null
     */
    @Nullable
    private Path resolveAbsolutePath(@NotNull VirtualFile currentFile,
                                     @NotNull info.dong4j.idea.plugin.entity.MarkdownImage markdownImage) {
        if (markdownImage.getVirtualFile() != null && markdownImage.getVirtualFile().exists()) {
            return Paths.get(markdownImage.getVirtualFile().getPath());
        }

        String path = markdownImage.getPath();
        if (StringUtils.isBlank(path)) {
            return null;
        }

        try {
            Path imagePath = Paths.get(path);
            if (imagePath.isAbsolute()) {
                return imagePath.normalize();
            }

            VirtualFile parent = currentFile.getParent();
            if (parent != null) {
                return Paths.get(parent.getPath()).resolve(imagePath).normalize();
            }
        } catch (InvalidPathException e) {
            log.trace("解析图片路径失败: {}", path, e);
        }

        return null;
    }

    /**
     * 读取指定路径下图片文件的大小 (字节数)
     * <p> 如果文件不存在或读取过程中发生异常, 则返回 null.
     *
     * @param imagePath 图片文件的路径
     * @return 文件大小 (字节数), 如果文件不存在或读取失败则返回 null
     */
    @Nullable
    private Long readFileSize(@NotNull Path imagePath) {
        try {
            if (!Files.exists(imagePath)) {
                return null;
            }
            return Files.size(imagePath);
        } catch (IOException e) {
            log.trace("读取图片字节大小失败: {}", imagePath, e);
            return null;
        }
    }

    /**
     * 将字节数转换为可读的大小格式
     * <p> 根据字节数自动转换为 B,KB 或 MB, 并保留两位小数
     *
     * @param bytes 需要转换的字节数
     * @return 格式化后的大小字符串, 例如 "1.23 KB" 或 "4.56 MB"
     */
    @NotNull
    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return DECIMAL_FORMAT.format(kb) + " KB";
        }
        double mb = kb / 1024.0;
        return DECIMAL_FORMAT.format(mb) + " MB";
    }

    private boolean isMarkdownFile(@NotNull PsiFile file) {
        VirtualFile virtualFile = file.getVirtualFile();
        return virtualFile != null && MarkdownUtils.isMardownFile(virtualFile);
    }

    /**
     * 尝试将提示放置在图片路径起始处 (即括号内路径之前)
     *
     * @param markdownImage Markdown 图片 PSI 元素
     * @return 内联提示位置, 找不到路径时返回 null
     */
    @Nullable
    private InlineInlayPosition calculateDestinationPosition(@NotNull MarkdownImage markdownImage) {
        MarkdownLinkDestination destination = PsiTreeUtil.findChildOfType(markdownImage, MarkdownLinkDestination.class);
        if (destination == null) {
            return null;
        }
        return new InlineInlayPosition(destination.getTextRange().getStartOffset(), true, 0);
    }
}
