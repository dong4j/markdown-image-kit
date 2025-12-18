package info.dong4j.idea.plugin.codevision;

import com.intellij.codeInsight.codeVision.CodeVisionEntry;
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFileManager;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.swing.Icon;

import icons.MikIcons;
import kotlin.Unit;
import lombok.extern.slf4j.Slf4j;

/**
 * 图片删除代码视图提供者
 * <p> 对本地图片提供“删除图片”代码视图条目, 支持是否确认的配置.</p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2025.12.16
 * @since 1.0.0
 */
@Slf4j
public class MarkdownImageDeleteCodeVisionProvider extends AbstractMarkdownImageCodeVisionProvider {
    /** 提供者的唯一标识符 */
    private static final String PROVIDER_ID = "markdown.image.kit.code.vision.image.delete";

    /**
     * 获取提供者的唯一标识符
     * <p> 返回该图像大小代码视图提供者的唯一 ID.
     *
     * @return 提供者的唯一标识符
     */
    @Override
    protected @NotNull String getProviderId() {
        return PROVIDER_ID;
    }

    /**
     * 获取提供者的名称
     * <p> 返回当前图像尺寸代码视图提供者的显示名称, 该名称通过资源包获取.
     *
     * @return 提供者的名称
     */
    @Override
    protected @NotNull String getProviderName() {
        return MikBundle.message("mik.codevision.image.delete.title");
    }

    /**
     * 为图片创建代码视图条目, 显示图片尺寸信息
     * <p> 仅当图片位于本地时创建条目, 解析图片的绝对路径并读取其尺寸, 生成包含尺寸信息的代码视图条目.
     *
     * @param context       上下文信息
     * @param markdownImage Markdown 图片对象
     * @return 包含图片尺寸信息的代码视图条目列表, 若无法解析或读取图片则返回空列表
     */
    @Override
    protected @NotNull List<CodeVisionEntry> createEntriesForImage(@NotNull Context context,
                                                                   @NotNull MarkdownImage markdownImage) {
        MikState state = MikPersistenComponent.getInstance().getState();
        if (!state.isDeleteImage()) {
            return Collections.emptyList();
        }

        if (markdownImage.getLocation() != ImageLocationEnum.LOCAL) {
            return Collections.emptyList();
        }

        Path imagePath = resolveAbsolutePath(context, markdownImage);
        String targetName = imagePath != null ? imagePath.getFileName().toString() : markdownImage.getImageName();
        String text = MikBundle.message("mik.codevision.image.delete.entry");
        String tooltip = MikBundle.message("mik.codevision.image.delete.tooltip", targetName == null ? "" : targetName);
        Icon icon = MikIcons.MIK;

        CodeVisionEntry entry = new ClickableTextCodeVisionEntry(
            text,
            getId(),
            (event, currentEditor) -> {
                if (currentEditor == null || context.project == null) {
                    return Unit.INSTANCE;
                }
                MikState currentState = MikPersistenComponent.getInstance().getState();
                if (currentState.isDeleteImageWithConfirm()) {
                    showConfirmPopup(currentEditor, () -> deleteImageAndMark(context, markdownImage, imagePath, currentEditor));
                } else {
                    deleteImageAndMark(context, markdownImage, imagePath, currentEditor);
                }
                return Unit.INSTANCE;
            },
            icon,
            text,
            tooltip,
            Collections.emptyList()
        );
        return Collections.singletonList(entry);
    }

    /**
     * 删除图片并从文档中移除对应的标记
     * <p> 根据提供的上下文信息和图片对象, 删除本地图片文件并从编辑器的当前文档中移除对应的图片标记.</p>
     *
     * @param context       上下文信息, 用于获取项目和执行写操作
     * @param markdownImage Markdown 图片对象, 包含图片路径和标记信息
     * @param imagePath     图片的绝对路径, 如果为 null 则不执行文件删除操作
     * @param editor        当前编辑器实例, 用于操作文档内容
     */
    private void deleteImageAndMark(@NotNull Context context,
                                    @NotNull MarkdownImage markdownImage,
                                    @Nullable Path imagePath,
                                    @NotNull Editor editor) {
        Project project = context.project;
        if (project == null) {
            return;
        }
        WriteCommandAction.runWriteCommandAction(project, () -> {
            // 删除图片文件, 不存在时静默
            if (imagePath != null) {
                try {
                    Files.deleteIfExists(imagePath);
                    // 删除后刷新文件系统
                    VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
                } catch (IOException e) {
                    log.trace("删除图片文件失败: {}", imagePath, e);
                }
            }

            Document document = editor.getDocument();
            int lineNumber = markdownImage.getLineNumber();
            if (lineNumber < 0 || lineNumber >= document.getLineCount()) {
                return;
            }

            int lineStart = document.getLineStartOffset(lineNumber);
            int lineEnd = document.getLineEndOffset(lineNumber);
            String lineText = document.getText(TextRange.create(lineStart, lineEnd));
            String originalMark = markdownImage.getOriginalMark();
            int markIndex = originalMark == null ? -1 : lineText.indexOf(originalMark);
            if (markIndex < 0) {
                // 找不到标记时删除整行内容
                markIndex = 0;
                originalMark = lineText;
            }
            int startOffset = lineStart + markIndex;
            int endOffset = startOffset + originalMark.length();
            if (startOffset < endOffset) {
                document.deleteString(startOffset, endOffset);
            }
        });
    }

