package info.dong4j.idea.plugin.chain.handler;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.io.FileUtil;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.action.intention.IntentionActionBase;
import info.dong4j.idea.plugin.chain.ProgressTracker;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.FileType;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

/**
 * 下载图片处理类
 * <p>
 * 用于处理网络图片的下载操作，将网络图片下载到本地并转换为输入流。
 * 该类继承自 ActionHandlerAdapter，用于在特定事件触发时执行图片下载逻辑。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.11.01
 * @since 1.0.0
 */
@SuppressWarnings("D")
@Slf4j
public class ImageDownloadHandler extends ActionHandlerAdapter {
    /**
     * 获取名称
     * <p>
     * 返回预定义的名称字符串，用于表示操作进度标题
     *
     * @return 名称字符串
     * @since 1.0.0
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.download.title");
    }

    /** 最大线程数 */
    private static final int MAX_THREAD_POOL_SIZE = 15;

    /**
     * 判断当前状态是否启用下载功能
     * <p>
     * 根据当前状态判断是否启用下载功能，只有 location 为 NETWORK 的图片才需要下载
     *
     * @param data 事件数据，用于上下文信息
     * @return 是否启用下载功能
     * @since 1.0.0
     */
    @Override
    public boolean isEnabled(EventData data) {
        return IntentionActionBase.getState().isApplyToNetworkImages();
    }

