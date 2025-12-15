package info.dong4j.idea.plugin.action.menu;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.MarkdownFileFilter;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.settings.MoveToOtherOssSettingsDialog;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ActionUtils;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 图床迁移操作类:在目录, 文件和编辑器中生效
 * <p>
 * 用于执行图床迁移任务，支持对 Markdown 内容或目录中的图片进行批量迁移操作。该类主要处理迁移逻辑的初始化、执行以及与用户交互的界面设置。
 * <p>
 * 该类基于 IntelliJ 平台的 AnAction 接口实现，用于在 IDE 中提供一个可配置的迁移操作功能。通过对话框收集迁移参数，如目标域名、云存储类型等，并根据参数状态控制操作按钮的可用性。
 * <p>
 * 智能判断功能：在编辑器中通过鼠标右键触发时，会先判断当前光标所在行是否为有效的Markdown图片标签，
 * 如果是则仅处理当前标签，否则处理整个文件。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@SuppressWarnings("D")
@Slf4j
public final class ImageMigrationAction extends AnAction {
    /** 移动所有 */
    private static final String MOVE_ALL = "ALL";

    /**
     * 更新操作
     * <p>
     * 执行更新操作，设置动作可用状态，并显示图床迁移图标和标题
     * 标题固定为"迁移到其他图床"
     *
     * @param event 事件对象，包含操作上下文信息
     * @since 0.0.1
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        // 检查全局开关
        MikState state = MikPersistenComponent.getInstance().getState();
        if (!state.isEnablePlugin()) {
            event.getPresentation().setEnabled(false);
            return;
        }

        // 调用基础的可用性检查
        ActionUtils.isAvailable(true, event, MikIcons.MIGRATION, MarkdownContents.MARKDOWN_TYPE_NAME);

        // 设置固定的菜单标题
        event.getPresentation().setText(MikBundle.message("mik.action.menu.migration.title"));
        event.getPresentation().setDescription(MikBundle.message("mik.action.menu.migration.description"));
    }

    /**
     * 处理移动文件到其他云存储设置的Action操作
     * <p>
     * 根据用户在对话框中选择的云存储类型和域名，执行移动文件操作，并更新相关配置。
     * <p>
     * 智能判断：如果是在编辑器中通过鼠标右键触发，会先判断当前光标所在行是否为有效的Markdown图片标签，
     * 如果是则仅处理当前标签，否则处理整个文件。
     *
     * @param event Action事件对象，包含用户操作相关信息
     * @since 0.0.1
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        Project project = event.getProject();
        if (project == null) {
            return;
        }

        // 智能判断：检查光标所在行是否为有效的图片标签（在对话框显示之前检查）
        Map<Document, List<MarkdownImage>> waitingProcessMap = ActionUtils.checkAndGetSingleImageTag(event, project);

        // 如果是编辑器中的单个合法标签，直接弹出可用图床选择，无需对话框
        if (waitingProcessMap != null && !waitingProcessMap.isEmpty()) {
            Editor editor = event.getData(PlatformDataKeys.EDITOR);
            showQuickTargetChooser(project, event, editor, waitingProcessMap);
            return;
        }

        MoveToOtherOssSettingsDialog dialog = new MoveToOtherOssSettingsDialog();
        if (!dialog.showAndGet()) {
            return;
        }

        // 获取域名输入（不管本地存储还是云存储，都需要输入）
        String domain = dialog.getDomainText().trim();
        if (StringUtils.isBlank(domain)) {
            return;
        }

        // 检查是否是本地存储
        boolean isLocalStorage = dialog.isLocalStorage();
        CloudEnum cloudEnum = dialog.getSelectedCloudEnum();

        // 本地存储时 cloudEnum 为 null
        String clientName = isLocalStorage
                            ? MikBundle.message("oss.title.local")
                            : (cloudEnum != null ? cloudEnum.title : "");

        // 获取临时存储路径（仅本地存储且无全局配置时）
        String temporaryStoragePath = dialog.getStoragePath();

        EventData data = new EventData()
            .setAction("ImageMigrationAction")
            .setActionEvent(event)
            .setProject(project)
            .setClient(isLocalStorage || cloudEnum == null ? null : ClientUtils.getClient(cloudEnum))
            .setClientName(clientName)
            .setTemporaryStoragePath(temporaryStoragePath);

        // 如果找到单个图片标签，直接设置到 EventData 中，跳过文件解析步骤
        if (waitingProcessMap != null && !waitingProcessMap.isEmpty()) {
            data.setWaitingProcessMap(waitingProcessMap);
            log.debug("检测到光标所在行为有效的图片标签，仅处理当前标签");
        } else {
            log.debug("未检测到光标所在行为有效的图片标签，将处理整个文件");
        }

        // http://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html
        PropertiesComponent propComp = PropertiesComponent.getInstance();
        // 过滤掉配置用户输入后的其他标签
        propComp.setValue(MarkdownFileFilter.FILTER_KEY, domain.equals(MOVE_ALL) ? "" : domain);

        new ActionTask(project,
                       MikBundle.message("mik.action.move.process", clientName),
                       ActionManager.buildImageMigrationChain(data)).queue();
    }

    /**
     * 显示快速目标选择器弹窗
     * <p>
     * 收集可用的迁移目标, 若无目标则提示用户无可用云服务, 否则创建动作组并显示弹窗供用户选择目标
     *
     * @param project           当前项目
     * @param event             动作事件
     * @param editor            编辑器实例, 可能为 null
     * @param waitingProcessMap 等待处理的文档与图片映射关系
     */
    private void showQuickTargetChooser(@NotNull Project project,
                                        @NotNull AnActionEvent event,
                                        @Nullable Editor editor,
                                        @NotNull Map<Document, List<MarkdownImage>> waitingProcessMap) {
        List<MigrationTarget> targets = collectAvailableTargets();
        if (targets.isEmpty()) {
            Messages.showInfoMessage(project,
                                     MikBundle.message("mik.codevision.no.available.cloud"),
                                     MikBundle.message("mik.codevision.title"));
            return;
        }

        DefaultActionGroup group = new DefaultActionGroup();
        for (MigrationTarget target : targets) {
            group.add(new MigrationTargetAction(target, project, event, waitingProcessMap));
        }

        ListPopup popup = JBPopupFactory.getInstance()
            .createActionGroupPopup(
                MikBundle.message("mik.codevision.select.cloud"),
                group,
                event.getDataContext(),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                false
                                   );

        if (editor != null) {
            popup.showInBestPositionFor(editor);
        } else {
            popup.showInBestPositionFor(event.getDataContext());
        }
    }

