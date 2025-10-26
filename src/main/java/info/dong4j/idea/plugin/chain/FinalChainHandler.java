package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.editor.Document;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * FinalChainHandler 类
 * <p>
 * 该类是 ActionHandlerAdapter 的具体实现，用于处理最终的链式操作。主要功能是根据配置重新设置 imageName，并确保资源正确释放。
 * <p>
 * 在执行过程中，会遍历传入的 EventData 中的等待处理数据，对其中的 MarkdownImage 对象进行处理，关闭其输入流并清空相关数据结构，以释放资源。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
public class FinalChainHandler extends ActionHandlerAdapter {
    /**
     * 获取名称
     * <p>
     * 返回与 "mik.action.final.title" 关键字关联的本地化名称信息
     *
     * @return 名称
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.final.title");
    }

    /**
     * 判断当前状态是否启用
     * <p>
     * 根据传入的事件数据判断当前状态是否启用，实际通过STATE对象的isRename方法返回结果
     *
     * @param data 事件数据
     * @return 是否启用
     * @since 0.0.1
     */
    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isRename();
    }

    /**
     * 根据配置重新设置 imageName
     * <p>
     * 清理事件数据中的 Markdown 图片资源，关闭输入流并清空相关数据结构。
     *
     * @param data 事件数据，包含待处理的图片信息
     * @return 始终返回 true，表示操作成功
     * @since 0.0.1
     */
    @Override
    public boolean execute(EventData data) {
        Map<Document, List<MarkdownImage>> processededData = data.getWaitingProcessMap();
        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : processededData.entrySet()) {
            List<MarkdownImage> markdownImages = imageEntry.getValue();
            for (MarkdownImage markdownImage : markdownImages) {

                if (markdownImage.getInputStream() != null) {
                    try {
                        markdownImage.getInputStream().close();
                    } catch (IOException ignored) {
                    }
                }
            }
            markdownImages.clear();
        }
        processededData.clear();
        return true;
    }
}
