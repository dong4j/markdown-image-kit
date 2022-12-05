package info.dong4j.idea.plugin.chain;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.util.ParserUtils;

import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: 替换已上传的图片标签 </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public class ImageLabelChangeHandler extends ActionHandlerAdapter {
    /** MESSAGE */
    private static final String MESSAGE = MikBundle.message("mik.action.replace.custom.error.title");

    /**
     * Gets name *
     *
     * @return the name
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.replace.title");
    }

    /**
     * Is enabled
     *
     * @param data data
     * @return the boolean
     * @since 0.0.1
     */
    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isChangeToHtmlTag();
    }

    /**
     * Invoke
     *
     * @param data          data
     * @param imageIterator image iterator
     * @param markdownImage markdown image
     * @since 0.0.1
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
     * Change.
     *
     * @param markdownImage the markdown image
     * @since 0.0.1
     */
    public static void change(MarkdownImage markdownImage) {
        String finalMark;
        // 最后替换与配置不一致的标签
        String typeCode = STATE.getTagTypeCode();
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
