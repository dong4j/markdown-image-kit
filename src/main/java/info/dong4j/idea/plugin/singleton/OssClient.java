package info.dong4j.idea.plugin.singleton;

import info.dong4j.idea.plugin.enums.SuffixEnum;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.util.CharacterUtils;
import info.dong4j.idea.plugin.util.EnumsUtils;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.Optional;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-20 11:52
 */
public interface OssClient {
    String PREFIX = "MIK-upload-";

    /**
     * 统一处理 fileName
     *
     * @param fileName the file name
     * @return the string
     */
    default String processFileName(String fileName) {
        ImageManagerState state = ImageManagerPersistenComponent.getInstance().getState();
        if(state.isRename()){
            int sufixIndex = state.getSuffixIndex();
            Optional<SuffixEnum> sufix = EnumsUtils.getEnumObject(SuffixEnum.class, e -> e.getIndex() == sufixIndex);
            SuffixEnum suffixEnum = sufix.orElse(SuffixEnum.FILE_NAME);
            switch (suffixEnum) {
                case FILE_NAME:
                    return fileName;
                case DATE_FILE_NAME:
                    return DateFormatUtils.format(new Date(), "yyyy-MM-dd-") + fileName;
                case RANDOM:
                    return PREFIX + CharacterUtils.getRandomString(6) + fileName.substring(fileName.lastIndexOf("."));
                default:
                    return fileName;
            }
        }
        return fileName;
    }
}
