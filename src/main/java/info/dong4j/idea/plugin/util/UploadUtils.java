package info.dong4j.idea.plugin.util;

import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.settings.OssPersistenConfig;
import info.dong4j.idea.plugin.settings.OssState;

import org.apache.commons.lang.StringUtils;

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
     * @param imageUrl the image url    上传后的 url, 有可能为 ""
     * @param original the original     如果为 "", 则使用此字段
     * @return the final image mark
     */
    public static String getFinalImageMark(String title, String imageUrl, String original) {
        OssState state = OssPersistenConfig.getInstance().getState();
        boolean isChangeToHtmlTag = OssPersistenConfig.getInstance().getState().isChangeToHtmlTag();
        imageUrl = StringUtils.isBlank(imageUrl) ?  original : imageUrl;
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
