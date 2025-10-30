package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.io.FileUtil;

import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
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
@Slf4j
public class ActionManager {
    /** 处理器链，用于按顺序执行一系列动作处理器 */
    private final List<IActionHandler> handlersChain = new LinkedList<>();
    /** 回调函数列表，用于存储任务执行后的回调操作 */
    @Getter
    private final List<TaskCallback> callbacks = new ArrayList<>();
    /** 事件数据对象，用于封装事件相关的信息 */
    private final EventData data;

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
     * 执行处理链中的各个处理器
     * <p>
     * 遍历处理器链，依次调用每个启用的处理器，并更新进度指示器的状态
     *
     * @param indicator 进度指示器，用于显示处理进度和当前处理的处理器名称
     */
    public void invoke(ProgressIndicator indicator) {
        int totalProcessed = 0;
        this.data.setIndicator(indicator);
        this.data.setSize(this.handlersChain.size());
        int index = 0;
        for (IActionHandler handler : this.handlersChain) {
            this.data.setIndex(index++);
            if (handler.isEnabled(this.data)) {
                log.trace("invoke {}", handler.getName());
                indicator.setText2(handler.getName());
                if (!handler.execute(this.data)) {
                    break;
                }
            }
            indicator.setFraction(++totalProcessed * 1.0 / this.handlersChain.size());
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
            .addHandler(new ResolveMarkdownFileHandler())
            // 图片压缩
            .addHandler(new ImageCompressionHandler())
            // 图片重命名
            .addHandler(new ImageRenameHandler())
            // 处理 client
            .addHandler(new OptionClientHandler())
            // 图片上传
            .addHandler(new ImageUploadHandler())
            // 标签转换
            .addHandler(new ImageLabelChangeHandler())
            // 写入标签
            .addHandler(new ReplaceToDocument())
            .addHandler(new FinalChainHandler());
    }

    /**
     * 生成图床迁移任务
     * <p>
     * 根据EventData构建一个用于图床迁移的ActionManager，处理不同迁移场景下的图片标签解析逻辑。
     * 右键批量迁移和意图迁移需要不同的数据解析方式：右键批量迁移直接解析当前文件中的图片标签，仅处理用户指定的标签；意图迁移则解析光标所在行的标签，若标签所在图床与设置图床一致则跳过处理。
     *
     * @param data 用于迁移操作的事件数据
     * @return 构建完成的ActionManager实例
     * @since 0.0.1
     */
    @SuppressWarnings("D")
    public static ActionManager buildMoveImageChain(EventData data) {
        // 过滤掉 LOCAL 和用户输入不匹配的标签
        ResolveMarkdownFileHandler resolveMarkdownFileHandler = new ResolveMarkdownFileHandler();
        resolveMarkdownFileHandler.setFileFilter((waitingProcessMap, filterString) -> {
            if (waitingProcessMap != null && !waitingProcessMap.isEmpty()) {
                for (Map.Entry<Document, List<MarkdownImage>> entry : waitingProcessMap.entrySet()) {
                    log.trace("old waitingProcessMap = {}", waitingProcessMap);

                    Iterator<MarkdownImage> iterator = entry.getValue().iterator();
                    while (iterator.hasNext()) {
                        MarkdownImage markdownImage = iterator.next();
                        OssClient client = data.getClient();
                        // 排除 LOCAL 和用户输入不匹配的标签
                        if (markdownImage.getLocation().name().equals(ImageLocationEnum.LOCAL.name())
                            || !markdownImage.getPath().contains(filterString)
                            || markdownImage.getPath().contains(client.getCloudType().feature)) {

                            iterator.remove();
                        } else {
                            // 通过 url 下载图片
                            try {
                                final URLConnection connection = getUrlConnection(markdownImage);
                                byte[] temp;
                                try (InputStream in = connection.getInputStream()) {
                                    temp = FileUtil.loadBytes(in);
                                }
                                InputStream inputStream = new ByteArrayInputStream(temp);
                                markdownImage.setInputStream(inputStream);
                                // 这里设置为本地图片, 才会在 uploadhandler 中上传
                                markdownImage.setLocation(ImageLocationEnum.LOCAL);
                            } catch (IOException e) {
                                log.info("下载图片出错: {}", e.getMessage());
                                iterator.remove();
                            }
                        }
                    }
                    log.trace("new waitingProcessMap = {}", waitingProcessMap);
                }
            }
        });
        return new ActionManager(data)
            .addHandler(resolveMarkdownFileHandler)
            // 处理 client
            .addHandler(new OptionClientHandler())
            // 图片上传
            .addHandler(new ImageUploadHandler())
            // 标签转换
            .addHandler(new ImageLabelChangeHandler())
            // 写入标签
            .addHandler(new ReplaceToDocument())
            .addHandler(new FinalChainHandler());
    }

    @NotNull
    private static URLConnection getUrlConnection(MarkdownImage markdownImage) throws IOException {
        URL url;
        try {
            url = new URI(markdownImage.getPath()).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IOException("Invalid URL: " + markdownImage.getPath(), e);
        }
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(5000);
        return connection;
    }
}