    /**
     * 判断是否需要处理该Markdown图片
     * <p>
     * 该方法用于判断是否需要对给定的Markdown图片进行处理。只处理网络图片，若为本地图片则检查路径是否有效。
     *
     * @param markdownImage 要判断的Markdown图片对象
     * @return 如果需要处理返回true，否则返回false
     */
    @Override
    protected boolean shouldProcess(@NotNull MarkdownImage markdownImage) {
        // 只处理网络图片
        if (ImageLocationEnum.NETWORK.equals(markdownImage.getLocation())) {
            return true;
        }
        String imageUrl = markdownImage.getPath();
        return imageUrl != null && !imageUrl.isEmpty() &&
               (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"));
    }

    /**
     * 执行多线程下载处理
     * <p>
     * 重写 execute 方法，使用线程池并发下载图片，提高下载效率。
     * 最多使用15个线程进行并发下载。
     *
     * @param data 事件数据对象
     * @return 始终返回 true，表示处理完成
     * @since 2.0.0
     */
    @Override
    public boolean execute(EventData data) {
        // 收集所有需要下载的网络图片
        List<ImageDownloadTask> downloadTasks = new ArrayList<>();

        for (Map.Entry<Document, List<MarkdownImage>> entry : data.getWaitingProcessMap().entrySet()) {
            List<MarkdownImage> images = entry.getValue();
            for (MarkdownImage markdownImage : images) {
                if (shouldProcess(markdownImage)) {
                    downloadTasks.add(new ImageDownloadTask(markdownImage, entry.getKey(), data));
                }
            }
        }

        // 如果没有需要下载的图片，直接返回
        if (downloadTasks.isEmpty()) {
            log.trace("没有需要下载的网络图片");
            return true;
        }

        // 多线程并行下载图片
        int totalCount = downloadTasks.size();
        int threadPoolSize = Math.min(totalCount, MAX_THREAD_POOL_SIZE);
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        log.info("开始下载 {} 张图片，使用 {} 个线程", totalCount, threadPoolSize);

        // 获取进度跟踪器
        ProgressTracker progressTracker = data.getProgressTracker();
        int stepIndex = data.getIndex();
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

                    // 使用 ProgressTracker 更新进度
                    if (progressTracker != null) {
                        String filename = markdownImage.getImageName();
                        if (filename == null || filename.isEmpty()) {
                            filename = markdownImage.getPath();
                        }
                        progressTracker.updateItemProgress(stepIndex, filename, currentProcessed, totalCount);
                    }

                    // 下载图片（调用单个图片的下载逻辑）
                    downloadSingleImage(markdownImage);

                    successCount.incrementAndGet();
                    log.info("下载图片成功: {}", markdownImage.getPath());
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("下载图片失败: {}: {}", task.markdownImage.getPath(), e.getMessage());
                    // 下载失败的图片需要移除
                    synchronized (data.getWaitingProcessMap()) {
                        List<MarkdownImage> imageList = data.getWaitingProcessMap().get(task.document);
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

        return true;
    }

    /**
     * 处理Markdown图片数据，下载网络图片并转换为输入流（单线程版本，已废弃）
     * <p>
     * 该方法用于处理Markdown图片数据，首先检查图片的 location 是否为 NETWORK。
     * 如果是网络图片，则下载图片并转换为输入流，同时将 location 设置为 LOCAL。
     * 如果下载失败，则从迭代器中移除该图片。
     * <p>
     * 注意：此方法已被 execute 方法的多线程实现替代。
     *
     * @param data          事件数据对象
     * @param imageIterator 图片迭代器，用于遍历和移除图片
     * @param markdownImage Markdown图片对象，包含图片路径和输入流
     * @since 1.0.0
     * @deprecated 使用多线程的 execute 方法代替
     */
    @Override
    @Deprecated
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        // 此方法已被多线程版本替代，不再使用
        log.trace("invoke 方法已被多线程 execute 方法替代");
    }

    /**
     * 下载单张图片
     * <p>
     * 该方法用于下载单张网络图片，处理图片的下载、类型识别、重命名等操作。
     * 成功下载后将图片数据设置到 MarkdownImage 对象中，并将 location 标记为 LOCAL。
     *
     * @param markdownImage Markdown图片对象，包含图片路径和输入流
     * @throws IOException 当下载失败或处理失败时抛出
     * @since 2.0.0
     */
    private void downloadSingleImage(MarkdownImage markdownImage) throws IOException {
        String imageUrl = markdownImage.getPath();

        // 下载图片
        URLConnection connection = getUrlConnection(imageUrl);

        // 从 HTTP 响应头获取 Content-Type
        String contentType = connection.getContentType();
        String extension = getExtensionFromContentType(contentType);

        byte[] imageBytes;
        try (InputStream in = connection.getInputStream()) {
            imageBytes = FileUtil.loadBytes(in);
        }

        if (imageBytes == null || imageBytes.length == 0) {
            throw new IOException("下载图片为空");
        }

        // 如果没有从 Content-Type 获取到扩展名，尝试从文件头推断
        if (extension == null || extension.isEmpty()) {
            extension = getExtensionFromFileHeader(imageBytes);
        }

        // 如果仍然无法推断，使用默认扩展名
        if (extension == null || extension.isEmpty()) {
            extension = ".png";
            log.trace("无法推断图片类型，使用默认扩展名: {}", extension);
        }

        // 使用 UUID 生成文件名（小写无横线）
        String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        String extWithoutDot = extension.startsWith(".") ? extension.substring(1) : extension;
        String newImageName = uuid + "." + extWithoutDot;

        markdownImage.setImageName(newImageName);
        markdownImage.setExtension(extWithoutDot);

        // 设置输入流
        markdownImage.setInputStream(new ByteArrayInputStream(imageBytes));
        // 将 location 设置为 LOCAL，以便后续流程可以继续处理
        markdownImage.setLocation(ImageLocationEnum.LOCAL);

        log.debug("下载图片成功: {} -> {} bytes, extension: {}", imageUrl, imageBytes.length, extension);
    }

    /**
     * 根据图片URL获取 URL 连接对象
     * <p>
     * 通过解析图片URL生成 URL 对象，并创建对应的 URL 连接。
     * 设置连接和读取超时时间，用于后续的网络请求操作。
     *
     * @param imageUrl 图片URL地址
     * @return 返回配置好的 URL 连接对象
     * @throws IOException 当路径解析或 URL 创建失败时抛出
     * @since 1.0.0
     */
    @NotNull
    private URLConnection getUrlConnection(@NotNull String imageUrl) throws IOException {
        URL url;
        try {
            url = new URI(imageUrl).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IOException("无效的URL: " + imageUrl, e);
        }
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(5000);
        return connection;
    }

    /**
     * 从 Content-Type 获取文件扩展名
     * <p>
     * 根据 HTTP 响应头的 Content-Type 推断图片文件扩展名。
     * 支持的常见图片类型：image/jpeg, image/png, image/gif, image/webp 等。
     *
     * @param contentType HTTP 响应头的 Content-Type，可能包含 charset 等信息
     * @return 文件扩展名（带点号，如 ".jpg"），如果无法推断则返回 null
     * @since 1.0.0
     */
    @Nullable
    private String getExtensionFromContentType(@Nullable String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return null;
        }

        // Content-Type 可能包含 charset 等信息，例如 "image/jpeg; charset=utf-8"
        String mimeType = contentType.split(";")[0].trim().toLowerCase();

        return switch (mimeType) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/bmp" -> ".bmp";
            case "image/svg+xml" -> ".svg";
            case "image/tiff" -> ".tiff";
            case "image/x-icon", "image/vnd.microsoft.icon" -> ".ico";
            case "image/avif" -> ".avif";
            case "image/heic" -> ".heic";
            case "image/heif" -> ".heif";
            default -> null;
        };
    }

    /**
     * 从文件头（magic bytes）推断文件扩展名
     * <p>
     * 通过读取图片文件的前几个字节（文件头）来判断图片类型，这种方法比依赖 Content-Type 更可靠。
     *
     * @param imageBytes 图片文件的字节数组
     * @return 文件扩展名（带点号，如 ".jpg"），如果无法推断则返回 null
     * @since 1.0.0
     */
    @Nullable
    private String getExtensionFromFileHeader(@NotNull byte[] imageBytes) {
        if (imageBytes.length < 4) {
            return null;
        }

        try (InputStream is = new ByteArrayInputStream(imageBytes)) {
            FileType fileType = ImageUtils.getFileType(is);
            if (fileType != null) {
                String ext = fileType.getExt();
                return ext != null && !ext.isEmpty() ? "." + ext : null;
            }
        } catch (IOException e) {
            log.trace("从文件头推断图片类型失败", e);
        }

        return null;
    }

    /**
     * 图片下载任务
     * <p>
     * 用于封装单个图片的下载任务信息，包括图片对象、所属文档和事件数据。
     *
     * @param markdownImage Markdown图片对象
     * @param document      所属文档
     * @param eventData     事件数据
     * @since 2.0.0
     */
    private record ImageDownloadTask(MarkdownImage markdownImage, Document document, EventData eventData) {
        /**
         * 构造函数
         *
         * @param markdownImage Markdown图片对象
         * @param document      所属文档
         * @param eventData     事件数据
         * @since 2.0.0
         */
        private ImageDownloadTask {
        }
    }

}

