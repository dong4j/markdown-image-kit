package info.dong4j.idea.plugin.codevision;

import com.intellij.codeInsight.codeVision.CodeVisionAnchorKind;
import com.intellij.codeInsight.codeVision.CodeVisionEntry;
import com.intellij.codeInsight.codeVision.CodeVisionProvider;
import com.intellij.codeInsight.codeVision.CodeVisionRelativeOrdering;
import com.intellij.codeInsight.codeVision.CodeVisionState;
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.handler.FinalChainHandler;
import info.dong4j.idea.plugin.chain.handler.ImageDownloadHandler;
import info.dong4j.idea.plugin.chain.handler.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.handler.ImageStorageHandler;
import info.dong4j.idea.plugin.chain.handler.ImageUploadHandler;
import info.dong4j.idea.plugin.chain.handler.WriteToDocumentHandler;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.MarkdownUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import icons.MikIcons;
import kotlin.Pair;
import kotlin.Unit;
import lombok.extern.slf4j.Slf4j;

/**
 * Markdown 图片代码视图提供者类
 * <p>
 * 该类实现了 CodeVisionProvider 接口, 用于在 Markdown 文件中提供图片相关的代码视图功能, 包括下载图片到本地和上传图片到图床的操作.
 * 主要用于在编辑器中为 Markdown 图片提供交互式操作入口, 如下载或上传.
 *
 * @author zeka.stack.team
 * @version 1.0.0
 * @email "mailto:zeka.stack@gmail.com"
 * @date 2025.12.15
 * @since 1.0.0
 */
@Slf4j
@SuppressWarnings("UnstableApiUsage")
public class MarkdownImageCodeVisionProvider implements CodeVisionProvider<MarkdownImageCodeVisionProvider.Context> {
    /** 提供者标识符, 用于指定代码视觉功能的图像处理提供者 */
    private static final String PROVIDER_ID = "markdown.image.kit.code.vision";

    /**
     * 获取提供者的 ID
     * <p>
     * 返回当前提供者的唯一标识 ID
     *
     * @return 提供者的 ID
     */
    @Override
    public @NotNull String getId() {
        return PROVIDER_ID;
    }

    /**
     * 获取名称
     * <p>
     * 返回与当前上下文相关的名称信息, 通常用于界面或资源标识.
     *
     * @return 名称, 不会为 null
     */
    @Override
    public @NotNull String getName() {
        return MikBundle.message("mik.codevision.title");
    }

    /**
     * 检查插件是否对指定项目可用
     * <p>
     * 从持久化组件中获取当前状态, 并检查是否启用了插件.
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
     * 获取默认的锚点类型
     * <p>
     * 返回用于定位的默认锚点类型, 该类型表示在代码视图中的顶部位置.
     *
     * @return 默认的锚点类型, 值为 {@link CodeVisionAnchorKind#Top}
     */
    @Override
    public @NotNull CodeVisionAnchorKind getDefaultAnchor() {
        return CodeVisionAnchorKind.Top;
    }

    /**
     * 获取相对排序信息
     * <p>
     * 返回一个包含相对排序信息的列表, 当前仅包含第一个实例的排序信息.
     *
     * @return 相对排序信息的列表, 元素类型为 {@link CodeVisionRelativeOrdering}
     */
    @Override
    public @NotNull List<CodeVisionRelativeOrdering> getRelativeOrderings() {
        return Collections.singletonList(CodeVisionRelativeOrdering.CodeVisionRelativeOrderingFirst.INSTANCE);
    }