    /**
     * 收集可用的迁移目标
     * <p>
     * 从云环境枚举中创建迁移目标列表, 并检查本地迁移路径是否可用. 如果可用, 则将本地迁移目标添加到列表中.
     *
     * @return 包含所有可用迁移目标的列表
     */
    private List<MigrationTarget> collectAvailableTargets() {
        List<MigrationTarget> cloudTargets = java.util.Arrays.stream(CloudEnum.values())
            .map(cloud -> new MigrationTarget(cloud, cloud.title, ClientUtils.getClient(cloud)))
            .filter(target -> ClientUtils.isEnable(target.client))
            .collect(Collectors.toList());

        // 本地存储：需要配置了存储路径才认为可用
        boolean localAvailable = StringUtils.isNotBlank(MikState.getInstance().getCurrentInsertPath());
        if (localAvailable) {
            cloudTargets.add(new MigrationTarget(null, MikBundle.message("oss.title.local"), null));
        }

        return cloudTargets;
    }

    /**
         * 迁移目标内部类
         * <p>
         * 用于封装迁移操作的目标信息, 包括云平台类型, 显示名称以及对应的 OSS 客户端实例, 主要用于迁移任务的配置和执行过程中的目标识别与操作.
         *
         * @author zeka.stack.team
         * @version 1.0.0
         * @email "mailto:zeka.stack@gmail.com"
         * @date 2025.12.15
         * @since 1.0.0
         */
        private record MigrationTarget(CloudEnum cloudEnum, String displayName, OssClient client) {
            private MigrationTarget(@Nullable CloudEnum cloudEnum, @NotNull String displayName, @Nullable OssClient client) {
                this.cloudEnum = cloudEnum;
                this.displayName = displayName;
                this.client = client;
            }
        }

    /**
     * 迁移目标操作类
     * <p>
     * 该类继承自 AnAction, 用于处理与迁移目标相关的操作, 主要负责触发图像迁移任务并传递必要的上下文信息.
     * 该类为内部静态类, 用于在特定迁移目标被选中时执行迁移流程.
     *
     * @author zeka.stack.team
     * @version 1.0.0
     * @email "mailto:zeka.stack@gmail.com"
     * @date 2025.12.15
     * @since 1.0.0
     */
    private static final class MigrationTargetAction extends AnAction {
        private final MigrationTarget target;
        private final Project project;
        private final AnActionEvent sourceEvent;
        private final Map<Document, List<MarkdownImage>> waitingProcessMap;

        private MigrationTargetAction(@NotNull MigrationTarget target,
                                      @NotNull Project project,
                                      @NotNull AnActionEvent sourceEvent,
                                      @NotNull Map<Document, List<MarkdownImage>> waitingProcessMap) {
            super(target.displayName, null, MikIcons.MIGRATION);
            this.target = target;
            this.project = project;
            this.sourceEvent = sourceEvent;
            this.waitingProcessMap = waitingProcessMap;
        }

        /**
         * 执行图像迁移操作
         * <p>
         * 根据传入的事件对象创建事件数据, 并配置相关参数, 然后启动图像迁移任务.
         *
         * @param e 与操作相关的事件对象
         * @throws NullPointerException 如果传入的事件对象为 null
         */
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            EventData data = new EventData()
                .setAction("ImageMigrationAction")
                .setActionEvent(sourceEvent)
                .setProject(project)
                .setWaitingProcessMap(waitingProcessMap)
                .setClient(target.cloudEnum == null ? null : target.client)
                .setClientName(target.displayName);

            // 单张标签迁移不需要过滤
            PropertiesComponent.getInstance().setValue(MarkdownFileFilter.FILTER_KEY, "");

            new ActionTask(project,
                           MikBundle.message("mik.action.move.process", target.displayName),
                           ActionManager.buildImageMigrationChain(data)).queue();
        }
    }


    /**
     * 获取动作更新线程
     *
     * <p>指定 update 方法在后台线程中执行，避免阻塞事件调度线程(EDT)。
     * 提高 UI 响应性，防止界面卡顿。
     *
     * @return ActionUpdateThread.BGT 后台线程
     * @see ActionUpdateThread#BGT
     */
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        // 在后台线程中执行 update，避免阻塞 EDT
        return ActionUpdateThread.BGT;
    }
}
