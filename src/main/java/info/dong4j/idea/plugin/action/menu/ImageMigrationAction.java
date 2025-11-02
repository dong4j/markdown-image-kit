package info.dong4j.idea.plugin.action.menu;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.MarkdownFileFilter;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MoveToOtherOssSettingsDialog;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ActionUtils;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.NotNull;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 图床迁移操作类:在目录, 文件和编辑器中生效
 * <p>
 * 用于执行图床迁移任务，支持对 Markdown 内容或目录中的图片进行批量迁移操作。该类主要处理迁移逻辑的初始化、执行以及与用户交互的界面设置。
 * <p>
 * 该类基于 IntelliJ 平台的 AnAction 接口实现，用于在 IDE 中提供一个可配置的迁移操作功能。通过对话框收集迁移参数，如目标域名、云存储类型等，并根据参数状态控制操作按钮的可用性。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@SuppressWarnings("D")
@Slf4j
public final class ImageMigrationAction extends AnAction {
    /** 默认域名提示信息，用于当域名字段未填写时显示的提示内容 */
    private static final String DOMAIN_DEFAULT_MESSAGE = MikBundle.message("mik.panel.message.domain-field");
    /** 连接超时时间，单位为毫秒，默认设置为 5 秒 */
    private static final int CONNECTION_TIMEOUT = 5 * 1000;
    /** 读取超时时间，单位为毫秒，默认设置为 10 秒 */
    private static final int READ_TIMEOUT = 10 * 1000;
    /** 移动所有 */
    private static final String MOVE_ALL = "ALL";

    /**
     * 更新操作
     * <p>
     * 执行更新操作，设置动作可用状态，并显示指定图标和类型名称
     *
     * @param event 事件对象，包含操作上下文信息
     * @since 0.0.1
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        ActionUtils.isAvailable(true, event, MikIcons.MIK, MarkdownContents.MARKDOWN_TYPE_NAME);
    }

    /**
     * 处理移动文件到其他云存储设置的Action操作
     * <p>
     * 根据用户在对话框中选择的云存储类型和域名，执行移动文件操作，并更新相关配置。
     *
     * @param event Action事件对象，包含用户操作相关信息
     * @since 0.0.1
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        Project project = event.getProject();
        if (project != null) {
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

            EventData data = new EventData()
                .setAction("ImageMigrationAction")
                .setActionEvent(event)
                .setProject(project)
                .setClient(isLocalStorage || cloudEnum == null ? null : ClientUtils.getClient(cloudEnum))
                .setClientName(clientName);

            // http://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html
            PropertiesComponent propComp = PropertiesComponent.getInstance();
            // 过滤掉配置用户输入后的其他标签
            propComp.setValue(MarkdownFileFilter.FILTER_KEY, domain.equals(MOVE_ALL) ? "" : domain);

            new ActionTask(project,
                           MikBundle.message("mik.action.move.process", clientName),
                           ActionManager.buildMoveImageChain(data)).queue();
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
