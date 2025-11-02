package info.dong4j.idea.plugin.chain.handler;

import com.intellij.openapi.editor.Document;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ProgressTracker;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

/**
 * 图片上传处理类
 * <p>
 * 用于处理 Markdown 文档中图片的上传操作，支持多线程并发处理，仅上传本地图片，并更新图片路径和标记信息。
 * 提供图片上传的执行逻辑，包括进度更新、异常处理和图片信息的更新。
 *
 * @author dong4j
 * @version 1.6.4
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public class ImageUploadHandler extends ActionHandlerAdapter {
    /**
     * 获取名称
     * <p>
     * 返回预定义的名称字符串，用于表示上传操作的标题
     *
     * @return 名称字符串
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.upload.title");
    }

    /**
     * 判断事件数据是否启用
     * <p>
     * 根据传入的事件数据判断是否启用，当前逻辑始终返回 true
     *
     * @param data 事件数据
     * @return 是否启用
     * @since 0.0.1
     */
    @Override
    public boolean isEnabled(EventData data) {
        // 如果开启
        return true;
    }

    /**
     * 执行事件处理逻辑，处理等待处理的Markdown图片数据。
     * <p>
     * 该方法使用线程池并发处理每个Markdown图片，调用extracted方法提取图片信息，并通过invoke方法进行后续操作。
     * 处理完成后等待所有任务完成，并关闭线程池。
     *
     * @param data 包含事件数据的对象，包括进度指示器、总大小、待处理图片列表等信息
     * @return 始终返回true，表示执行成功
     */
    @SuppressWarnings("D")
    @Override
    public boolean execute(EventData data) {
        ProgressTracker progressTracker = data.getProgressTracker();
        int stepIndex = data.getIndex();

        // 统计总数，用于进度计算
        int totalCount = data.getWaitingProcessMap().values().stream()
            .mapToInt(List::size)
            .sum();

        if (totalCount == 0) {
            log.info("没有待处理的数据");
            return false;
        }

        // 收集所有需要处理的图片
        List<ImageUploadTask> uploadTasks = new ArrayList<>();
        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
            for (MarkdownImage markdownImage : imageEntry.getValue()) {
                String imageName = markdownImage.getImageName();

                // 已上传过的不处理
                if (ImageLocationEnum.NETWORK.equals(markdownImage.getLocation())) {
                    log.debug("图片 {} 已经上传过，跳过", imageName);
                    continue;
                }

                // 验证图片数据
                if (StringUtils.isBlank(imageName) || markdownImage.getInputStream() == null) {
                    log.warn("图片名称或输入流为空，移除该图片: {}", markdownImage);
                    continue;
                }
                uploadTasks.add(new ImageUploadTask(markdownImage));
            }
        }

        // 如果没有需要处理的图片，直接返回
        if (uploadTasks.isEmpty()) {
            log.info("数据被清洗后没有添加任何可处理任务");
            return false;
        }

        // 重设任务数，使用 final 变量以便在 lambda 中使用
        final int finalTotalCount = uploadTasks.size();

        // 使用原子变量跟踪进度，确保线程安全
        AtomicInteger processedCount = new AtomicInteger(0);
        // 动态计算线程池大小，最多使用15个线程，但要考虑图片数量
        int threadPoolSize = Math.min(finalTotalCount, 15);
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        log.info("开始上传 {} 张图片，使用 {} 个线程", finalTotalCount, threadPoolSize);
        List<CompletableFuture<?>> futures = new ArrayList<>();

        // 为每个图片创建异步任务
        for (ImageUploadTask task : uploadTasks) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    MarkdownImage markdownImage = task.markdownImage;
                    int currentProcessed = processedCount.incrementAndGet();

                    // 使用 ProgressTracker 更新进度
                    if (progressTracker != null) {
                        progressTracker.updateItemProgress(stepIndex, markdownImage.getImageName(), currentProcessed, finalTotalCount);
                    }

                    // 执行上传逻辑
                    uploadImage(data, markdownImage);
                } catch (Exception e) {
                    log.error("上传图片时发生异常: {}", task.markdownImage.getImageName(), e);
                }
            }, executorService);

            futures.add(future);
        }

        // 等待所有任务完成
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[] {})).join();
            log.info("图片上传完成，共处理 {} 张图片", finalTotalCount);
        } finally {
            executorService.shutdown();
        }

        return true;
    }

    /**
         * 内部类，用于封装图片上传任务
         */
        private record ImageUploadTask(MarkdownImage markdownImage) {
    }

    /**
     * 上传图片的核心逻辑
     *
     * @param data          事件数据
     * @param markdownImage 待上传的图片
     */
    private void uploadImage(EventData data, MarkdownImage markdownImage) {
        String imageName = markdownImage.getImageName();

        // 检查输入流是否为空
        if (markdownImage.getInputStream() == null) {
            log.warn("图片 {} 的输入流为空，跳过上传", imageName);
            buildMarkdownImage("![upload error: empty stream](", markdownImage);
            return;
        }

        // 检查文件大小
        try {
            long fileSize;
            // 优先从 VirtualFile 获取文件大小（更准确）
            if (markdownImage.getVirtualFile() != null) {
                fileSize = markdownImage.getVirtualFile().getLength();
            } else {
                // 否则使用 available() 方法估算（可能不准确）
                fileSize = markdownImage.getInputStream().available();
            }

            // 如果文件小于 1KB，跳过上传
            if (fileSize < 1024) {
                log.warn("图片 {} 文件大小 {} 字节小于 1KB，跳过上传", imageName, fileSize);
                buildMarkdownImage("![upload skipped: file too small](", markdownImage);
                return;
            }

            log.debug("图片 {} 文件大小: {} 字节", imageName, fileSize);
        } catch (Exception e) {
            log.warn("无法获取图片 {} 的文件大小，继续上传: {}", imageName, e.getMessage());
        }

        String imageUrl = null;

        try {
            log.debug("开始上传图片: {}", imageName);
            final OssClient client = data.getClient();
            imageUrl = client.upload(markdownImage.getInputStream(), markdownImage.getImageName());
            log.info("图片上传成功: {} {} -> {}", client.getName(), imageName, imageUrl);
        } catch (Exception e) {
            log.error("上传图片失败: {}, 错误信息: {}", imageName, e.getMessage(), e);
        }

        // 更新图片信息
        String mark;
        if (StringUtils.isBlank(imageUrl)) {
            mark = "![upload error](" + markdownImage.getPath() + ")";
            markdownImage.setLocation(ImageLocationEnum.LOCAL);
            log.warn("图片 {} 上传失败，保留为本地路径", imageName);
        } else {
            mark = "![](" + imageUrl + ")";
            markdownImage.setPath(imageUrl);
            markdownImage.setLocation(ImageLocationEnum.NETWORK);
        }

        markdownImage.setOriginalLineText(mark);
        markdownImage.setOriginalMark(mark);
        markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
        markdownImage.setFinalMark(mark);
    }

    /**
     * 将传入的字符串与图片路径拼接，并设置到Markdown图片对象中
     * <p>
     * 该方法用于构建图片的标记字符串，并更新Markdown图片对象的相关属性
     *
     * @param title             传入的字符串
     * @param markdownImage Markdown图片对象，用于存储拼接后的标记信息
     */
    private static void buildMarkdownImage(String title, MarkdownImage markdownImage) {
        String mark = title + markdownImage.getPath() + ")";
        markdownImage.setOriginalLineText(mark);
        markdownImage.setOriginalMark(mark);
        markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
        markdownImage.setFinalMark(mark);
        markdownImage.setLocation(ImageLocationEnum.LOCAL);
    }


    /**
     * 处理 Markdown 图片数据，仅上传 location 为 LOCAL 的图片
     * <p>
     * 该方法会检查图片的 location 属性，如果是 NETWORK 则直接返回。否则，检查图片名称和输入流是否有效，若无效则从 imageIterator 中移除该图片。若有效，则尝试上传图片，根据上传结果设置图片的路径、位置和标记信息。
     *
     * @param data          事件数据对象，包含上传所需客户端信息
     * @param imageIterator Markdown 图片迭代器，用于遍历和移除图片
     * @param markdownImage 当前处理的 Markdown 图片对象
     * @since 0.0.1
     */
    @Override
    @Deprecated
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        // 此方法已被多线程版本替代，不再使用
        log.trace("invoke 方法已被多线程 execute 方法替代");
    }
}
