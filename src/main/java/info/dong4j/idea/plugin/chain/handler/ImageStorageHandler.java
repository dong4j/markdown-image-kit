package info.dong4j.idea.plugin.chain.handler;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.action.intention.IntentionActionBase;
import info.dong4j.idea.plugin.console.MikConsoleView;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.util.PathUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 图片存储处理类
 * <p>
 * 用于处理文档中图片的保存操作，支持从输入流中复制图片到指定路径，并更新相关图片信息。
 * 该类继承自 ActionHandlerAdapter，主要用于在文档编辑过程中执行图片保存逻辑。
 * <p>
 * 主要功能包括：获取操作名称、判断是否启用、执行保存操作。
 * 在执行过程中，会遍历等待处理的图片集合，逐个复制图片并设置保存路径、图片标记类型、位置等信息。
 * 若保存失败，则设置错误标记并保留原始信息。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@SuppressWarnings("D")
@Slf4j
public class ImageStorageHandler extends ActionHandlerAdapter {
    /**
     * 获取名称
     * <p>
     * 返回预定义的名称字符串，用于表示存储操作的标题
     *
     * @return 名称字符串
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.store.title");
    }

    /**
     * 判断是否启用功能
     * <p>
     * 根据传入的事件数据判断当前功能是否启用。
     * 当以下任一条件满足时启用：
     * 1. 全局配置了图片保存路径（getCurrentInsertPath 不为空）
     * 2. 存在临时存储路径（temporaryStoragePath 不为空）
     *
     * @param data 事件数据
     * @return 是否启用功能
     * @see EventData#temporaryStoragePath
     * @since 0.0.1
     */
    @Override
    public boolean isEnabled(EventData data) {
        // 检查全局配置路径或临时存储路径
        boolean hasGlobalPath = !MikState.getInstance().getCurrentInsertPath().isBlank();
        boolean hasTemporaryPath = StringUtils.isNotBlank(data.getTemporaryStoragePath());
        return hasGlobalPath || hasTemporaryPath;
    }

