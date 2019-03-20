package info.dong4j.idea.plugin.util;

import com.intellij.openapi.editor.Editor;

import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.singleton.OssClient;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-17 20:17
 */
@Slf4j
public class UploadUtils {
    private static ImageManagerState state = ImageManagerPersistenComponent.getInstance().getState();

    /**
     * 根据是否替换标签替换为最终的标签
     * 所有标签保持统一格式
     * [](./imgs/a.png)
     * ![](https://ws2.sinaimg.cn/large/a.jpg)
     * <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a>
     * 未开启标签替换:
     * 1. [](./imgs/a.png) --> ![](https://ws2.sinaimg.cn/large/a.jpg)
     * 2. ![](https://ws2.sinaimg.cn/large/a.jpg) --> ![](https://ws2.sinaimg.cn/large/a.jpg)
     * 3. <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a> -> <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a>
     * 开启标签替换 (按照设置的标签格式替换):
     * 1. [](./imgs/a.png) --> <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a>
     * 2. ![](https://ws2.sinaimg.cn/large/a.jpg) -> <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a>
     * 3. 与设置的标签一样则不处理
     *
     * @param title     the title
     * @param imageUrl  the image url    上传后的 url, 有可能为 ""
     * @param original  the original     如果为 "", 则使用此字段
     * @param endString the end string
     * @return the final image mark
     */
    public static String getFinalImageMark(String title, String imageUrl, String original, String endString) {
        boolean isChangeToHtmlTag = ImageManagerPersistenComponent.getInstance().getState().isChangeToHtmlTag();
        // 处理 imageUrl 为空的情况
        imageUrl = StringUtils.isBlank(imageUrl) ? original : imageUrl;
        // 默认标签格式
        String newLineText = ParserUtils.parse0(ImageContents.DEFAULT_IMAGE_MARK,
                                                title,
                                                imageUrl);
        if (isChangeToHtmlTag) {
            newLineText = ParserUtils.parse0(state.getTagTypeCode(),
                                             title,
                                             imageUrl,
                                             title,
                                             imageUrl);
        }
        return newLineText + endString;
    }

    /**
     * "Upload Test" 按钮被点击后调用, 每次获取最新的配置, 不使用 state 配置.
     * 调用者 {@link info.dong4j.idea.plugin.settings.ProjectSettingsPage#testAndHelpListener()}
     * 被调用 {@link info.dong4j.idea.plugin.singleton.OssClient#upload(InputStream, String, JPanel)}
     *
     * @param cloudEnum   the cloud enum
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     */
    public static String upload(CloudEnum cloudEnum, InputStream inputStream, String fileName, JPanel jPanel) {
        try {
            Class<?> cls = Class.forName(cloudEnum.getClassName());
            Constructor constructor = cls.getDeclaredConstructor();
            // 有意破坏单例, 避免条件判断
            constructor.setAccessible(true);
            Object obj = constructor.newInstance();
            Method setFunc = cls.getMethod("upload", InputStream.class, String.class, JPanel.class);
            return (String) setFunc.invoke(obj, inputStream, fileName, jPanel);
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            // todo-dong4j : (2019年03月17日 03:20) [添加通知]
            log.trace("", e);
        }
        return "";
    }

    /**
     * Test 通过且保存配置后, 通过此方法进行上传 (使用缓存中的 ossClient).
     * 调用者 {@link info.dong4j.idea.plugin.handler.PasteImageHandler#uploadAndInsert(Editor, BufferedImage, String)}
     * 被调用 {@link info.dong4j.idea.plugin.singleton.OssClient#upload(InputStream, String))}
     *
     * @param cloudEnum   the cloud enum    图床类型
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    public static String upload(@NotNull CloudEnum cloudEnum, InputStream inputStream, String fileName) {
        String className = cloudEnum.getClassName();
        try {
            Class<?> cls = Class.forName(className);
            Object uploader = OssClient.UPLOADER.get(className);

            if (uploader == null) {
                Constructor constructor = cls.getDeclaredConstructor();
                // 有意破坏单例, 避免条件判断
                constructor.setAccessible(true);
                uploader = constructor.newInstance();
                OssClient.UPLOADER.put(className, uploader);
            }
            Method setFunc = cls.getMethod("upload", InputStream.class, String.class);
            return (String) setFunc.invoke(uploader, inputStream, fileName);
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            // todo-dong4j : (2019年03月17日 03:20) [添加通知]
            log.trace("", e);
        }
        return "";
    }
}
