package info.dong4j.idea.plugin.chain;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.action.intention.IntentionActionBase;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.util.ParserUtils;

import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

/**
 * 图片标签替换处理器
 * <p>
 * 用于处理 Markdown 文档中图片标签的替换逻辑，主要负责判断是否需要替换图片标签，并执行相应的替换操作。
 * 该处理器继承自 ActionHandlerAdapter，用于在 Markdown 解析过程中对图片进行自定义处理。
 * </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public class ImageLabelChangeHandler extends ActionHandlerAdapter {
    /** 替换自定义错误提示信息，用于显示错误标题 */
    private static final String MESSAGE = MikBundle.message("mik.action.replace.custom.error.title");

    /**
     * 获取名称
     * <p>
     * 返回与 "mik.action.replace.title" 关键字关联的本地化名称信息
     *
     * @return 名称
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.replace.title");
    }

    /**
     * 判断当前事件数据是否启用
     * <p>
     * 根据传入的事件数据判断是否启用相关功能
     *
     * @param data 事件数据
     * @return 是否启用
     * @since 0.0.1
     */
    @Override
    public boolean isEnabled(EventData data) {
        return IntentionActionBase.getState().isChangeToHtmlTag();
    }

    /**
     * 处理事件数据，根据图片标记类型决定是否进行替换操作
     * <p>
     * 如果图片位置为本地类型，则直接返回不进行处理。若图片标记类型为原始类型，则调用change方法进行替换。
     *
     * @param data          事件数据对象
     * @param imageIterator 图片迭代器，用于遍历图片列表
     * @param markdownImage 当前Markdown图片对象
     */
    @Override
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        // 如果是本地类型, 则不替换
        if (markdownImage.getLocation().equals(ImageLocationEnum.LOCAL)) {
            return;
        }

        // 只替换原始类型的标签, 避免全部替换(使用右键上传时, 根据类型替换为指定标签, 如果已经替换过则不处理)
        ImageMarkEnum currentMarkType = markdownImage.getImageMarkType();
        if (ImageMarkEnum.ORIGINAL.equals(currentMarkType)) {
            change(markdownImage);
        }
    }

    /**
     * 根据 Markdown 图片对象更新其最终标记内容
     * <p>
     * 该方法根据配置的标签类型代码判断使用默认消息还是解析后的结果作为最终标记，并设置到 Markdown 图片对象中
     *
     * @param markdownImage Markdown 图片对象，用于存储更新后的最终标记
     */
    public static void change(MarkdownImage markdownImage) {
        String finalMark;
        // 最后替换与配置不一致的标签
        String typeCode = IntentionActionBase.getState().getTagTypeCode();
        if (MikBundle.message("mik.change.mark.message").equals(typeCode)) {
            finalMark = MESSAGE;
        } else {
            finalMark = ParserUtils.parse2(typeCode,
                                           markdownImage.getTitle(),
                                           markdownImage.getPath());
        }
        markdownImage.setFinalMark(finalMark);
    }
}