    /**
     * 执行处理逻辑，将 Markdown 图片保存为本地文件并更新相关属性
     * <p>
     * 该方法从传入的 EventData 中获取待处理的图片信息，依次处理每张图片。
     * 根据配置参数（applyToLocalImages、preferRelativePath、addDotSlash、autoEscapeImageUrl）
     * 决定图片的处理方式和最终的 Markdown 标签路径。
     * <p>
     * 处理逻辑：
     * 1. 网络图片：始终拷贝到 currentInsertPath
     * 2. 图片流：根据 preferRelativePath 决定路径类型
     * 3. 文件：根据 applyToLocalImages 和 preferRelativePath 决定是否拷贝和路径类型
     *
     * @param data 包含处理所需数据的事件数据对象，包含待处理图片信息、进度指示器等
     * @return 始终返回 true，表示处理成功
     */
    @SuppressWarnings("D")
    @Override
    public boolean execute(EventData data) {
        ProgressIndicator indicator = data.getIndicator();
        int size = data.getSize();
        int totalProcessed = 0;

        // 获取配置
        MikState state = MikPersistenComponent.getInstance().getState();

        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
            int totalCount = imageEntry.getValue().size();
            Document document = imageEntry.getKey();
            VirtualFile currentFile = FileDocumentManager.getInstance().getFile(document);
            if (currentFile == null) {
                continue;
            }

            // 优先使用临时存储路径，如果没有则使用全局配置
            String savepath = StringUtils.isNotBlank(data.getTemporaryStoragePath())
                              ? data.getTemporaryStoragePath()
                              : IntentionActionBase.getState().getImageSavePath();

            // 处理临时存储路径中的占位符
            if (StringUtils.isNotBlank(data.getTemporaryStoragePath())) {
                savepath = processPathPlaceholders(savepath, currentFile, data.getProject());
            }

            for (MarkdownImage markdownImage : imageEntry.getValue()) {
                String imageName = markdownImage.getImageName();
                indicator.setText2("Processing " + imageName);
                indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);

                // 根据不同情况处理图片
                String storageType;
                if (markdownImage.getLocation() == ImageLocationEnum.NETWORK) {
                    // todo-dong4j : (2025.12.15 01:53) [这个逻辑可能走不到 需要排查一下]
                    storageType = "网络图片";
                    // 网络图片：始终拷贝到 currentInsertPath（已经由 ImageDownloadHandler 处理）
                    processNetworkImage(markdownImage, currentFile, savepath, state);
                } else if (markdownImage.isImageStream()) {
                    // 网络图片迁移到本地和本地图片迁移到本地都会走这个分支
                    storageType = "图片流";
                    // 图片流：从剪贴板粘贴的图片
                    processImageStream(markdownImage, currentFile, savepath, state);
                } else {
                    storageType = "本地文件";
                    // 文件：从剪贴板粘贴的文件
                    processImageFile(markdownImage, currentFile, savepath, state);
                }

                // 输出存储日志
                MikConsoleView.printMessage(data.getProject(),
                                            String.format("  [存储] 类型: %s | 图片: %s", storageType, imageName));

                // 输出最终的Markdown路径
                String finalPath = markdownImage.getPath();
                if (finalPath != null && !finalPath.isEmpty()) {
                    MikConsoleView.printMessage(data.getProject(),
                                                String.format("         Markdown路径: %s", finalPath));
                }
            }
        }
        return true;
    }

    /**
     * 处理网络图片
     * <p>
     * 网络图片始终拷贝到 currentInsertPath，仅区分相对路径与绝对路径。
     *
     * @param markdownImage Markdown 图片对象
     * @param currentFile   当前 Markdown 文件
     * @param savepath      保存路径
     * @param state         配置状态
     */
    private void processNetworkImage(MarkdownImage markdownImage,
                                     VirtualFile currentFile,
                                     String savepath,
                                     MikState state) {
        // 网络图片已经下载并保存，只需要生成正确的路径
        String imagePath = generateImagePath(markdownImage, currentFile, savepath, state, true);
        updateMarkdownImage(markdownImage, imagePath, state);
    }

    /**
     * 处理本地图片
     * <p>
     * 本地图片直接移动到 currentInsertPath。
     *
     * @param markdownImage Markdown 图片对象
     * @param currentFile   当前 Markdown 文件
     * @param savepath      保存路径
     * @param state         配置状态
     */
    private void processLocalImage(MarkdownImage markdownImage,
                                   VirtualFile currentFile,
                                   String savepath,
                                   MikState state) {
        // 本地图片直接移动到 currentInsertPath
        String imagePath = generateImagePath(markdownImage, currentFile, savepath, state, false);
        updateMarkdownImage(markdownImage, imagePath, state);
    }

    /**
     * 处理图片流
     * <p>
     * 处理逻辑：
     * 1. applyToLocalImages = false, preferRelativePath = false: 写入绝对路径
     * 2. applyToLocalImages = true, preferRelativePath = false: 拷贝到 currentInsertPath，使用绝对路径
     * 3. applyToLocalImages = true, preferRelativePath = true: 拷贝到 currentInsertPath，使用相对路径
     * 4. applyToLocalImages = false, preferRelativePath = true: 拷贝到 currentInsertPath，使用相对路径
     *
     * @param markdownImage Markdown 图片对象
     * @param currentFile   当前 Markdown 文件
     * @param savepath      保存路径
     * @param state         配置状态
     */
    private void processImageStream(MarkdownImage markdownImage,
                                    VirtualFile currentFile,
                                    String savepath,
                                    MikState state) {
        // 图片流始终需要保存到文件
        File saveFile = saveImageToFile(markdownImage, currentFile, savepath);
        if (saveFile == null) {
            markdownImage.setFinalMark("copy error");
            markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
            markdownImage.setLocation(ImageLocationEnum.LOCAL);
            return;
        }

        // 生成图片路径
        String imagePath;
        if (state.isPreferRelativePath()) {
            // 使用相对路径
            imagePath = PathUtils.calculateTargetRelativePath(currentFile, savepath, markdownImage.getImageName());
            if (state.isAddDotSlash()) {
                imagePath = PathUtils.addDotSlashPrefix(imagePath);
            }
        } else {
            // 使用绝对路径
            imagePath = PathUtils.getAbsolutePath(currentFile, savepath, markdownImage.getImageName());
        }

        updateMarkdownImage(markdownImage, imagePath, state);

        // 更新输入流
        try {
            markdownImage.setInputStream(new FileInputStream(saveFile));
        } catch (FileNotFoundException e) {
            log.trace("", e);
        }
    }

    /**
     * 处理图片文件
     * <p>
     * 处理逻辑：
     * 1. applyToLocalImages = false, preferRelativePath = false: 使用文件的绝对路径，不拷贝
     * 2. applyToLocalImages = true, preferRelativePath = false: 拷贝到 currentInsertPath，使用绝对路径
     * 3. applyToLocalImages = true, preferRelativePath = true: 拷贝到 currentInsertPath，使用相对路径
     * 4. applyToLocalImages = false, preferRelativePath = true: 使用文件的相对路径，不拷贝
     *
     * @param markdownImage Markdown 图片对象
     * @param currentFile   当前 Markdown 文件
     * @param savepath      保存路径
     * @param state         配置状态
     */
    private void processImageFile(MarkdownImage markdownImage,
                                  VirtualFile currentFile,
                                  String savepath,
                                  MikState state) {
        String imagePath;

        if (state.isApplyToLocalImages()) {
            // 拷贝文件到 currentInsertPath
            File saveFile = saveImageToFile(markdownImage, currentFile, savepath);
            if (saveFile == null) {
                markdownImage.setFinalMark("copy error");
                markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
                markdownImage.setLocation(ImageLocationEnum.LOCAL);
                return;
            }

            // 生成路径
            if (state.isPreferRelativePath()) {
                imagePath = PathUtils.calculateTargetRelativePath(currentFile, savepath, markdownImage.getImageName());
                if (state.isAddDotSlash()) {
                    imagePath = PathUtils.addDotSlashPrefix(imagePath);
                }
            } else {
                imagePath = PathUtils.getAbsolutePath(currentFile, savepath, markdownImage.getImageName());
            }

            // 更新输入流
            try {
                markdownImage.setInputStream(new FileInputStream(saveFile));
            } catch (FileNotFoundException e) {
                log.trace("", e);
            }
        } else {
            // 不拷贝，使用原文件路径
            String sourceFilePath = markdownImage.getSourceFilePath();
            if (StringUtils.isBlank(sourceFilePath)) {
                markdownImage.setFinalMark("source file path is empty");
                markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
                markdownImage.setLocation(ImageLocationEnum.LOCAL);
                return;
            }

            if (state.isPreferRelativePath()) {
                // 使用相对路径
                imagePath = PathUtils.calculateRelativePath(currentFile, sourceFilePath);
                if (state.isAddDotSlash()) {
                    imagePath = PathUtils.addDotSlashPrefix(imagePath);
                }
            } else {
                // 使用绝对路径
                imagePath = PathUtils.normalizePathSeparator(sourceFilePath);
            }
        }

        updateMarkdownImage(markdownImage, imagePath, state);
    }

    /**
     * 保存图片到文件
     * <p>
     * 将图片的输入流写入到指定目录下的文件中。
     * 目录路径会被规范化，去除冗余的 "./" 等路径部分。
     * 如果 savepath 已经是绝对路径，则直接使用，不与父目录拼接。
     *
     * @param markdownImage Markdown 图片对象
     * @param currentFile   当前 Markdown 文件
     * @param savepath      保存路径（可能是相对路径或绝对路径）
     * @return 保存的文件对象，如果失败返回 null
     */
    private File saveImageToFile(MarkdownImage markdownImage, VirtualFile currentFile, String savepath) {
        File imageDir;

        // 检查 savepath 是否已经是绝对路径
        File savepathFile = new File(savepath);
        if (savepathFile.isAbsolute()) {
            // 已经是绝对路径，直接使用
            imageDir = savepathFile;
        } else {
            // 相对路径，基于 Markdown 文件的父目录拼接
            File curDocument = new File(currentFile.getPath());
            imageDir = new File(curDocument.getParent(), savepath);
        }

        // 规范化目录路径，去除 "./" 等冗余部分
        try {
            imageDir = imageDir.getCanonicalFile();
        } catch (IOException e) {
            // 如果无法获取规范路径，使用绝对路径
            imageDir = imageDir.getAbsoluteFile();
        }

        boolean checkDir = imageDir.exists() && imageDir.isDirectory();
        if (!checkDir && !imageDir.mkdirs()) {
            log.trace("无法创建目录: {}", imageDir.getAbsolutePath());
            return null;
        }

        File saveFile = new File(imageDir, markdownImage.getImageName());
        try {
            FileUtil.copy(markdownImage.getInputStream(), new FileOutputStream(saveFile));
            log.trace("图片已保存到: {}", saveFile.getAbsolutePath());
            return saveFile;
        } catch (IOException e) {
            log.trace("Failed to save image file", e);
            return null;
        }
    }

    /**
     * 生成图片路径
     * <p>
     * 根据配置生成最终的图片路径。
     *
     * @param markdownImage     Markdown 图片对象
     * @param currentFile       当前 Markdown 文件
     * @param savepath          保存路径
     * @param state             配置状态
     * @param alwaysUseSavePath 是否总是使用保存路径（用于网络图片）
     * @return 图片路径
     */
    private String generateImagePath(MarkdownImage markdownImage,
                                     VirtualFile currentFile,
                                     String savepath,
                                     MikState state,
                                     boolean alwaysUseSavePath) {
        String imagePath;

        if (state.isPreferRelativePath()) {
            // 使用相对路径
            imagePath = PathUtils.calculateTargetRelativePath(currentFile, savepath, markdownImage.getImageName());
            if (state.isAddDotSlash()) {
                imagePath = PathUtils.addDotSlashPrefix(imagePath);
            }
        } else {
            // 使用绝对路径
            imagePath = PathUtils.getAbsolutePath(currentFile, savepath, markdownImage.getImageName());
        }

        return imagePath;
    }

    /**
     * 更新 MarkdownImage 对象
     * <p>
     * 根据生成的图片路径更新 Markdown 图片对象的相关属性。
     *
     * @param markdownImage Markdown 图片对象
     * @param imagePath     图片路径
     * @param state         配置状态
     */
    private void updateMarkdownImage(MarkdownImage markdownImage, String imagePath, MikState state) {
        // 根据配置决定是否转义 URL
        String finalPath = imagePath;
        if (state.isAutoEscapeImageUrl()) {
            finalPath = PathUtils.escapeImageUrl(imagePath);
        }

        markdownImage.setTitle("");
        markdownImage.setPath(finalPath);

        String mark = "![](" + finalPath + ")";
        markdownImage.setOriginalLineText(mark);
        markdownImage.setOriginalMark(mark);
        markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
        markdownImage.setLocation(ImageLocationEnum.LOCAL);
        markdownImage.setFinalMark(mark);

        log.trace("图片路径已更新: {}", finalPath);
    }

    /**
     * 处理路径中的占位符
     * <p>
     * 替换路径中的占位符：
     * - ${filename}: 当前文件名（不含扩展名）
     * - ${project}: 当前项目路径
     * <p>
     * 处理跨平台路径差异：
     * - 统一规范化为正斜杠，确保跨平台兼容性
     *
     * @param path        原始路径
     * @param virtualFile 当前文件
     * @param project     项目对象
     * @return 处理后的路径
     * @see info.dong4j.idea.plugin.action.paste.PasteImageAction#processPathPlaceholders(String, VirtualFile, Project)
     * @see info.dong4j.idea.plugin.entity.EventData#temporaryStoragePath
     */
    @NotNull
    private String processPathPlaceholders(@NotNull String path, @NotNull VirtualFile virtualFile, @Nullable Project project) {
        if (path.isEmpty()) {
            return path;
        }

        String result = path;

        // 替换 ${filename} 为当前文件名（不含扩展名）
        String filename = virtualFile.getNameWithoutExtension();
        result = result.replace("${filename}", filename);

        // 替换 ${project} 为项目根路径
        if (project != null && project.getBasePath() != null) {
            String projectPath = project.getBasePath();
            result = result.replace("${project}", projectPath);
        } else {
            // 如果项目路径不可用，移除 ${project} 占位符
            result = result.replace("${project}", "");
        }

        // 规范化路径分隔符，统一使用正斜杠（跨平台兼容）
        result = normalizePathSeparator(result);

        return result;
    }

    /**
     * 规范化路径分隔符
     * <p>
     * 将路径中的反斜杠统一转换为正斜杠，确保跨平台兼容性。
     * Java 的 File 类可以接受正斜杠，即使在 Windows 上也能正确处理。
     *
     * @param path 原始路径
     * @return 规范化后的路径（统一使用正斜杠）
     * @see info.dong4j.idea.plugin.action.paste.PasteImageAction#normalizePathSeparator(String)
     */
    @NotNull
    private static String normalizePathSeparator(@NotNull String path) {
        return path.replace('\\', '/');
    }
}
