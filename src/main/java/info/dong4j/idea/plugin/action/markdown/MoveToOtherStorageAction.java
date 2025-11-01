package info.dong4j.idea.plugin.action.markdown;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.MarkdownFileFilter;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MoveToOtherOssSettingsDialog;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.swing.JTextFieldHintListener;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ActionUtils;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.DocumentEvent;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * 图床迁移操作类
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
@Slf4j
public final class MoveToOtherStorageAction extends AnAction {
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
            MoveToOtherOssSettingsDialog dialog = showDialog();
            if (dialog == null) {
                return;
            }
            String domain = dialog.getDomain().getText().trim();
            if (StringUtils.isBlank(domain)) {
                return;
            }
            if (!OssState.getStatus(dialog.getCloudComboBox().getSelectedIndex())) {
                return;
            }

            int cloudIndex = dialog.getCloudComboBox().getSelectedIndex();
            CloudEnum cloudEnum = OssState.getCloudType(cloudIndex);

            EventData data = new EventData()
                .setActionEvent(event)
                .setProject(project)
                .setClient(ClientUtils.getClient(cloudEnum))
                // client 有可能为 null, 使用 cloudEnum 安全点
                .setClientName(cloudEnum.title);

            // http://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html
            PropertiesComponent propComp = PropertiesComponent.getInstance();
            // 过滤掉配置用户输入后的其他标签
            propComp.setValue(MarkdownFileFilter.FILTER_KEY, domain.equals(MOVE_ALL) ? "" : domain);

            new ActionTask(project,
                           MikBundle.message("mik.action.move.process", cloudEnum.title),
                           ActionManager.buildMoveImageChain(data)).queue();
        }
    }

    /**
     * 初始化并显示迁移至其他OSS设置的对话框
     * <p>
     * 创建并配置迁移至其他OSS设置的对话框，包括初始化云类型下拉框、绑定输入监听器、设置按钮状态等逻辑。
     * 组件索引与云类型索引相差1，用于适配显示逻辑。
     *
     * @return 迁移至其他OSS设置的对话框实例，若用户取消操作则返回 null
     * @since 0.0.1
     */
    @Nullable
    private static MoveToOtherOssSettingsDialog showDialog() {
        DialogBuilder builder = new DialogBuilder();
        MoveToOtherOssSettingsDialog dialog = new MoveToOtherOssSettingsDialog();
        // 获取设置的默认图床索引, 如果在设置页面中关闭了默认图床, 那就是 CloudEnum.SM_MS_CLOUD (0)
        int index = MikPersistenComponent.getInstance().getState().getDefaultCloudType();
        // 设置选中默认的图床
        dialog.getCloudComboBox().setSelectedIndex(index);
        showMessage(builder, dialog, index);

        dialog.getCloudComboBox().addActionListener(e -> {
            int selectedIndex = dialog.getCloudComboBox().getSelectedIndex();
            showMessage(builder, dialog, selectedIndex);
        });

        dialog.getDomain().getDocument().addDocumentListener(new DocumentAdapter() {
            /**
             * 处理文本内容变化事件，更新操作按钮的可用状态
             * <p>
             * 监听文本变化事件，根据输入内容和云服务状态判断是否启用操作按钮
             *
             * @param e 文本变化事件对象
             */
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                boolean isValidInput = StringUtils.isNotBlank(dialog.getDomain().getText()) && !DOMAIN_DEFAULT_MESSAGE.equals(dialog.getDomain().getText());
                boolean isClientEnable = OssState.getStatus(dialog.getCloudComboBox().getSelectedIndex());
                builder.setOkActionEnabled(isValidInput && isClientEnable);
            }
        });

        dialog.getDomain().addFocusListener(new JTextFieldHintListener(dialog.getDomain(), DOMAIN_DEFAULT_MESSAGE));

        builder.setOkActionEnabled(false);
        builder.setCenterPanel(dialog.getContentPane());
        builder.setTitle(MikBundle.message("picture.migration.plan.title"));
        builder.removeAllActions();
        builder.addOkAction();
        builder.addCancelAction();
        builder.setPreferredFocusComponent(dialog.getCloudComboBox());
        builder.setOkOperation((() -> {
            log.trace("自定义 ok 操作");
            builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
        }));

        if (builder.show() != DialogWrapper.OK_EXIT_CODE) {
            return null;
        }
        return dialog;
    }

    /**
     * 初始化 message 监听更新 ok 按钮可用状态
     * <p>
     * 根据指定的索引获取 OSS 状态，判断是否启用客户端以及输入是否有效，设置 message 文本和颜色，并更新 ok 按钮的可用状态。
     *
     * @param builder DialogBuilder 实例，用于设置 ok 按钮的可用状态
     * @param dialog  MoveToOtherOssSettingsDialog 实例，用于获取 message 和 domain 输入框
     * @param index   指定的 OSS 索引，用于获取状态信息
     */
    private static void showMessage(@NotNull DialogBuilder builder,
                                    @NotNull MoveToOtherOssSettingsDialog dialog,
                                    int index) {
        boolean isClientEnable = OssState.getStatus(index);
        boolean isValidInput = StringUtils.isNotBlank(dialog.getDomain().getText())
                               && !DOMAIN_DEFAULT_MESSAGE.equals(dialog.getDomain().getText());

        dialog.getMessage().setText(isClientEnable ? "" : MikBundle.message("oss.not.available"));
        dialog.getMessage().setForeground(isClientEnable ? JBColor.WHITE : JBColor.RED);
        builder.setOkActionEnabled(isClientEnable && isValidInput);
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
