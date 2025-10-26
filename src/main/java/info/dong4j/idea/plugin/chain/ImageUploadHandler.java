package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;

import info.dong4j.idea.plugin.MikBundle;
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
    @Override
    public boolean execute(EventData data) {
        ProgressIndicator indicator = data.getIndicator();
        int size = data.getSize();
        int totalProcessed = 0;
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
            int totalCount = imageEntry.getValue().size();
            Iterator<MarkdownImage> imageIterator = imageEntry.getValue().iterator();
            while (imageIterator.hasNext()) {
                CompletableFuture<?> future = CompletableFuture.supplyAsync(() -> {
                    MarkdownImage markdownImage = imageIterator.next();
                    this.extracted(data,
                                   markdownImage.getImageName(),
                                   indicator,
                                   size,
                                   totalProcessed,
                                   totalCount);

                    this.invoke(data, imageIterator, markdownImage);
                    return null;
                }, executorService).exceptionally(e -> null);

                futures.add(future);
            }
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[] {})).join();

        executorService.shutdown();
        return true;
    }

    /**
     * 处理数据并更新进度指示器
     * <p>
     * 根据传入的数据和参数更新进度指示器的状态，包括设置文本和进度比例
     *
     * @param data           当前处理的数据对象
     * @param markdownName   标记名称，用于显示在进度指示器中
     * @param indicator      进度指示器对象，用于更新进度和文本
     * @param size           单个数据项的大小
     * @param totalProcessed 已处理的总项数
     * @param totalCount     总项数，用于计算进度比例
     * @since 1.6.4
     */
    private void extracted(EventData data,
                           String markdownName,
                           ProgressIndicator indicator,
                           int size,
                           int totalProcessed,
                           int totalCount) {
        indicator.setText2(MikBundle.message("mik.action.processing.title", markdownName));
        indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);
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
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        String imageName = markdownImage.getImageName();
        // 已上传过的不处理, 此时 finalmark 为 null, 替换是忽略
        if (ImageLocationEnum.NETWORK.equals(markdownImage.getLocation())) {
            return;
        }

        if (StringUtils.isBlank(imageName) || markdownImage.getInputStream() == null) {
            log.trace("inputstream 为 null 或者 imageName 为 null, remove markdownImage = {}", markdownImage);
            imageIterator.remove();
            return;
        }

        String imageUrl = null;

        try {
            imageUrl = data.getClient().upload(markdownImage.getInputStream(), markdownImage.getImageName());
        } catch (Exception ignored) {
        }

        String mark;
        // 如果上传失败, 则只修改 ![], 避免丢失原始格式
        if (StringUtils.isBlank(imageUrl)) {
            mark = "![upload error](" + markdownImage.getPath() + ")";
            markdownImage.setLocation(ImageLocationEnum.LOCAL);
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
}
