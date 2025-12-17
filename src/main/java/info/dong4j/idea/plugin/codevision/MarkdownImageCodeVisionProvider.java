package info.dong4j.idea.plugin.codevision;

import com.intellij.codeInsight.codeVision.CodeVisionEntry;
import com.intellij.codeInsight.codeVision.ui.model.ClickableTextCodeVisionEntry;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.handler.FinalChainHandler;
import info.dong4j.idea.plugin.chain.handler.ImageDownloadHandler;
import info.dong4j.idea.plugin.chain.handler.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.handler.ImageStorageHandler;
import info.dong4j.idea.plugin.chain.handler.ImageUploadHandler;
import info.dong4j.idea.plugin.chain.handler.RefreshFileSystemHandler;
import info.dong4j.idea.plugin.chain.handler.WriteToDocumentHandler;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.InsertImageActionEnum;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ClientUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import icons.MikIcons;
import kotlin.Unit;
import lombok.extern.slf4j.Slf4j;

/**
 * Markdown 图片代码视图提供者
 * <p> 该类继承自 AbstractMarkdownImageCodeVisionProvider, 用于为 Markdown 图片提供代码视图功能, 支持图片的下载和上传操作. 根据图片的来源 (本地或网络), 提供相应的操作入口, 如下载到本地或上传至图床.
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2025.12.16
 * @since 1.0.0
 */
@Slf4j
public class MarkdownImageCodeVisionProvider extends AbstractMarkdownImageCodeVisionProvider {
    /** 提供者的唯一标识符 */
    private static final String PROVIDER_ID = "markdown.image.kit.code.vision";

    /**
     * 获取提供者的唯一标识符
     * <p> 返回该 Markdown 图片代码视图提供者的唯一 ID.
     *
     * @return 提供者的唯一标识符
     */
    @Override
    protected @NotNull String getProviderId() {
        return PROVIDER_ID;
    }

    /**
     * 获取提供者的名称
     * <p> 返回当前代码视图提供者的显示名称, 该名称通过资源文件获取.
     *
     * @return 提供者的名称
     */
    @Override
    protected @NotNull String getProviderName() {
        return MikBundle.message("mik.codevision.title");
    }

    /**
     * 根据图片的位置创建对应的代码视图条目
     * <p>根据图片的来源位置 (网络或本地) 创建相应的操作条目, 如下载或上传.
     *
     * @param context       上下文信息
     * @param markdownImage Markdown 图片对象
     * @return 返回对应的代码视图条目列表, 若图片位置不支持则返回空列表
     */
    @Override
    protected @NotNull List<CodeVisionEntry> createEntriesForImage(@NotNull Context context,
                                                                   @NotNull MarkdownImage markdownImage) {
        if (markdownImage.getLocation() == ImageLocationEnum.NETWORK) {
            final MikState instance = MikState.getInstance();
            //noinspection DataFlowIssue 下载时需要先判断配置(todo-dong4j : (2025.12.17 09:53) [在 EventData 添加一个字段, 用于覆写全局配置])
            return instance.getInsertImageAction() == InsertImageActionEnum.NONE
                   || instance.getInsertImageAction() == InsertImageActionEnum.UPLOAD
                   || !instance.isApplyToLocalImages()
                   || !instance.isApplyToNetworkImages()
                   ? Collections.emptyList()
                   : Collections.singletonList(createDownloadEntry(context.project, markdownImage));
        }

        if (markdownImage.getLocation() == ImageLocationEnum.LOCAL) {
            //noinspection DataFlowIssue
            return Collections.singletonList(createUploadEntry(context.project, markdownImage));
        }

        return Collections.emptyList();
    }

