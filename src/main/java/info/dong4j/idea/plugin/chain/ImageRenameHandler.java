package info.dong4j.idea.plugin.chain;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.action.intention.IntentionActionBase;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.SuffixEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.util.CharacterUtils;
import info.dong4j.idea.plugin.util.EnumsUtils;
import info.dong4j.idea.plugin.util.ImageUtils;
import info.dong4j.idea.plugin.util.date.DateFormatUtils;

import java.util.Date;
import java.util.Iterator;
import java.util.Optional;

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
    /** 用于标识消息前缀的常量，固定值为 "MIK-" */
    private static final String PREFIX = "MIK-";

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
     * 该方法根据当前配置的后缀类型对传入的图片名称进行处理，包括去除空格、添加时间前缀或随机字符串等操作。
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
        int sufixIndex = state.getSuffixIndex();
        Optional<SuffixEnum> sufix = EnumsUtils.getEnumObject(SuffixEnum.class, e -> e.getIndex() == sufixIndex);
        SuffixEnum suffixEnum = sufix.orElse(SuffixEnum.FILE_NAME);
        switch (suffixEnum) {
            case DATE_FILE_NAME -> {
                // 删除原来的时间前缀
                String oldDateTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd-");
                imageName = imageName.replace(oldDateTime, "");
                imageName = oldDateTime + imageName;
            }
            case RANDOM -> {
                if (!imageName.startsWith(PREFIX)) {
                    imageName = PREFIX + CharacterUtils.getRandomString(6) + ImageUtils.getFileExtension(imageName);
                }
            }
            default -> {
            }
        }

        markdownImage.setImageName(imageName);
    }
}