    /**
     * 显示确认删除图片的弹窗
     * <p> 创建一个弹窗, 用于确认是否删除图片. 弹窗包含两个选项: 确认删除和不确认 (并关闭确认提示). 根据用户选择执行相应操作.</p>
     *
     * @param editor       当前编辑器实例
     * @param deleteAction 删除图片的回调操作
     */
    private void showConfirmPopup(@NotNull Editor editor, @NotNull Runnable deleteAction) {
        List<String> items = Arrays.asList(
            MikBundle.message("mik.codevision.image.delete.confirm"),
            MikBundle.message("mik.codevision.image.delete.no.confirm")
                                          );
        JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<>(MikBundle.message("mik.codevision.image.delete.title"),
                                                                             items) {
            /**
             * 处理用户在弹出菜单中选择的选项
             * <p> 当用户选择某个选项后, 根据选择的值执行相应的操作. 如果选择的是最终选项, 则执行删除操作或修改状态后执行删除操作.
             *
             * @param selectedValue 用户选择的选项值
             * @param finalChoice   是否为最终选择
             * @return 返回 {@link PopupStep#FINAL_CHOICE} 表示处理完成
             */
            @Override
            public PopupStep<?> onChosen(String selectedValue, boolean finalChoice) {
                if (!finalChoice) {
                    return FINAL_CHOICE;
                }
                if (Objects.equals(selectedValue, items.get(0))) {
                    deleteAction.run();
                } else if (Objects.equals(selectedValue, items.get(1))) {
                    MikState state = MikPersistenComponent.getInstance().getState();
                    state.setDeleteImageWithConfirm(false);
                    deleteAction.run();
                }
                return FINAL_CHOICE;
            }

            /**
             * 禁用自动选择功能
             * <p> 该方法返回 false, 表示在弹出窗口中不会自动选择任何选项.</p>
             *
             * @return 返回 false, 表示自动选择功能被禁用
             */
            @Override
            public boolean isAutoSelectionEnabled() {
                return false;
            }

            /**
             * 根据传入的值返回对应的图标
             * <p> 根据传入的字符串值判断是否与列表中的特定项匹配, 并返回相应的图标.
             *
             * @param value 用于匹配的字符串值
             * @return 如果值匹配第一个项则返回警告图标, 匹配第二个项则返回检查通过图标, 否则返回 null
             */
            @Override
            public Icon getIconFor(String value) {
                if (Objects.equals(value, items.get(0))) {
                    return AllIcons.General.Warning;
                }
                if (Objects.equals(value, items.get(1))) {
                    return AllIcons.General.InspectionsOK;
                }
                return null;
            }
        }).showInBestPositionFor(editor);
    }

    /**
     * 解析并返回图片的绝对路径
     * <p> 根据提供的上下文和图片信息, 尝试解析出图片的绝对路径. 如果图片位于虚拟文件系统中且存在, 则直接返回其路径; 否则根据相对路径和上下文基础目录进行解析.
     *
     * @param context       上下文信息, 用于获取基础目录
     * @param markdownImage Markdown 图片信息, 包含路径和虚拟文件信息
     * @return 解析后的绝对路径, 如果解析失败或路径无效则返回 null
     */
    @Nullable
    private Path resolveAbsolutePath(@NotNull Context context, @NotNull MarkdownImage markdownImage) {
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

            if (context.virtualFile != null && context.virtualFile.getParent() != null) {
                Path baseDir = Paths.get(context.virtualFile.getParent().getPath());
                return baseDir.resolve(imagePath).normalize();
            }
        } catch (InvalidPathException e) {
            log.trace("解析图片路径失败: {}", path, e);
        }

        return null;
    }

}
