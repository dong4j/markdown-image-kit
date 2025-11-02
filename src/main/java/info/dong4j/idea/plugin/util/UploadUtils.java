package info.dong4j.idea.plugin.util;

import com.google.common.collect.Iterables;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.slf4j.Slf4j;

/**
 * 上传工具类
 * <p>
 * 提供与文件上传相关的辅助方法，包括图片标签格式转换、虚拟文件查找等功能。
 * 主要用于处理图片链接的格式转换，支持根据配置决定是否将图片链接替换为 HTML 标签格式。
 * 同时提供查找虚拟文件的方法，用于在项目中定位特定文件。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public class UploadUtils {
    /** 系统状态信息，用于表示当前组件的运行状态 */
    private static final MikState state = MikPersistenComponent.getInstance().getState();

    /**
     * 根据标题、图片URL、原始内容和结束字符串生成最终的图片标记
     * <p>
     * 该方法用于处理图片标记的生成逻辑，根据是否开启标签替换功能，将图片标记转换为统一格式。
     * 如果图片URL为空，则使用原始内容作为替代。根据设置的标签类型，生成对应的图片标记，并追加结束字符串。
     *
     * @param title     标题
     * @param imageUrl  图片URL，上传后的URL，有可能为空
     * @param original  原始内容，如果图片URL为空则使用该字段
     * @param endString 结束字符串
     * @return 最终的图片标记
     * @since 0.0.1
     */
    @NotNull
    public static String getFinalImageMark(String title, String imageUrl, String original, String endString) {
        boolean isChangeToHtmlTag = MikPersistenComponent.getInstance().getState().isChangeToHtmlTag();
        // 处理 imageUrl 为空的情况
        imageUrl = StringUtils.isBlank(imageUrl) ? original : imageUrl;
        // 默认标签格式
        String newLineText = ParserUtils.parse0(ImageContents.DEFAULT_IMAGE_MARK,
                                                title,
                                                imageUrl);
        if (isChangeToHtmlTag) {
            // 根据标签类型枚举获取代码
            ImageMarkEnum tagEnum = state.getImageMarkEnum();
            String tagCode = tagEnum == ImageMarkEnum.CUSTOM
                             ? state.getCustomTagCode()
                             : (tagEnum != null ? tagEnum.getCode() : ImageContents.DEFAULT_IMAGE_MARK);

            newLineText = ParserUtils.parse0(tagCode,
                                             title,
                                             imageUrl,
                                             title,
                                             imageUrl);
        }
        return newLineText + endString;
    }

    /**
     * 根据文件名搜索虚拟文件
     * <p>
     * 在读取操作线程或读取动作内查找指定项目中匹配名称的虚拟文件，并返回第一个找到的文件。
     *
     * @param project 项目对象，用于限定搜索范围
     * @param name    要搜索的文件名
     * @return 匹配的第一个虚拟文件，若未找到则返回 null
     * @since 0.0.1
     */
    public static VirtualFile searchVirtualFileByName(Project project, String name) {
        // Read access is allowed from event dispatch thread or inside read-action only (see com.intellij.openapi.application.Application.runReadAction())
        AtomicReference<Collection<VirtualFile>> findedFiles = new AtomicReference<>();
        ApplicationManager.getApplication().runReadAction(() -> {
            findedFiles.set(FilenameIndex.getVirtualFilesByName(name, GlobalSearchScope.allScope(project)));
        });

        // 只取第一个图片
        return Iterables.getFirst(findedFiles.get(), null);
    }

    /**
     * 通过文件精确查找 VirtualFile
     * <p>
     * 根据传入的 File 对象，在本地文件系统中查找对应的 VirtualFile。
     *
     * @param file 要查找的文件对象
     * @return 对应的 VirtualFile 对象，若未找到则返回 null
     * @since 0.0.1
     */
    public static VirtualFile searchVirtualFile(File file) {
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    }
}
