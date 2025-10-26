package info.dong4j.idea.plugin.action.markdown;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ActionUtils;

import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * 右键上传到 OSS 的基础操作类
 * <p>
 * 该类作为所有右键上传到 OSS 功能的基类，提供通用的逻辑和方法，包括按钮可用性检查、上传操作执行等。
 * 所有具体实现类需继承该类并实现抽象方法，如获取图标、获取名称和获取 OSS 客户端。
 * <p>
 * 该类使用了策略模式，通过子类实现不同的上传逻辑，统一调用上传流程。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2019.03.14
 * @since 0.0.1
 */
@Slf4j
public abstract class UploadActionBase extends AnAction {
    /**
     * 获取图标
     * <p>
     * 返回当前对象对应的图标
     *
     * @return 图标对象
     * @since 0.0.1
     */
    abstract protected Icon getIcon();

    /**
     * 判断当前图床是否可用
     * <p>
     * 返回一个布尔值，表示图床是否已经完成初始化或准备就绪。
     *
     * @return true 表示图床可用，false 表示不可用
     * @since 0.0.1
     */
    boolean available() {
        return true;
    }

    /**
     * 获取 action name
     * <p>
     * 返回当前 action 的名称
     *
     * @return the name
     * @since 0.0.1
     */
    abstract String getName();

    /**
     * 检查 "上传到XXX OSS" 按钮是否可用
     * <p>
     * 根据条件判断按钮是否可启用：
     * 1. 相关测试通过后
     * 2. 如果所有内容均为目录则可用
     * 3. 如果文件类型为Markdown则可用
     *
     * @param event 事件对象
     * @since 0.0.1
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        ActionUtils.isAvailable(this.available(), event, this.getIcon(), MarkdownContents.MARKDOWN_TYPE_NAME);
    }

    /**
     * 执行动作事件，所有子类都走此逻辑，用于进行前置判断和解析 markdown 图片标记
     * <p>
     * 该方法首先刷新虚拟文件系统，确保新添加的文件已被正确加载。然后获取项目对象，并创建事件数据对象，设置相关属性。最后启动后台任务，执行上传处理流程。
     *
     * @param event 事件对象，表示触发的动作事件
     * @since 0.0.1
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // 先刷新一次, 避免才添加的文件未被添加的 VFS 中, 导致找不到文件的问题
        VirtualFileManager.getInstance().syncRefresh();

        Project project = event.getProject();
        if (project != null) {
            EventData data = new EventData()
                .setActionEvent(event)
                .setProject(project)
                // 使用子类的具体 client
                .setClient(this.getClient())
                .setClientName(this.getName());

            // 开启后台任务
            new ActionTask(project, MikBundle.message("mik.action.upload.process", this.getName()), ActionManager.buildUploadChain(data)).queue();
        }
    }

    /**
     * 获取具体上传的客户端，委托给后台任务执行
     * <p>
     * 该方法用于获取配置的OSS客户端实例，具体实现由子类提供。
     *
     * @return the oss client
     * @since 0.0.1
     */
    abstract OssClient getClient();

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
