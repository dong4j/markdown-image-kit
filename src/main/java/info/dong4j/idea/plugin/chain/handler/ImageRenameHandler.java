package info.dong4j.idea.plugin.chain.handler;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.action.intention.IntentionActionBase;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.util.PlaceholderParser;

import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

/**
 * 图片文件重命名处理类
 * <p>
 * 用于处理图片文件的重命名逻辑，根据配置信息对图片名称进行格式化和替换操作，支持日期格式、随机字符串等多种重命名策略。
 * 该类继承自 ActionHandlerAdapter，用于在特定事件触发时执行图片重命名操作。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public class ImageRenameHandler extends ActionHandlerAdapter {
    /**
     * 获取名称
     * <p>
     * 返回与 "mik.action.rename.title" 关键字关联的名称信息
     *
     * @return 名称字符串
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.rename.title");
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
        return IntentionActionBase.getState().isRename();
    }

    /**
     * 根据配置重新设置 imageName
     * <p>
     * 该方法使用占位符模板对传入的图片名称进行处理，支持自定义的重命名规则。
     *
     * @param data          事件数据对象
     * @param imageIterator 图片迭代器
     * @param markdownImage Markdown 图片对象，用于存储处理后的图片名称
     * @since 0.0.1
     */
    @Override
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        String imageName = markdownImage.getImageName();
        MikState state = MikPersistenComponent.getInstance().getState();

        // 处理文件名有空格导致上传 gif 变为静态图的问题
        imageName = imageName.replaceAll("\\s*", "");

        try {
            // 使用占位符模板重命名
            String template = state.getRenameTemplate();
            if (template != null && !template.trim().isEmpty() && PlaceholderParser.validateTemplate(template)) {
                // 使用占位符解析器处理文件名
                imageName = PlaceholderParser.parse(template, imageName);
                log.debug("使用模板 [{}] 重命名图片: {}", template, imageName);
            } else {
                // 如果模板为空或无效，保持原文件名不变
                log.debug("重命名模板无效或为空，保持原文件名: {}", imageName);
            }
        } catch (Exception e) {
            // 如果解析失败，保持原文件名
            log.debug("解析重命名模板失败，保持原文件名: {}", e.getMessage(), e);
        }

        markdownImage.setImageName(imageName);
    }

}
