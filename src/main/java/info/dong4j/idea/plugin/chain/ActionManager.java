package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.progress.ProgressIndicator;

import info.dong4j.idea.plugin.action.intention.ImageMigrationIntentionAction;
import info.dong4j.idea.plugin.action.menu.ImageMigrationAction;
import info.dong4j.idea.plugin.chain.handler.CheckAvailableClientHandler;
import info.dong4j.idea.plugin.chain.handler.FinalChainHandler;
import info.dong4j.idea.plugin.chain.handler.ImageCompressionHandler;
import info.dong4j.idea.plugin.chain.handler.ImageDownloadHandler;
import info.dong4j.idea.plugin.chain.handler.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.handler.ImageRenameHandler;
import info.dong4j.idea.plugin.chain.handler.ImageStorageHandler;
import info.dong4j.idea.plugin.chain.handler.ImageUploadHandler;
import info.dong4j.idea.plugin.chain.handler.ParseMarkdownFileHandler;
import info.dong4j.idea.plugin.chain.handler.RefreshFileSystemHandler;
import info.dong4j.idea.plugin.chain.handler.WriteToDocumentHandler;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.console.MikConsoleView;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * ActionManager
 * <p>
 * 用于管理动作处理链的工具类，支持添加处理节点和回调，并提供执行动作链的方法。该类主要用于处理一系列需要按顺序执行的业务逻辑，例如文件上传、数据处理等场景。
 * <p>
 * 该类使用责任链模式（Chain of Responsibility Pattern）来组织多个处理节点，每个节点可以决定是否处理当前数据，并在处理失败时中断链式调用。
 * <p>
 * 提供了构建上传链和迁移链的静态方法，方便快速创建特定业务场景下的处理流程。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
public class ActionManager {
    /** 处理器链，用于按顺序执行一系列动作处理器 */
    private final List<IActionHandler> handlersChain = new LinkedList<>();
    /** 回调函数列表，用于存储任务执行后的回调操作 */
    @Getter
    private final List<TaskCallback> callbacks = new ArrayList<>();
    /** 事件数据对象，用于封装事件相关的信息 */
    private final EventData data;
    /** 主任务标题 */
    private String mainTaskTitle;

    /**
     * 初始化一个新的 ActionManager 实例。
     * <p>
     * 通过传入的 EventData 对象进行初始化，用于管理事件相关的操作。
     *
     * @param data 事件数据对象，用于初始化 ActionManager
     * @since 0.0.1
     */
    public ActionManager(EventData data) {
        this.data = data;
    }

    /**
     * 添加处理器动作管理器
     * <p>
     * 将指定的处理器添加到处理器链中，并返回当前动作管理器实例，支持链式调用
     *
     * @param handler 要添加的处理器对象
     * @return 当前动作管理器实例，支持链式调用
     * @since 0.0.1
     */
    public ActionManager addHandler(IActionHandler handler) {
        this.handlersChain.add(handler);
        return this;
    }

    /**
     * 根据条件添加动作处理器
     * <p>
     * 如果条件为真，则将指定的动作处理器添加到管理器中，并返回当前对象以便链式调用
     *
     * @param condition 添加动作处理器的条件判断
     * @param handler   要添加的动作处理器
     * @return 当前ActionManager实例，支持链式调用
     */
    public ActionManager addHandler(boolean condition, IActionHandler handler) {
        if (condition) {
            addHandler(handler);
        }
        return this;
    }

    /**
     * 添加回调操作管理器
     * <p>
     * 将指定的回调对象添加到回调列表中，并返回当前操作管理器实例
     *
     * @param callback 要添加的回调对象
     * @return 当前 ActionManager 实例，支持方法链式调用
     * @since 0.0.1
     */
    public ActionManager addCallback(TaskCallback callback) {
        this.callbacks.add(callback);
        return this;
    }

    /**
     * 设置主任务标题
     * <p>
     * 设置主任务标题，用于进度展示
     *
     * @param title 主任务标题
     * @return 当前 ActionManager 实例，支持方法链式调用
     */
    public ActionManager setMainTaskTitle(String title) {
        this.mainTaskTitle = title;
        return this;
    }

