package info.dong4j.idea.plugin.codevision;

import com.intellij.codeInsight.codeVision.CodeVisionEntry;
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.ui.Messages;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import icons.MikIcons;
import kotlin.Unit;
import lombok.extern.slf4j.Slf4j;

/**
 * CleanShot 注解代码视觉提供者
 * <p> 该类继承自 AbstractMarkdownImageCodeVisionProvider, 用于在 Markdown 图像上提供 CleanShot 注解功能.
 * 主要功能包括创建注解条目, 解析图像路径以及在 CleanShot 中打开图像进行注解.
 * 仅对本地路径的 Markdown 图像提供注解功能, 远程图像将被忽略.
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2025.12.16
 * @since 1.0.0
 */
@Slf4j
public class CleanShotAnnotateCodeVisionProvider extends AbstractMarkdownImageCodeVisionProvider {
    /** 提供者的唯一标识符 */
    private static final String PROVIDER_ID = "markdown.image.kit.code.vision.annotate";

    /**
     * 获取提供者的唯一标识符
     * <p> 返回该代码视觉提供者的唯一 ID, 用于识别和注册提供者.
     *
     * @return 提供者的唯一标识符
     */
    @Override
    protected @NotNull String getProviderId() {
        return PROVIDER_ID;
    }

    /**
     * 获取提供者的名称
     * <p> 返回当前代码视觉提供者的本地化名称.
     *
     * @return 提供者的名称
     */
    @Override
    protected @NotNull String getProviderName() {
        return MikBundle.message("mik.codevision.annotate");
    }

    /**
     * 为图片创建代码视觉条目
     * <p> 根据图片的位置, 若图片为本地图片, 则创建注解条目, 否则返回空列表.</p>
     *
     * @param context       上下文信息
     * @param markdownImage Markdown 图片对象
     * @return 包含注解条目的列表, 若无法创建条目则返回空列表
     */
    @Override
    protected @NotNull List<CodeVisionEntry> createEntriesForImage(@NotNull Context context,
                                                                   @NotNull MarkdownImage markdownImage) {
        if (markdownImage.getLocation() != ImageLocationEnum.LOCAL) {
            return Collections.emptyList();
        }
        CodeVisionEntry entry = createAnnotateEntry(context, markdownImage);
        return entry == null ? Collections.emptyList() : Collections.singletonList(entry);
    }

    /**
     * 创建用于标注图片的 CodeVisionEntry
     * <p> 根据给定的上下文和图片信息, 生成一个可点击的 CodeVisionEntry, 用于在 CleanShot 中打开图片进行标注.
     *
     * @param context       上下文信息
     * @param markdownImage Markdown 图片信息
     * @return 返回创建的 CodeVisionEntry, 如果无法解析路径则返回 null
     */
    @Nullable
    private CodeVisionEntry createAnnotateEntry(@NotNull Context context, @NotNull MarkdownImage markdownImage) {
        String absolutePath = resolveAbsolutePath(context, markdownImage);
        if (StringUtils.isBlank(absolutePath)) {
            return null;
        }

        return new ClickableTextCodeVisionEntry(
            MikBundle.message("mik.codevision.annotate"),
            getId(),
            (event, currentEditor) -> {
                openInCleanShot(context, absolutePath);
                return Unit.INSTANCE;
            },
            MikIcons.CLEANSHOTX,
            MikBundle.message("mik.codevision.annotate"),
            MikBundle.message("mik.codevision.annotate.tooltip"),
            Collections.emptyList()
        );
    }

    /**
     * 在 CleanShot Annotate 中打开指定路径的图片
     * <p> 将指定的绝对路径编码后生成 CleanShot Annotate 的 URL, 并尝试在默认浏览器中打开.
     * 如果打开失败, 将记录日志并显示错误提示.
     *
     * @param context      上下文信息, 包含项目等信息
     * @param absolutePath 要打开的图片的绝对路径
     */
    private void openInCleanShot(@NotNull Context context, @NotNull String absolutePath) {
        try {
            String encodedPath = URLEncoder.encode(absolutePath, StandardCharsets.UTF_8).replace("+", "%20");
            String url = "cleanshot://open-annotate?filepath=" + encodedPath;
            BrowserUtil.browse(url);
        } catch (Exception e) {
            log.trace("打开 CleanShot Annotate 失败: {}", absolutePath, e);
            Messages.showErrorDialog(
                context.project,
                MikBundle.message("mik.codevision.annotate.path.missing"),
                MikBundle.message("mik.codevision.title")
                                    );
        }
    }

    /**
     * 解析并返回图片的绝对路径
     * <p> 根据提供的上下文和图片信息, 尝试解析出图片的绝对路径. 如果图片的虚拟文件存在, 则直接返回其路径; 否则尝试根据相对路径和上下文基础目录进行解析.
     *
     * @param context       上下文信息, 用于获取基础目录
     * @param markdownImage Markdown 图片对象, 包含路径信息
     * @return 解析出的绝对路径, 如果解析失败则返回 null
     */
    @Nullable
    private String resolveAbsolutePath(@NotNull Context context, @NotNull MarkdownImage markdownImage) {
        if (markdownImage.getVirtualFile() != null && markdownImage.getVirtualFile().exists()) {
            return markdownImage.getVirtualFile().getPath();
        }

        String path = markdownImage.getPath();
        if (StringUtils.isBlank(path)) {
            return null;
        }

        try {
            Path imagePath = Paths.get(path);
            if (imagePath.isAbsolute()) {
                return imagePath.normalize().toString();
            }

            if (context.virtualFile != null && context.virtualFile.getParent() != null) {
                Path baseDir = Paths.get(context.virtualFile.getParent().getPath());
                return baseDir.resolve(imagePath).normalize().toString();
            }
        } catch (InvalidPathException e) {
            log.trace("解析图片路径失败: {}", path, e);
        }

        return null;
    }
}