    /**
     * 在 UI 线程上预计算上下文信息
     * <p>
     * 通过编辑器获取项目信息, 并检查是否为 Markdown 文件以及插件是否启用, 最终返回相应的上下文对象.
     *
     * @param editor 编辑器实例, 用于获取当前文档和项目信息
     * @return 预计算的上下文对象, 若条件不满足则返回空上下文
     * @throws NullPointerException 如果编辑器或项目为 null, 返回空上下文
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
     * 为编辑器计算代码视图条目
     * <p>
     * 根据给定的编辑器和上下文信息, 遍历文档中的每一行, 分析是否存在非法或有效的 Markdown 图像标记, 并为每个有效标记创建对应的代码视图条目.
     *
     * @param editor  编辑器实例, 用于获取当前文档内容
     * @param context 上下文信息, 包含是否可用等状态
     * @return 包含文本范围和对应代码视图条目的列表, 如果上下文不可用则返回空列表
     */
    @Override
    public @NotNull CodeVisionState computeCodeVision(@NotNull Editor editor, @NotNull Context context) {
        if (!context.available) {
            return new CodeVisionState.Ready(Collections.emptyList());
        }

        Document document = editor.getDocument();
        return ReadAction.compute(() -> {
            List<Pair<TextRange, CodeVisionEntry>> entries = new ArrayList<>();
            int lineCount = document.getLineCount();
            for (int line = 0; line < lineCount; line++) {
                int lineStartOffset = document.getLineStartOffset(line);
                int lineEndOffset = document.getLineEndOffset(line);
                TextRange lineRange = TextRange.create(lineStartOffset, lineEndOffset);
                String lineText = document.getText(lineRange);

                if (MarkdownUtils.illegalImageMark(context.project, lineText)) {
                    continue;
                }

                MarkdownImage markdownImage = MarkdownUtils.analysisImageMark(context.virtualFile, lineText, line);
                if (markdownImage == null || markdownImage.getLocation() == null) {
                    continue;
                }

                //noinspection DataFlowIssue context.project 不可能为 null 的
                CodeVisionEntry entry = createEntryForImage(context.project, markdownImage);
                if (entry != null) {
                    entries.add(new Pair<>(lineRange, entry));
                }
            }
            if (entries.isEmpty()) {
                return new CodeVisionState.Ready(Collections.emptyList());
            }
            return new CodeVisionState.Ready(entries);
        });
    }

    /**
     * 为图片创建代码视图条目
     * <p>
     * 根据图片的位置 (本地或网络) 创建对应的可点击文本代码视图条目. 若图片位置未知, 则返回 null.
     *
     * @param project       项目对象
     * @param markdownImage Markdown 图片对象
     * @return 返回对应的代码视图条目, 若图片位置未知则返回 null
     */
    @SuppressWarnings("DuplicatedCode")
    @Nullable
    private CodeVisionEntry createEntryForImage(@NotNull Project project,
                                                @NotNull MarkdownImage markdownImage) {
        if (markdownImage.getLocation() == ImageLocationEnum.NETWORK) {
            return new ClickableTextCodeVisionEntry(
                MikBundle.message("mik.codevision.download"),
                getId(),
                (event, currentEditor) -> {
                    downloadImageToLocal(project, currentEditor, markdownImage);
                    return Unit.INSTANCE;
                },
                MikIcons.DOWNLOAD,
                MikBundle.message("mik.codevision.download"),
                MikBundle.message("mik.codevision.download.tooltip"),
                Collections.emptyList()
            );
        }

        if (markdownImage.getLocation() == ImageLocationEnum.LOCAL) {
            return new ClickableTextCodeVisionEntry(
                MikBundle.message("mik.codevision.upload"),
                getId(),
                (event, currentEditor) -> {
                    showUploadCloudMenu(project, currentEditor, markdownImage);
                    return Unit.INSTANCE;
                },
                MikIcons.UPLOAD,
                MikBundle.message("mik.codevision.upload"),
                MikBundle.message("mik.codevision.upload.tooltip"),
                Collections.emptyList()
            );
        }

        return null;
    }

    /**
     * 下载图片到本地并处理相关操作
     * <p>
     * 该方法用于下载指定的图片到本地, 并通过一系列处理器完成后续处理步骤, 如图片存储, 标签更改和写入文档等.
     *
     * @param project       当前项目实例
     * @param editor        当前编辑器实例
     * @param markdownImage 要下载的 Markdown 图片对象, 包含图片路径等信息
     * @throws NullPointerException 如果传入的参数为 null, 可能引发异常
     */
    private void downloadImageToLocal(@NotNull Project project,
                                      @NotNull Editor editor,
                                      @NotNull MarkdownImage markdownImage) {
        log.debug("开始下载图片到本地: {}", markdownImage.getPath());

        // 创建 EventData
        EventData data = new EventData()
            .setAction("CodeVisionDownloadImage")
            .setProject(project)
            .setEditor(editor);

        // 创建只包含该图片的处理映射
        buildData(editor, markdownImage, data);

        // 构建处理链
        ActionManager actionManager = new ActionManager(data)
            .addHandler(new ImageDownloadHandler())
            .addHandler(new ImageStorageHandler())
            .addHandler(new ImageLabelChangeHandler())
            .addHandler(new WriteToDocumentHandler())
            .addHandler(new FinalChainHandler());

        // 执行任务
        new ActionTask(project, MikBundle.message("mik.action.download.process"), actionManager).queue();
    }

