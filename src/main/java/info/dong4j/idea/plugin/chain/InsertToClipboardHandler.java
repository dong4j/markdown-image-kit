package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.util.ImageUtils;

import java.util.List;
import java.util.Map;

/**
 * 将直接上传的图片 URL 写入剪贴板的处理类
 * <p>
 * 该类用于处理将图片 URL 写入系统剪贴板的操作，通常在用户上传图片后触发。它通过遍历待处理的图片数据，将每张图片的标记信息拼接成字符串，并最终写入剪贴板。
 * <p>
 * 该类继承自 ActionHandlerAdapter，实现了 execute 方法以执行具体操作。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
public class InsertToClipboardHandler extends ActionHandlerAdapter {
    /**
     * 获取名称
     * <p>
     * 返回预定义的名称字符串，用于表示当前操作的标题
     *
     * @return 名称字符串
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.write.clipboard.title");
    }

    /**
     * 执行处理逻辑，处理等待处理的Markdown图片并更新进度指示器
     * <p>
     * 该方法从事件数据中获取需要处理的Markdown图片，逐个处理并更新进度指示器。
     * 处理完成后，将生成的标记内容复制到剪贴板。
     *
     * @param data 事件数据，包含需要处理的Markdown图片和进度指示器等信息
     * @return 始终返回true，表示处理成功
     */
    @Override
    public boolean execute(EventData data) {
        ProgressIndicator indicator = data.getIndicator();
        int size = data.getSize();
        int totalProcessed = 0;
        StringBuilder marks = new StringBuilder();
        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
            int totalCount = imageEntry.getValue().size();
            for (MarkdownImage markdownImage : imageEntry.getValue()) {
                String imageName = markdownImage.getImageName();
                indicator.setText2("Processing " + imageName);

                marks.append(markdownImage.getFinalMark()).append(ImageContents.LINE_BREAK);

                indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);
            }
        }
        ImageUtils.setStringToClipboard(marks.toString());
        return true;
    }
}