    /**
     * 执行处理链中的各个处理器
     * <p>
     * 遍历处理器链，依次调用每个启用的处理器，并更新进度指示器的状态
     * 使用 ProgressTracker 统一管理进度展示，提供更友好的用户体验
     *
     * @param indicator 进度指示器，用于显示处理进度和当前处理的处理器名称
     */
    @SuppressWarnings("D")
    public void invoke(ProgressIndicator indicator) {
        this.data.setIndicator(indicator);

        // 第一步：收集所有启用的 handler 信息
        List<String> enabledHandlerNames = new ArrayList<>();
        List<IActionHandler> enabledHandlers = new ArrayList<>();
        for (IActionHandler handler : this.handlersChain) {
            if (handler.isEnabled(this.data)) {
                enabledHandlerNames.add(handler.getName());
                enabledHandlers.add(handler);
            }
        }

        // 如果没有启用的 handler，直接返回
        if (enabledHandlers.isEmpty()) {
            log.trace("没有启用的处理器");
            return;
        }

        // 第二步：创建 ProgressTracker（仅在 indicator 不为 null 时创建，预览模式下可能为 null）
        ProgressTracker progressTracker = null;
        if (indicator != null) {
            // 使用主任务标题，如果没有设置则使用默认标题
            String taskTitle = this.mainTaskTitle != null ? this.mainTaskTitle : "处理任务";
            progressTracker = new ProgressTracker(indicator, this.data.getProject(), taskTitle, enabledHandlerNames);
        }
        this.data.setProgressTracker(progressTracker);
        this.data.setSize(enabledHandlers.size());

        // 第三步：执行处理器链，使用 ProgressTracker 更新进度
        int stepIndex = 0;
        for (IActionHandler handler : this.handlersChain) {
            if (handler.isEnabled(this.data)) {
                this.data.setIndex(stepIndex);

                // 开始新步骤（仅在 progressTracker 不为 null 时）
                if (progressTracker != null) {
                    progressTracker.startStep(stepIndex);
                }
                
                log.trace("invoke {}", handler.getName());

                // 记录处理器开始执行时间
                long handlerStartTime = System.currentTimeMillis();

                // 执行处理器
                boolean success = handler.execute(this.data);

                // 计算处理器执行耗时
                long handlerDuration = System.currentTimeMillis() - handlerStartTime;

                if (!success) {
                    log.trace("处理器 {} 执行失败，中断处理链", handler.getName());
                    MikConsoleView.printErrorMessage(this.data.getProject(),
                                                     "[✗] 处理器执行失败: " + handler.getName() + " (耗时: " + formatDuration(handlerDuration) +
                                                     ")");
                    break;
                } else {
                    log.trace("处理器 {} 执行成功，耗时: {}ms", handler.getName(), handlerDuration);
                }

                // 步骤完成
                stepIndex++;
            }
        }

        // 完成所有步骤（仅在 progressTracker 不为 null 时）
        if (progressTracker != null) {
            progressTracker.finish();
        }
    }