    /**
     * 显示上传到图床的菜单选项
     * <p>
     * 根据当前项目和编辑器上下文, 查找可用的图床服务, 并生成对应的上传动作菜单. 如果没有任何图床服务可用, 则显示提示信息.
     *
     * @param project       当前项目
     * @param editor        当前编辑器
     * @param markdownImage 要上传的 Markdown 图像信息
     * @throws NullPointerException 如果传入的参数为 null, 可能引发异常
     */
    private void showUploadCloudMenu(@NotNull Project project,
                                     @NotNull Editor editor,
                                     @NotNull MarkdownImage markdownImage) {
        log.debug("显示上传到图床菜单: {}", markdownImage.getPath());

        // 获取所有可用的图床
        List<CloudEnum> availableClouds = new ArrayList<>();
        for (CloudEnum cloudEnum : CloudEnum.values()) {
            OssClient client = ClientUtils.getClient(cloudEnum);
            if (ClientUtils.isEnable(client)) {
                availableClouds.add(cloudEnum);
            }
        }

        if (availableClouds.isEmpty()) {
            // 如果没有可用的图床，显示提示
            Messages.showInfoMessage(
                project,
                MikBundle.message("mik.codevision.no.available.cloud"),
                MikBundle.message("mik.codevision.title")
                                    );
            return;
        }

        // 创建 Action 组
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (CloudEnum cloudEnum : availableClouds) {
            actionGroup.add(new UploadToCloudAction(project, editor, markdownImage, cloudEnum));
        }

        // 创建弹出菜单（类似 usage 的弹出框，没有确认/关闭按钮）
        ListPopup popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                MikBundle.message("mik.codevision.select.cloud"),
                actionGroup,
                DataContext.EMPTY_CONTEXT,
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                false  // 不显示确认按钮
                                   );

