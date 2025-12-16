package info.dong4j.idea.plugin.codevision;

import com.intellij.codeInsight.codeVision.CodeVisionEntry;
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;

import icons.MikIcons;
import kotlin.Unit;
import lombok.extern.slf4j.Slf4j;

/**
 * 图片尺寸代码视图提供者
 * <p> 该类继承自抽象的 Markdown 图片代码视图提供者, 用于在 Markdown 图片上提供图片尺寸的代码视图功能. 当图片位于本地时, 会读取图片的尺寸信息并生成对应的代码视图条目, 显示图片的宽高信息.
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2025.12.16
 * @since 1.0.0
 */
@Slf4j
public class MarkdownImageSizeCodeVisionProvider extends AbstractMarkdownImageCodeVisionProvider {
    /** 提供者的唯一标识符 */
    private static final String PROVIDER_ID = "markdown.image.kit.code.vision.image.size";

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
        return MikBundle.message("mik.codevision.image.size.title");
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
        if (markdownImage.getLocation() != ImageLocationEnum.LOCAL) {
            return Collections.emptyList();
        }

        Path imagePath = resolveAbsolutePath(context, markdownImage);
        if (imagePath == null) {
            return Collections.emptyList();
        }

        Dimension dimension = readImageDimension(imagePath);
        if (dimension == null) {
            return Collections.emptyList();
        }

        String text = MikBundle.message("mik.codevision.image.size.entry", dimension.width, dimension.height);
        String tooltip = MikBundle.message("mik.codevision.image.size.tooltip", imagePath.getFileName());
        Icon icon = MikIcons.MIK;

        CodeVisionEntry entry = new ClickableTextCodeVisionEntry(
            text,
            getId(),
            (event, currentEditor) -> Unit.INSTANCE,
            icon,
            text,
            tooltip,
            Collections.emptyList()
        );
        return Collections.singletonList(entry);
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

    /**
     * 读取图片的尺寸信息
     * <p> 根据给定的图片路径读取图片文件, 并返回其宽度和高度. 如果图片无法读取或路径无效, 则返回 null.
     *
     * @param imagePath 图片的绝对路径
     * @return 图片的尺寸 (宽度和高度), 如果读取失败则返回 null
     */
    @Nullable
    private Dimension readImageDimension(@NotNull Path imagePath) {
        try {
            BufferedImage image = ImageIO.read(imagePath.toFile());
            if (image == null) {
                return null;
            }
            return new Dimension(image.getWidth(), image.getHeight());
        } catch (Exception e) {
            log.trace("读取图片尺寸失败: {}", imagePath, e);
            return null;
        }
    }
}