    /**
     * 格式化时长
     *
     * @param millis 毫秒数
     * @return 格式化后的时长字符串
     */
    private String formatDuration(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        } else if (millis < 60000) {
            return String.format("%.2fs", millis / 1000.0);
        } else {
            long seconds = millis / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%dm %ds", minutes, seconds);
        }
    }

    /**
     * 构建上传流程的动作管理器
     * <p>
     * 根据传入的事件数据创建一个包含多个处理步骤的动作管理器，用于处理文件上传的完整流程。
     *
     * @param data 事件数据
     * @return 动作管理器实例
     * @since 0.0.1
     */
    public static ActionManager buildUploadChain(EventData data) {
        return new ActionManager(data)
            // 解析 markdown 文件
            .addHandler(new ParseMarkdownFileHandler())
            // 图片压缩
            .addHandler(new ImageCompressionHandler())
            // 图片重命名
            .addHandler(new ImageRenameHandler())
            // 检查 client
            .addHandler(new CheckAvailableClientHandler())
            // 图片上传
            .addHandler(new ImageUploadHandler())
            // 标签转换
            .addHandler(new ImageLabelChangeHandler())
            // 写入标签
            .addHandler(new WriteToDocumentHandler())
            // 刷新文件系统
            .addHandler(new RefreshFileSystemHandler())
            // 回收资源
            .addHandler(new FinalChainHandler());
    }

    /**
     * 生成图床迁移任务
     * <p>
     * 根据EventData构建一个用于图床迁移的ActionManager，处理不同迁移场景下的图片标签解析逻辑。
     * 右键批量迁移和意图迁移需要不同的数据解析方式：右键批量迁移直接解析当前文件中的图片标签，仅处理用户指定的标签；意图迁移则解析光标所在行的标签，若标签所在图床与设置图床一致则跳过处理。
     * 使用 DownloadImageHandler 处理网络图片下载。
     *
     * @param data 用于迁移操作的事件数据
     * @return 构建完成的ActionManager实例
     * @since 0.0.1
     * @see ImageMigrationIntentionAction
     * @see ImageMigrationAction
     */
    @SuppressWarnings("D")
    public static ActionManager buildImageMigrationChain(EventData data) {
        // 过滤掉 LOCAL 和用户输入不匹配的标签
        ParseMarkdownFileHandler parseMarkdownFileHandler = new ParseMarkdownFileHandler();
        parseMarkdownFileHandler.setFileFilter((waitingProcessMap, filterString) -> {
            if (waitingProcessMap == null || waitingProcessMap.isEmpty()) {
                return;
            }

            // 收集需要移除的图片
            List<MarkdownImage> toRemove = new ArrayList<>();
            OssClient client = data.getClient();

            for (Map.Entry<Document, List<MarkdownImage>> entry : waitingProcessMap.entrySet()) {
                log.trace("old waitingProcessMap = {}", waitingProcessMap);
                List<MarkdownImage> images = entry.getValue();


                // 过滤图片：排除 LOCAL 和用户输入不匹配的标签，以及已在目标图床的图片
                for (MarkdownImage markdownImage : images) {
                    // 排除 LOCAL 和用户输入不匹配的标签
                    if (markdownImage.getLocation() == ImageLocationEnum.LOCAL
                        || !markdownImage.getPath().contains(filterString)
                        || (client != null && markdownImage.getPath().contains(client.getCloudType().feature))) {

                        log.trace("排除 LOCAL 和用户输入不匹配的标签: {}", markdownImage.getPath());
                        toRemove.add(markdownImage);
                    } else {
                        // 将需要下载的图片标记为 NETWORK 类型，DownloadImageHandler 会处理
                        markdownImage.setLocation(ImageLocationEnum.NETWORK);
                        log.trace("标记为网络图片: {}", markdownImage.getPath());
                    }
                }
            }

            // 移除不需要处理的图片
            for (Map.Entry<Document, List<MarkdownImage>> entry : waitingProcessMap.entrySet()) {
                entry.getValue().removeAll(toRemove);
            }

            log.trace("new waitingProcessMap = {}", waitingProcessMap);
        });


        return new ActionManager(data)
            .addHandler(parseMarkdownFileHandler)
            // 处理 client
            .addHandler(data.getClient() != null, new CheckAvailableClientHandler())
            // 下载网络图片
            .addHandler(new ImageDownloadHandler())
            // 图片重命名
            .addHandler(new ImageRenameHandler())
            // 图片上传
            .addHandler(data.getClient() != null, new ImageUploadHandler())
            // 迁移到本地存储
            .addHandler(data.getClient() == null, new ImageStorageHandler())
            // 标签转换
            .addHandler(new ImageLabelChangeHandler())
            // 写入标签
            .addHandler(new WriteToDocumentHandler())
            // 刷新文件系统
            .addHandler(new RefreshFileSystemHandler())
            // 回收资源
            .addHandler(new FinalChainHandler());
    }
}