        // 在编辑器附近显示
        popup.showInBestPositionFor(editor);
    }

    /**
     * 上传到图床的操作类
     * <p>
     * 该类继承自 AnAction, 用于在 IntelliJ IDEA 中执行将 Markdown 图片上传到指定图床的操作. 它封装了上传所需的上下文信息, 如项目, 编辑器, 图片对象和图床类型, 并通过一系列处理器完成上传流程.
     *
     * @author zeka.stack.team
     * @version 1.0.0
     * @email "mailto:zeka.stack@gmail.com"
     * @date 2025.12.15
     * @since 1.0.0
     */
    private static class UploadToCloudAction extends AnAction {
        /** 项目对象, 表示当前操作的项目 */
        private final Project project;
        /** 编辑器实例 */
        private final Editor editor;
        /**
         * Markdown 图像对象
         * <p>
         * 用于处理和渲染 Markdown 格式的图片
         *
         * @see MarkdownImage
         */
        private final MarkdownImage markdownImage;
        /** 表示云服务的枚举类型 */
        private final CloudEnum cloudEnum;

        /**
         * 构造一个上传到云的 Action 对象
         * <p>
         * 初始化上传到云的操作, 设置项目, 编辑器,Markdown 图片信息和云服务类型
         *
         * @param project       当前项目
         * @param editor        当前编辑器
         * @param markdownImage 要上传的 Markdown 图片信息
         * @param cloudEnum     云服务类型
         * @throws NullPointerException 如果任何参数为 null, 由于参数使用了 @NotNull 注解, 因此在运行时会抛出异常
         */
        public UploadToCloudAction(@NotNull Project project,
                                   @NotNull Editor editor,
                                   @NotNull MarkdownImage markdownImage,
                                   @NotNull CloudEnum cloudEnum) {
            super(cloudEnum.title, null, getCloudIcon(cloudEnum));
            this.project = project;
            this.editor = editor;
            this.markdownImage = markdownImage;
            this.cloudEnum = cloudEnum;
        }

        /**
         * 执行上传图片到图床的操作
         * <p>
         * 该方法在用户触发上传动作时被调用, 记录日志并根据指定的图床类型创建客户端. 如果客户端不可用, 则显示错误提示并返回. 否则, 构建事件数据并启动上传任务.
         *
         * @param e 动作事件对象, 包含触发该动作的相关信息
         * @throws NullPointerException 如果项目或编辑器对象为 null, 可能导致后续操作失败
         */
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            log.debug("上传图片到图床: {} -> {}", markdownImage.getPath(), cloudEnum.title);

            // 获取客户端
            OssClient client = ClientUtils.getClient(cloudEnum);
            if (client == null) {
                com.intellij.openapi.ui.Messages.showErrorDialog(
                    project,
                    MikBundle.message("mik.codevision.client.not.available", cloudEnum.title),
                    MikBundle.message("mik.codevision.title")
                                                                );
                return;
            }

            // 创建 EventData
            EventData data = new EventData()
                .setAction("CodeVisionUploadImage")
                .setProject(project)
                .setEditor(editor)
                .setClient(client)
                .setClientName(cloudEnum.title);

            // 创建只包含该图片的处理映射
            buildData(editor, markdownImage, data);

            // 构建处理链
            ActionManager actionManager = new ActionManager(data)
                .addHandler(new ImageUploadHandler())
                .addHandler(new ImageLabelChangeHandler())
                .addHandler(new WriteToDocumentHandler())
                .addHandler(new FinalChainHandler());

            // 执行任务
            new ActionTask(project,
                           MikBundle.message("mik.action.upload.process", cloudEnum.title),
                           actionManager).queue();
        }

        /**
         * 根据云类型枚举获取对应的图标
         * <p>
         * 无论传入何种云类型枚举值, 均返回上传图标
         *
         * @param cloudEnum 云类型枚举
         * @return 上传图标
         */
        private static Icon getCloudIcon(@NotNull CloudEnum cloudEnum) {
            // 可以根据不同的图床返回不同的图标
            return MikIcons.UPLOAD;
        }
    }

    /**
     * 构建待处理的图片数据映射
     * <p>
     * 将指定的 Markdown 图片添加到与编辑器文档关联的待处理列表中, 并设置到事件数据中
     *
     * @param editor        编辑器实例, 用于获取文档
     * @param markdownImage 要处理的 Markdown 图片对象
     * @param data          事件数据对象, 用于存储待处理的图片映射
     */
    private static void buildData(@NotNull Editor editor, @NotNull MarkdownImage markdownImage, EventData data) {
        Map<Document, List<MarkdownImage>> waitingProcessMap = new HashMap<>(1);
        List<MarkdownImage> imageList = new ArrayList<>(1);
        imageList.add(markdownImage);
        waitingProcessMap.put(editor.getDocument(), imageList);
        data.setWaitingProcessMap(waitingProcessMap);
    }

    /**
     * 上下文类
     * <p>
     * 用于封装与项目和虚拟文件相关的上下文信息, 并表示当前上下文是否可用. 该类为静态内部类, 主要用于提供一个不可变的上下文对象, 用于传递和存储环境相关数据.
     *
     * @author zeka.stack.team
     * @version 1.0.0
     * @email "mailto:zeka.stack@gmail.com"
     * @date 2025.12.15
     * @since 1.0.0
     */
    public static final class Context {
        /** 空的上下文对象, 用于表示没有实际内容的上下文 */
        private static final Context EMPTY = new Context(null, null, false);
        /** 项目对象, 表示当前操作的项目 */
        private final Project project;
        /**
         * 虚拟文件对象
         * <p>
         * 用于表示文件系统的虚拟文件节点
         *
         * @see VirtualFile
         */
        private final VirtualFile virtualFile;
        /** 表示资源是否可用 */
        private final boolean available;

        /**
         * 构造一个 Context 对象
         * <p>
         * 用于初始化 Context 的实例, 包含项目, 虚拟文件和可用性状态
         *
         * @param project     项目对象, 可能为 null
         * @param virtualFile 虚拟文件对象, 可能为 null
         * @param available   表示该上下文是否可用的布尔值
         */
        private Context(@Nullable Project project, @Nullable VirtualFile virtualFile, boolean available) {
            this.project = project;
            this.virtualFile = virtualFile;
            this.available = available;
        }
    }
}
