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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
     * 使用多线程并行下载图片，最多使用15个线程，提高处理速度。
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
            if (waitingProcessMap == null || waitingProcessMap.isEmpty()) {
                return;
            }

            // 收集需要下载的图片和需要移除的图片
            List<ImageDownloadTask> downloadTasks = new ArrayList<>();
            List<MarkdownImage> toRemove = new ArrayList<>();

            for (Map.Entry<Document, List<MarkdownImage>> entry : waitingProcessMap.entrySet()) {
                log.trace("old waitingProcessMap = {}", waitingProcessMap);
                List<MarkdownImage> images = entry.getValue();
                OssClient client = data.getClient();

                // 先分类：需要移除的 vs 需要下载的
                for (MarkdownImage markdownImage : images) {
                    // 排除 LOCAL 和用户输入不匹配的标签
                    if (markdownImage.getLocation().name().equals(ImageLocationEnum.LOCAL.name())
                        || !markdownImage.getPath().contains(filterString)
                        || markdownImage.getPath().contains(client.getCloudType().feature)) {

                        log.trace("排除 LOCAL 和用户输入不匹配的标签: {}", markdownImage.getPath());
                        toRemove.add(markdownImage);
                    } else {
                        // 需要下载的图片
                        downloadTasks.add(new ImageDownloadTask(markdownImage, entry.getKey()));
                    }
                }
            }

            // 移除不需要处理的图片
            for (Map.Entry<Document, List<MarkdownImage>> entry : waitingProcessMap.entrySet()) {
                entry.getValue().removeAll(toRemove);
            }

            // 如果没有需要下载的图片，直接返回
            if (downloadTasks.isEmpty()) {
                log.trace("new waitingProcessMap = {}", waitingProcessMap);
                return;
            }

            // 多线程并行下载图片
            int totalCount = downloadTasks.size();
            int threadPoolSize = Math.min(totalCount, 15);
            ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
            log.info("开始下载 {} 张图片，使用 {} 个线程", totalCount, threadPoolSize);

            // 获取进度指示器（如果已设置）
            ProgressIndicator indicator = data.getIndicator();
            AtomicInteger processedCount = new AtomicInteger(0);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            // 为每个图片创建异步下载任务
            for (ImageDownloadTask task : downloadTasks) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        int currentProcessed = processedCount.incrementAndGet();
                        MarkdownImage markdownImage = task.markdownImage;

                        // 更新进度
                        if (indicator != null) {
                            String fileName = markdownImage.getImageName();
                            indicator.setText2(String.format("下载图片: %s (%d/%d)",
                                                             fileName, currentProcessed, totalCount));
                            indicator.setFraction(currentProcessed * 1.0 / totalCount);
                        }

                        // 下载图片
                        final URLConnection connection = getUrlConnection(markdownImage);
                        byte[] temp;
                        try (InputStream in = connection.getInputStream()) {
                            temp = FileUtil.loadBytes(in);
                        }
                        InputStream inputStream = new ByteArrayInputStream(temp);
                        markdownImage.setInputStream(inputStream);
                        // 这里设置为本地图片, 才会在 uploadhandler 中上传
                        markdownImage.setLocation(ImageLocationEnum.LOCAL);
                        successCount.incrementAndGet();
                        log.debug("下载图片成功: {}", markdownImage.getPath());
                    } catch (IOException e) {
                        failCount.incrementAndGet();
                        log.warn("下载图片出错: {}, 错误信息: {}", task.markdownImage.getPath(), e.getMessage());
                        // 下载失败的图片需要移除
                        synchronized (waitingProcessMap) {
                            List<MarkdownImage> imageList = waitingProcessMap.get(task.document);
                            if (imageList != null) {
                                imageList.remove(task.markdownImage);
                            }
                        }
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                        log.error("下载图片时发生异常: {}", task.markdownImage.getPath(), e);
                        // 下载失败的图片需要移除
                        synchronized (waitingProcessMap) {
                            List<MarkdownImage> imageList = waitingProcessMap.get(task.document);
                            if (imageList != null) {
                                imageList.remove(task.markdownImage);
                            }
                        }
                    }
                }, executorService);

                futures.add(future);
            }

            // 等待所有任务完成
            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                log.info("图片下载完成，共处理 {} 张图片，成功 {} 张，失败 {} 张", totalCount, successCount.get(), failCount.get());
            } finally {
                executorService.shutdown();
            }

            log.trace("new waitingProcessMap = {}", waitingProcessMap);
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

    /**
     * 图片下载任务内部类
     * <p>
     * 用于封装图片下载任务的相关信息，包括需要处理的Markdown图片对象和文档对象。
     * 该类作为下载任务的载体，用于在异步或并发处理中传递必要的数据。
     *
     * @author 未知
     * @version 1.0.0
     * @date 2025.10.24
     * @since 1.0.0
     */
    @SuppressWarnings("ClassCanBeRecord")
    private static class ImageDownloadTask {
        /** Markdown 图片对象，用于表示 Markdown 格式中的图片元素 */
        final MarkdownImage markdownImage;
        /** 当前处理的文档对象 */
        final Document document;

        /**
         * 初始化图片下载任务
         * <p>
         * 根据提供的Markdown图片信息和文档对象，初始化图片下载任务的相关属性
         *
         * @param markdownImage Markdown图片信息对象
         * @param document      文档对象，用于关联图片下载任务
         */
        ImageDownloadTask(MarkdownImage markdownImage, Document document) {
            this.markdownImage = markdownImage;
            this.document = document;
        }
    }

    /**
     * 根据 Markdown 图片信息获取 URL 连接对象
     * <p>
     * 通过解析 Markdown 图片路径生成 URL 对象，并创建对应的 URL 连接。
     * 设置连接和读取超时时间，用于后续的网络请求操作。
     *
     * @param markdownImage 包含图片路径信息的 Markdown 图片对象
     * @return 返回配置好的 URL 连接对象
     * @throws IOException 当路径解析或 URL 创建失败时抛出
     */
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
