package info.dong4j.idea.plugin.chain;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.action.intention.IntentionActionBase;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.util.ParserUtils;
import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.NotNull;

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
     * 只有在勾选了"标签替换"开关时才启用
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
     * 判断是否需要处理给定的Markdown图片
     * <p>
     * 该方法用于判断是否需要对指定的Markdown图片进行处理，如果图片的位置类型为本地，则不进行处理。
     *
     * @param markdownImage 要判断的Markdown图片对象
     * @return 如果图片位置不是本地类型，则返回true，表示需要处理；否则返回false
     */
    @Override
    protected boolean shouldProcess(@NotNull MarkdownImage markdownImage) {
        // 如果是本地类型, 则不替换
        return !markdownImage.getLocation().equals(ImageLocationEnum.LOCAL);
    }

    /**
     * 处理事件数据，根据配置的标签类型执行标签转换
     * <p>
     * 该方法会根据配置的目标标签类型（imageMarkEnum）和当前图片的标签类型（imageMarkType）进行比较，
     * 如果两者不同，则执行标签转换，支持所有标签类型之间的相互转换：
     * <ul>
     *   <li>原始 ↔ 正常</li>
     *   <li>原始 ↔ 点击放大</li>
     *   <li>原始 ↔ 自定义</li>
     *   <li>正常 ↔ 点击放大</li>
     *   <li>正常 ↔ 自定义</li>
     *   <li>点击放大 ↔ 自定义</li>
     * </ul>
     *
     * @param data          事件数据对象
     * @param imageIterator 图片迭代器，用于遍历图片列表
     * @param markdownImage 当前Markdown图片对象
     */
    @Override
    public void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage) {
        // 获取配置的目标标签类型
        ImageMarkEnum targetMarkType = IntentionActionBase.getState().getImageMarkEnum();
        // 获取当前图片的标签类型
        ImageMarkEnum currentMarkType = markdownImage.getImageMarkType();

        // 如果目标标签类型与当前标签类型不同，则执行转换
        if (targetMarkType != null && !targetMarkType.equals(currentMarkType)) {
            log.debug("标签转换: {} -> {}, 图片: {}",
                      currentMarkType != null ? currentMarkType.getText() : "null",
                      targetMarkType.getText(),
                      markdownImage.getImageName());
            changeMarkType(markdownImage, targetMarkType);
        } else {
            log.trace("标签类型一致，跳过转换: {}, 图片: {}",
                      currentMarkType != null ? currentMarkType.getText() : "null",
                      markdownImage.getImageName());
        }
    }

    /**
     * 根据目标标签类型更新 Markdown 图片的标记内容
     * <p>
     * 该方法根据目标标签类型生成相应的标记代码，并设置到 Markdown 图片对象中。
     * 支持以下标签类型：
     * <ul>
     *   <li>ORIGINAL - 原始标记：![](...)</li>
     *   <li>COMMON_PICTURE - 正常标记：<a title='...' href='...'>![...](...)&#60;/a></li>
     *   <li>LARGE_PICTURE - 点击放大标记：<a data-fancybox title='...' href='...'>![...](...)&#60;/a></li>
     *   <li>CUSTOM - 自定义标记：使用用户配置的模板</li>
     * </ul>
     *
     * @param markdownImage  Markdown 图片对象，用于存储更新后的最终标记
     * @param targetMarkType 目标标签类型
     */
    private void changeMarkType(@NotNull MarkdownImage markdownImage, @NotNull ImageMarkEnum targetMarkType) {
        String finalMark;
        String typeCode;

        // 根据目标标签类型获取对应的代码模板
        if (targetMarkType == ImageMarkEnum.CUSTOM) {
            // 自定义类型需要从配置中获取自定义代码
            typeCode = IntentionActionBase.getState().getCustomTagCode();
            if (StringUtils.isBlank(typeCode)) {
                log.warn("自定义标签代码为空，跳过转换: {}", markdownImage.getImageName());
                return;
            }
        } else {
            // 使用枚举中定义的代码
            typeCode = targetMarkType.getCode();
        }

        // 检查代码是否为错误提示信息
        if (MikBundle.message("mik.change.mark.message").equals(typeCode)) {
            finalMark = MESSAGE;
        } else {
            // 使用解析器生成最终标记
            finalMark = ParserUtils.parse2(typeCode,
                                           markdownImage.getTitle(),
                                           markdownImage.getPath());
        }

        // 更新图片的标记类型和最终标记
        markdownImage.setImageMarkType(targetMarkType);
        markdownImage.setFinalMark(finalMark);

        log.debug("标签转换完成: {}, 新标记: {}", markdownImage.getImageName(), finalMark);
    }

    /**
     * 根据 Markdown 图片对象更新其最终标记内容（兼容旧版本）
     * <p>
     * 该方法为向后兼容保留，建议使用 {@link #changeMarkType(MarkdownImage, ImageMarkEnum)}
     *
     * @param markdownImage Markdown 图片对象，用于存储更新后的最终标记
     * @deprecated 使用 {@link #changeMarkType(MarkdownImage, ImageMarkEnum)} 替代
     */
    @Deprecated
    public static void change(MarkdownImage markdownImage) {
        ImageMarkEnum targetMarkType = IntentionActionBase.getState().getImageMarkEnum();
        if (targetMarkType == null) {
            log.warn("目标标签类型为空，跳过转换");
            return;
        }
        new ImageLabelChangeHandler().changeMarkType(markdownImage, targetMarkType);
    }


}
