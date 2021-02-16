/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package info.dong4j.idea.plugin.util;

import com.google.common.collect.Iterables;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public class UploadUtils {
    /** state */
    private static final MikState state = MikPersistenComponent.getInstance().getState();

    /**
     * 根据是否替换标签替换为最终的标签
     * 所有标签保持统一格式
     * [](./imgs/a.png)
     * ![](https://ws2.sinaimg.cn/large/a.jpg)
     * <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a>
     * 未开启标签替换:
     * 1. [](./imgs/a.png) --> ![](https://ws2.sinaimg.cn/large/a.jpg)
     * 2. ![](https://ws2.sinaimg.cn/large/a.jpg) --> ![](https://ws2.sinaimg.cn/large/a.jpg)
     * 3. <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a>
     * -> <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a>
     * 开启标签替换 (按照设置的标签格式替换):
     * 1. [](./imgs/a.png) --> <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a>
     * 2. ![](https://ws2.sinaimg.cn/large/a.jpg) ->
     * <a title='' href='https://ws2.sinaimg.cn/large/a.jpg' >![](https://ws2.sinaimg.cn/large/a.jpg)</a>
     * 3. 与设置的标签一样则不处理
     *
     * @param title     the title
     * @param imageUrl  the image url    上传后的 url, 有可能为 ""
     * @param original  the original     如果为 "", 则使用此字段
     * @param endString the end string
     * @return the final image mark
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
            newLineText = ParserUtils.parse0(state.getTagTypeCode(),
                                             title,
                                             imageUrl,
                                             title,
                                             imageUrl);
        }
        return newLineText + endString;
    }

    /**
     * Search virtual file by name virtual file.
     *
     * @param project the project
     * @param name    the name
     * @return the virtual file
     * @since 0.0.1
     */
    public static VirtualFile searchVirtualFileByName(Project project, String name) {
        // Read access is allowed from event dispatch thread or inside read-action only (see com.intellij.openapi.application.Application.runReadAction())
        AtomicReference<Collection<VirtualFile>> findedFiles = new AtomicReference<>();
        ApplicationManager.getApplication().runReadAction(() -> {
            findedFiles.set(FilenameIndex.getVirtualFilesByName(project, name, GlobalSearchScope.allScope(project)));
        });

        // 只取第一个图片
        return Iterables.getFirst(findedFiles.get(), null);
    }

    /**
     * 通过文件精确查找 VirtualFile
     *
     * @param file the file
     * @return the virtual file
     * @since 0.0.1
     */
    public static VirtualFile searchVirtualFile(File file) {
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
    }
}
