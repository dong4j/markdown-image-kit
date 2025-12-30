package info.dong4j.idea.plugin.codevision;

import com.intellij.codeInsight.codeVision.CodeVisionEntry;
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfoRt;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.messages.MessageBusConnection;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageEditorEnum;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.util.ImageUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Desktop;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

import static com.intellij.openapi.vfs.VirtualFileManager.VFS_CHANGES;

/**
 * 图片编辑器注解代码视觉提供者
 * <p> 该类继承自 AbstractMarkdownImageCodeVisionProvider, 用于在 Markdown 图像上提供外部图片编辑能力.
 * 会根据配置选择 Shottr 或 CleanShot X, 通过对应的 scheme 打开本地图片进行标注, 远程图片将被忽略.
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2025.12.16
 * @since 1.0.0
 */
@Slf4j
public class ImageEditorCodeVisionProvider extends AbstractMarkdownImageCodeVisionProvider {
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
        ImageEditorEnum editor = MikPersistenComponent.getInstance().getState().getImageEditor();
        if (editor == null) {
            editor = ImageEditorEnum.CLEANSHOT_X;
        }
        return MikBundle.message("mik.codevision.annotate", editor.getName());
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
        MikState state = MikPersistenComponent.getInstance().getState();
        if (!state.isEnableImageEditor() || markdownImage.getLocation() != ImageLocationEnum.LOCAL) {
            return Collections.emptyList();
        }
        ImageEditorEnum editor = state.getImageEditor();

        if (markdownImage.getPath().toLowerCase().endsWith(".drawio.svg")) {
            editor = ImageEditorEnum.DRAWIO;
        }
        if (editor == null) {
            editor = ImageEditorEnum.CLEANSHOT_X;
        }