    /**
     * 创建下载图片到本地的代码视图条目
     * <p> 生成一个可点击的文本条目, 用于将指定的 Markdown 图片下载到本地.
     *
     * @param project       当前项目
     * @param markdownImage 要下载的 Markdown 图片对象
     * @return 返回一个用于下载图片的代码视图条目
     */
    private CodeVisionEntry createDownloadEntry(@NotNull Project project, @NotNull MarkdownImage markdownImage) {
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

    /**
     * 创建上传到图床的代码视图条目
     * <p> 生成一个可点击的文本条目, 用于触发上传图片到图床的操作.
     *
     * @param project       当前项目
     * @param markdownImage 要上传的 Markdown 图片对象
     * @return 返回一个用于上传操作的 CodeVisionEntry 条目
     */
    private CodeVisionEntry createUploadEntry(@NotNull Project project, @NotNull MarkdownImage markdownImage) {
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

    /**
     * 将图片下载到本地
     * <p> 该方法用于将指定的 Markdown 图片下载到本地, 并通过一系列处理器执行相关操作.
     *
     * @param project       当前项目
     * @param editor        当前编辑器
     * @param markdownImage 要下载的 Markdown 图片对象
     */
    private void downloadImageToLocal(@NotNull Project project,
                                      @NotNull Editor editor,
                                      @NotNull MarkdownImage markdownImage) {
        log.debug("开始下载图片到本地: {}", markdownImage.getPath());

        EventData data = new EventData()
            .setAction("CodeVisionDownloadImage")
            .setProject(project)
            .setEditor(editor);

        buildData(editor, markdownImage, data);

        ActionManager actionManager = new ActionManager(data)
            .addHandler(new ImageDownloadHandler())
            .addHandler(new ImageStorageHandler())
            .addHandler(new ImageLabelChangeHandler())
            .addHandler(new WriteToDocumentHandler())
            .addHandler(new RefreshFileSystemHandler())
            .addHandler(new FinalChainHandler());

        new ActionTask(project, MikBundle.message("mik.action.download.process"), actionManager).queue();
    }

    /**
     * 显示上传到图床的菜单
     * <p> 根据可用的图床服务, 生成上传菜单并显示在编辑器最佳位置
     *
     * @param project       当前项目
     * @param editor        当前编辑器
     * @param markdownImage 要上传的 Markdown 图片对象
     */
    private void showUploadCloudMenu(@NotNull Project project,
                                     @NotNull Editor editor,
                                     @NotNull MarkdownImage markdownImage) {
        log.debug("显示上传到图床菜单: {}", markdownImage.getPath());

        List<CloudEnum> availableClouds = new ArrayList<>();
        for (CloudEnum cloudEnum : CloudEnum.values()) {
            OssClient client = ClientUtils.getClient(cloudEnum);
            if (ClientUtils.isEnable(client)) {
                availableClouds.add(cloudEnum);
            }
        }

        if (availableClouds.isEmpty()) {
            Messages.showInfoMessage(
                project,
                MikBundle.message("mik.codevision.no.available.cloud"),
                MikBundle.message("mik.codevision.title")
                                    );
            return;
        }

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (CloudEnum cloudEnum : availableClouds) {
            actionGroup.add(new UploadToCloudAction(project, editor, markdownImage, cloudEnum));
        }

        ListPopup popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                MikBundle.message("mik.codevision.select.cloud"),
                actionGroup,
                DataContext.EMPTY_CONTEXT,
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                false
                                   );

        popup.showInBestPositionFor(editor);
    }

    /**
     * 上传到图床的操作类
     * <p> 该类继承自 AnAction, 用于在 IntelliJ IDEA 中实现将 Markdown 图片上传到指定图床的功能. 它封装了上传流程中的各个处理步骤, 包括客户端初始化, 数据构建, 事件处理等, 支持多种图床服务.
     *
     * @author dong4j
     * @version 1.0.0
     * @email "mailto:dong4j@gmail.com"
     * @date 2025.12.16
     * @since 1.0.0
     */
    private static class UploadToCloudAction extends AnAction {
        /** 当前项目的引用 */
        private final Project project;
        /** 当前编辑器实例, 用于获取编辑内容和上下文信息 */
        private final Editor editor;
        /** 用于上传的 Markdown 图片信息 */
        private final MarkdownImage markdownImage;
        /** 表示当前上传操作所使用的图床服务类型 */
        private final CloudEnum cloudEnum;

        /**
         * 构造一个用于上传图片到云图床的操作对象
         * <p> 初始化上传到指定云图床的操作, 设置相关参数并调用父类构造函数
         *
         * @param project       当前项目实例
         * @param editor        当前编辑器实例
         * @param markdownImage 要上传的 Markdown 图片对象
         * @param cloudEnum     表示目标云图床的枚举类型
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
         * <p> 该方法用于处理上传图片到指定图床的流程, 包括创建客户端, 构建数据, 添加处理程序并启动任务.
         *
         * @param e ActionEvent 对象, 包含触发此操作的事件信息
         */
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            log.debug("上传图片到图床: {} -> {}", markdownImage.getPath(), cloudEnum.title);

            OssClient client = ClientUtils.getClient(cloudEnum);
            if (client == null) {
                com.intellij.openapi.ui.Messages.showErrorDialog(
                    project,
                    MikBundle.message("mik.codevision.client.not.available", cloudEnum.title),
                    MikBundle.message("mik.codevision.title")
                                                                );
                return;
            }

            EventData data = new EventData()
                .setAction("CodeVisionUploadImage")
                .setProject(project)
                .setEditor(editor)
                .setClient(client)
                .setClientName(cloudEnum.title);

            buildData(editor, markdownImage, data);

            ActionManager actionManager = new ActionManager(data)
                .addHandler(new ImageUploadHandler())
                .addHandler(new ImageLabelChangeHandler())
                .addHandler(new WriteToDocumentHandler())
                .addHandler(new RefreshFileSystemHandler())
                .addHandler(new FinalChainHandler());

            new ActionTask(project,
                           MikBundle.message("mik.action.upload.process", cloudEnum.title),
                           actionManager).queue();
        }

        /**
         * 获取云服务的图标
         * <p> 无论传入哪个云服务枚举值, 均返回相同的上传图标.
         *
         * @param cloudEnum 云服务枚举值, 用于标识图床类型
         * @return 返回一个表示上传操作的图标
         */
        private static Icon getCloudIcon(@NotNull CloudEnum cloudEnum) {
            return MikIcons.UPLOAD;
        }
    }

    /**
     * 构建待处理的图片数据
     * <p> 将指定的 Markdown 图片添加到等待处理的映射表中, 用于后续的图片处理流程
     *
     * @param editor        编辑器实例
     * @param markdownImage 要处理的 Markdown 图片对象
     * @param data          事件数据对象, 用于存储等待处理的图片映射
     */
    private static void buildData(@NotNull Editor editor, @NotNull MarkdownImage markdownImage, EventData data) {
        Map<com.intellij.openapi.editor.Document, List<MarkdownImage>> waitingProcessMap = new HashMap<>(1);
        List<MarkdownImage> imageList = new ArrayList<>(1);
        imageList.add(markdownImage);
        waitingProcessMap.put(editor.getDocument(), imageList);
        data.setWaitingProcessMap(waitingProcessMap);
    }
}
