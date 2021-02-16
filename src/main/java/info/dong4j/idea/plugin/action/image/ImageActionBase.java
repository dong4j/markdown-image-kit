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

package info.dong4j.idea.plugin.action.image;

import com.intellij.mock.MockDocument;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.util.ActionUtils;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: 图片 压缩, 直接上传后将 url 写入到 clipboard </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public abstract class ImageActionBase extends AnAction {
    /** STATE */
    protected static final MikState STATE = MikPersistenComponent.getInstance().getState();

    /**
     * Gets icon.
     *
     * @return the icon
     * @since 0.0.1
     */
    abstract protected Icon getIcon();

    /**
     * Build chain.
     *
     * @param event             the event
     * @param waitingProcessMap the waiting process map
     * @since 0.0.1
     */
    abstract void buildChain(AnActionEvent event, Map<Document, List<MarkdownImage>> waitingProcessMap);

    /**
     * Update
     *
     * @param event event
     * @since 0.0.1
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        ActionUtils.isAvailable(event, this.getIcon(), ImageContents.IMAGE_TYPE_NAME);
    }

    /**
     * Action performed
     *
     * @param event event
     * @since 0.0.1
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Map<String, File> imageMap = new HashMap<>(32);
        Map<String, VirtualFile> virtualFileMap = new HashMap<>(32);

        Map<Document, List<MarkdownImage>> waitingProcessMap = new HashMap<>(32);

        Project project = event.getProject();
        if (project != null) {
            log.trace("project's base path = {}", project.getBasePath());

            // 如果选中编辑器
            DataContext dataContext = event.getDataContext();

            Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
            if (null != editor) {
                VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
                assert virtualFile != null;
                this.buildWaitingProcessMap(waitingProcessMap, virtualFile);
            } else {
                // 获取被选中的有文件和目录
                VirtualFile[] virtualFiles = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
                if (null != virtualFiles) {
                    for (VirtualFile rootFile : virtualFiles) {
                        if (ImageContents.IMAGE_TYPE_NAME.equals(rootFile.getFileType().getName())) {
                            this.buildWaitingProcessMap(waitingProcessMap, rootFile);

                        }
                        // 如果是目录, 则递归获取所有 image 文件
                        if (rootFile.isDirectory()) {
                            List<VirtualFile> imageFiles = ImageUtils.recursivelyImageFile(rootFile);
                            for (VirtualFile subFile : imageFiles) {
                                this.buildWaitingProcessMap(waitingProcessMap, subFile);
                            }
                        }
                    }
                }
            }

            if(waitingProcessMap.size() > 0){
                this.buildChain(event, waitingProcessMap);
            }
        }
    }

    /**
     * 将 VirtualFile 转换为 File
     *
     * @param waitingProcessMap the waiting process map
     * @param virtualFile       the virtual file
     * @since 0.0.1
     */
    private void buildWaitingProcessMap(@NotNull Map<Document, List<MarkdownImage>> waitingProcessMap,
                                        @NotNull VirtualFile virtualFile) {
        MarkdownImage markdownImage = new MarkdownImage();
        markdownImage.setVirtualFile(virtualFile);
        markdownImage.setImageName(virtualFile.getName());
        markdownImage.setPath(virtualFile.getPath());
        try {
            markdownImage.setInputStream(virtualFile.getInputStream());
        } catch (IOException e) {
            return;
        }
        markdownImage.setFileName(virtualFile.getName());
        markdownImage.setExtension(virtualFile.getExtension());
        markdownImage.setLocation(ImageLocationEnum.LOCAL);
        markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);

        waitingProcessMap.put(new MockDocument(), new ArrayList<MarkdownImage>() {
            private static final long serialVersionUID = 5838886826856938689L;

            {
                this.add(markdownImage);
            }
        });
    }
}
