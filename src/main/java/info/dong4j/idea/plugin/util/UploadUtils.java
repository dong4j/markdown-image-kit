package info.dong4j.idea.plugin.util;

import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.settings.OssState;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-17 20:17
 */
public class UploadUtils {

    /**
     * 根据是否替换标签替换为最终的标签
     *
     * @param title    the title
     * @param imageUrl the image url
     * @return the final image mark
     */
    public static String getFinalImageMark(String title, String imageUrl) {
        OssState state = OssPersistenConfig.getInstance().getState();
        boolean isChangeToHtmlTag = OssPersistenConfig.getInstance().getState().isChangeToHtmlTag();
        String newLineText = ParserUtils.parse0(MarkdownContents.DEFAULT_IMAGE_MARK,
                                                title,
                                                imageUrl);
        if (isChangeToHtmlTag) {
            newLineText = ParserUtils.parse0(state.getTagTypeCode(),
                                             title,
                                             imageUrl,
                                             title,
                                             imageUrl);
        }
        return newLineText;
    }
}