        CodeVisionEntry entry = createAnnotateEntry(context, markdownImage, editor);
        return entry == null ? Collections.emptyList() : Collections.singletonList(entry);
    }

    /**
     * 创建用于标注图片的 CodeVisionEntry
     * <p> 根据给定的上下文和图片信息, 生成一个可点击的 CodeVisionEntry, 用于在外部图片编辑器中打开图片进行标注.
     *
     * @param context       上下文信息
     * @param markdownImage Markdown 图片信息
     * @param editor        当前选中的图片编辑器
     * @return 返回创建的 CodeVisionEntry, 如果无法解析路径则返回 null
     */
    @Nullable
    private CodeVisionEntry createAnnotateEntry(@NotNull Context context,
                                                @NotNull MarkdownImage markdownImage,
                                                @NotNull ImageEditorEnum editor) {
        String absolutePath = resolveAbsolutePath(context, markdownImage);
        if (StringUtils.isBlank(absolutePath)) {
            return null;
        }

        String editorName = editor.getName();
        Icon editorIcon = getEditorIcon(editor);
        return new ClickableTextCodeVisionEntry(
            MikBundle.message("mik.codevision.annotate", editorName),
            getId(),
            (event, currentEditor) -> {
                openInImageEditor(context, absolutePath, editor);
                return Unit.INSTANCE;
            },
            editorIcon,
            MikBundle.message("mik.codevision.annotate", editorName),
            MikBundle.message("mik.codevision.annotate.tooltip", editorName),
            Collections.emptyList()
        );
    }

    /**
     * 在外部图片编辑器中打开指定路径的图片
     * <p> 根据不同的编辑器类型执行不同的处理逻辑:
     * <ul>
     *   <li>CleanShot X: 将路径编码后通过 scheme 打开</li>
     *   <li>Shottr: 先将图片复制到剪切板, 然后调用 scheme</li>
     * </ul>
     * 如果打开失败, 将记录日志并显示错误提示.
     *
     * @param context      上下文信息, 包含项目等信息
     * @param absolutePath 要打开的图片的绝对路径
     * @param editor       当前选中的图片编辑器
     */
    private void openInImageEditor(@NotNull Context context,
                                   @NotNull String absolutePath,
                                   @NotNull ImageEditorEnum editor) {
        VirtualFile imageVirtualFile = LocalFileSystem.getInstance().findFileByPath(absolutePath);
        if (imageVirtualFile == null) {
            Messages.showErrorDialog(
                context.project,
                MikBundle.message("mik.codevision.annotate.path.missing", editor.getName()),
                MikBundle.message("mik.codevision.title")
                                    );
            return;
        }

        // MessageBusConnection connection = registerRefreshOnImageChange(context, imageVirtualFile);

        try {
            if (editor == ImageEditorEnum.DRAWIO) {
                // 1. 尝试使用 drawio idea 插件打开
                // 2. 如果失败, 则尝试使用桌面端应用打开
                if (!openByDiagramsNetPlugin(context, imageVirtualFile)) {
                    openByDesktop(context, absolutePath, editor);
                }
            } else if (editor == ImageEditorEnum.SHOTTR) {
                processByShottr(context, absolutePath, editor);
            } else {
                // CleanShot X 保持现有逻辑: 将路径编码后通过 scheme(https://cleanshot.com/docs-api) 打开
                String encodedPath = URLEncoder.encode(absolutePath, StandardCharsets.UTF_8).replace("+", "%20");
                String url = editor.getScheme() + encodedPath;
                BrowserUtil.browse(url);
            }
        } catch (Exception e) {
            // disconnectQuietly(connection);
            log.trace("打开图片编辑器失败: {}", absolutePath, e);
            Messages.showErrorDialog(
                context.project,
                MikBundle.message("mik.codevision.annotate.path.missing", editor.getName()),
                MikBundle.message("mik.codevision.title")
                                    );
        }
    }

    /**
     * 使用 Diagrams.net 插件打开指定的图片文件
     * <p> 检查 Diagrams.net 插件是否已安装, 并在项目中打开指定的图片文件. 如果插件未安装或项目为空, 则返回 false.</p>
     *
     * @param context          上下文信息, 包含项目等信息
     * @param imageVirtualFile 要打开的图片的虚拟文件
     * @return 如果成功打开图片文件或插件未安装则返回 true, 否则返回 false
     */
    private boolean openByDiagramsNetPlugin(@NotNull Context context, @NotNull VirtualFile imageVirtualFile) {
        if (!isDiagramsNetPluginInstalled()) {
            return false;
        }
        if (context.project == null) {
            return false;
        }
        FileEditorManager.getInstance(context.project).openFile(imageVirtualFile, true, true);
        return true;
    }

    /**
     * 检查 Diagrams.net 插件是否已安装
     * <p> 通过调用 {@link #isPluginInstalledById(String)} 方法来判断指定 ID 的插件是否已安装.</p>
     *
     * @return 如果插件已安装则返回 true, 否则返回 false
     */
    private boolean isDiagramsNetPluginInstalled() {
        return isPluginInstalledById("de.docs_as_co.intellij.plugin.diagramsnet");
    }

    /**
     * 检查指定插件 ID 的插件是否已安装并加载
     * <p> 通过 PluginManagerCore 获取插件描述符, 并判断插件是否已加载.</p>
     *
     * @param pluginId 插件的唯一标识符
     * @return 如果插件已安装并加载, 则返回 true; 否则返回 false
     */
    private boolean isPluginInstalledById(@NotNull String pluginId) {
        IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId(pluginId));
        return plugin != null && PluginManagerCore.isLoaded(PluginId.getId(pluginId));
    }

    /**
     * 在桌面环境中打开指定路径的图片文件
     * <p> 根据操作系统的不同, 使用相应的命令打开图片文件. 如果图片文件不存在或无法打开, 则显示错误提示.</p>
     *
     * @param context      上下文信息, 包含项目等信息
     * @param absolutePath 要打开的图片文件的绝对路径
     * @param editor       当前选中的图片编辑器
     * @throws IOException 如果在打开图片文件时发生 I/O 错误
     */
    private void openByDesktop(@NotNull Context context,
                               @NotNull String absolutePath,
                               @NotNull ImageEditorEnum editor) throws IOException {
        File imageFile = new File(absolutePath);
        if (!imageFile.exists() || !imageFile.isFile()) {
            Messages.showErrorDialog(
                context.project,
                MikBundle.message("mik.codevision.annotate.path.missing", editor.getName()),
                MikBundle.message("mik.codevision.title")
                                    );
            return;
        }

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(imageFile);
                return;
            }
        }

        if (SystemInfoRt.isMac) {
            new ProcessBuilder("", absolutePath).start();
        } else if (SystemInfoRt.isWindows) {
            new ProcessBuilder("cmd", "/c", "start", "", absolutePath).start();
        } else if (SystemInfoRt.isLinux) {
            new ProcessBuilder("xdg-open", absolutePath).start();
        } else {
            Messages.showErrorDialog(
                context.project,
                MikBundle.message("mik.codevision.annotate.path.missing", editor.getName()),
                MikBundle.message("mik.codevision.title")
                                    );
        }
    }

    /**
     * 注册监听图片文件内容变更的消息总线连接
     * <p> 当图片文件内容发生变化时, 刷新 VFS 和 Markdown 预览. 如果项目为空, 则返回 null.</p>
     * todo-dong4j : (2026.12.30 16:05) [未生效]
     *
     * @param context          上下文信息, 包含项目等信息
     * @param imageVirtualFile 要监听的图片的虚拟文件
     * @return 消息总线连接对象, 如果项目为空则返回 null
     */
    @Nullable
    private MessageBusConnection registerRefreshOnImageChange(@NotNull Context context, @NotNull VirtualFile imageVirtualFile) {
        if (context.project == null) {
            return null;
        }

        MessageBusConnection connection = context.project.getMessageBus().connect(context.project);
        connection.subscribe(VFS_CHANGES, new BulkFileListener() {
            /**
             * 在文件事件处理后执行的操作
             * <p> 遍历文件事件列表, 检查是否有文件内容变更事件, 并且变更的文件是否为目标图像文件.
             * 如果是, 则在应用程序的事件调度线程中刷新图像和 Markdown 预览, 并断开连接.
             *
             * @param events 文件事件列表
             */
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                String imagePath = imageVirtualFile.getPath();
                for (VFileEvent event : events) {
                    VirtualFile changedFile = event.getFile();
                    boolean pathMatch = imagePath.equals(event.getPath())
                                        || (changedFile != null && imagePath.equals(changedFile.getPath()));
                    if (!pathMatch) {
                        continue;
                    }
                    ApplicationManager.getApplication().invokeLater(() -> refreshImageAndMarkdownPreview(context, imageVirtualFile));
                    connection.disconnect();
                    break;
                }
            }
        });
        return connection;
    }

    /**
     * 刷新图片和 Markdown 预览
     * <p> 在图片文件内容发生变化时, 刷新 VFS 和 Markdown 预览. 如果项目或虚拟文件为空, 则直接返回.</p>
     *
     * @param context          上下文信息, 包含项目等信息
     * @param imageVirtualFile 要刷新的图片的虚拟文件
     */
    private void refreshImageAndMarkdownPreview(@NotNull Context context, @NotNull VirtualFile imageVirtualFile) {
        VirtualFile refreshFile = imageVirtualFile;
        if (!refreshFile.isValid()) {
            refreshFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(imageVirtualFile.getPath());
        }
        if (refreshFile != null) {
            VfsUtil.markDirtyAndRefresh(false, false, true, refreshFile);
        }

        if (context.project == null || context.virtualFile == null) {
            return;
        }

        FileEditor[] editors = FileEditorManager.getInstance(context.project).getAllEditors(context.virtualFile);
        for (FileEditor editor : editors) {
            if (editor instanceof TextEditorWithPreview textEditorWithPreview) {
                tryReloadPreview(textEditorWithPreview.getPreviewEditor());
                continue;
            }
            tryReloadPreview(editor);
        }
    }

    /**
     * 尝试重新加载 Markdown 预览
     * <p> 遍历一系列可能的方法名 (如 reload,reloadPanel,reloadHtml 等), 检查是否存在且无参数的方法, 并调用该方法以刷新 Markdown 预览. 如果找到并成功调用, 则返回 true; 否则返回 false.</p>
     *
     * @param editor 文件编辑器对象, 可能为 null
     * @return 如果成功调用其中一个方法以刷新预览, 则返回 true; 否则返回 false
     */
    private boolean tryReloadPreview(@Nullable FileEditor editor) {
        if (editor == null) {
            return false;
        }

        for (String methodName : new String[] {"reload", "reloadPanel", "reloadHtml", "invalidateHtml", "render", "reparseMarkdown",
                                               "refresh"}) {
            try {
                Method method = ReflectionUtil.getMethod(editor.getClass(), methodName);
                if (method == null) {
                    method = findNoArgMethod(editor.getClass(), methodName);
                }
                if (method != null && method.getParameterCount() == 0) {
                    method.setAccessible(true);
                    method.invoke(editor);
                    return true;
                }
            } catch (Exception e) {
                log.trace("刷新 Markdown 预览失败: {}", methodName, e);
            }
        }
        return false;
    }

    @Nullable
    private Method findNoArgMethod(@NotNull Class<?> editorClass, @NotNull String methodName) {
        Class<?> current = editorClass;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 0) {
                    return method;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    /**
     * 安静地断开消息总线连接
     * <p> 如果连接不为空, 则尝试断开连接. 如果断开过程中发生异常, 则记录错误日志.</p>
     *
     * @param connection 要断开的消息总线连接
     */
    private void disconnectQuietly(@Nullable MessageBusConnection connection) {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception e) {
                log.trace("断开 VFS 监听连接失败", e);
            }
        }
    }

    /**
     * 使用 Shottr 处理图片文件
     * <p> 将指定路径的图片文件读取并设置到剪贴板, 然后通过 Shottr 的方案 URL 打开图片编辑器. 如果文件不存在或无法读取, 会显示错误提示.
     * <a href="https://shottr.cc/kb/urlschemes">...</a>
     *
     * @param context      上下文信息
     * @param absolutePath 图片文件的绝对路径
     * @param editor       图片编辑器类型
     */
    private static void processByShottr(@NotNull Context context, @NotNull String absolutePath, @NotNull ImageEditorEnum editor) {
        // Shottr 需要先将图片复制到剪切板, 然后调用 scheme
        File imageFile = new File(absolutePath);
        if (!imageFile.exists() || !imageFile.isFile()) {
            log.trace("图片文件不存在: {}", absolutePath);
            Messages.showErrorDialog(
                context.project,
                MikBundle.message("mik.codevision.annotate.path.missing", editor.getName()),
                MikBundle.message("mik.codevision.title")
                                    );
            return;
        }

        try {
            // 读取图片文件
            Image image = ImageIO.read(imageFile);
            if (image == null) {
                log.trace("无法读取图片文件: {}", absolutePath);
                Messages.showErrorDialog(
                    context.project,
                    MikBundle.message("mik.codevision.annotate.path.missing", editor.getName()),
                    MikBundle.message("mik.codevision.title")
                                        );
                return;
            }

            // 将图片复制到剪切板
            ImageUtils.setImageToClipboard(image);

            // 调用 Shottr 的 scheme (不需要路径参数)
            String url = editor.getScheme();
            BrowserUtil.browse(url);
        } catch (IOException e) {
            log.trace("读取图片文件失败: {}", absolutePath, e);
            Messages.showErrorDialog(
                context.project,
                MikBundle.message("mik.codevision.annotate.path.missing", editor.getName()),
                MikBundle.message("mik.codevision.title")
                                    );
        }
    }

    /**
     * 根据配置的图片编辑器选择对应的图标
     *
     * @param editor 当前选中的图片编辑器
     * @return 对应的编辑器图标
     */
    @NotNull
    private Icon getEditorIcon(@NotNull ImageEditorEnum editor) {
        return switch (editor) {
            case SHOTTR -> MikIcons.SHOTTR;
            case CLEANSHOT_X -> MikIcons.CLEANSHOTX;
            case DRAWIO -> MikIcons.DRAWIO;
        };
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
