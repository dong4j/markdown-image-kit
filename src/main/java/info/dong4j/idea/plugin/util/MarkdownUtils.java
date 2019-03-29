/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j <dong4j@gmail.com>
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
 *
 */

package info.dong4j.idea.plugin.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.content.MikContents;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-22 12:52
 */
@Slf4j
public final class MarkdownUtils {


    /**
     * 通过文件验证是否为 markdown 且是否可写
     *
     * @param file the file
     * @return the boolean
     */
    @Contract("null -> false")
    static boolean isValidForFile(PsiFile file) {
        if (file == null) {
            return false;
        }

        if (!isMardownFile(file)) {
            return false;
        }
        // 不可写时按钮不可用
        return file.isWritable();
    }

    /**
     * Is mardown file boolean.
     *
     * @param file the file
     * @return the boolean
     */
    private static boolean isMardownFile(PsiFile file) {
        return file.getFileType().getName().equals(MarkdownContents.MARKDOWN_TYPE_NAME)
               || file.getName().endsWith(MarkdownContents.MARKDOWN_FILE_SUFIX);
    }

    /**
     * Is mardown file boolean.
     *
     * @param file the file
     * @return the boolean
     */
    public static boolean isMardownFile(VirtualFile file) {
        return file.getFileType().getName().equals(MarkdownContents.MARKDOWN_TYPE_NAME)
               || file.getName().endsWith(MarkdownContents.MARKDOWN_FILE_SUFIX);
    }

    /**
     * 从 markdown 文件中获取图片信息
     *
     * @param virtualFile the virtual file
     * @return the list
     */
    private static List<MarkdownImage> getImageInfoFromFiles(Document document, VirtualFile virtualFile) {
        List<MarkdownImage> markdownImageList = new ArrayList<>();

        if (document != null) {
            int lineCount = document.getLineCount();
            // 解析每一行文本
            for (int line = 0; line < lineCount; line++) {
                // 获取指定行的第一个字符在全文中的偏移量，行号的取值范围为：[0,getLineCount()-1]
                int startOffset = document.getLineStartOffset(line);
                // 获取指定行的最后一个字符在全文中的偏移量，行号的取值范围为：[0,getLineCount()-1]
                int endOffset = document.getLineEndOffset(line);
                TextRange currentLineTextRange = TextRange.create(startOffset, endOffset);
                String originalLineText = document.getText(currentLineTextRange);
                // todo-dong4j : (2019年03月29日 03:42) [如果此行文本中有 markdown image mark 才处理]
                if(StringUtils.isBlank(originalLineText) || !isImageMark(originalLineText)){
                    continue;
                }
                log.trace("originalLineText: {}", originalLineText);
                MarkdownImage markdownImage;
                // todo-dong4j : (2019年03月29日 03:44) [获取 imageName]
                //  根据 imageName 全文搜索 UploadUtils.searchVirtualFileByName
                //  如果找不到则不处理
                if ((markdownImage = matchImageMark(virtualFile, originalLineText, line)) != null) {
                    markdownImageList.add(markdownImage);
                }
            }
        }
        return markdownImageList;
    }

    /**
     * 不使用正则, 因为需要记录偏移量
     * fixme-dong4j : (2019年03月29日 03:38 [virtualFile 错误, 应该是图片的 virtualFile])
     * @param virtualFile the virtual file 当前处理的文件
     * @param lineText    the line text    当前处理的文本行
     * @param line        the line         在文本中的行数
     * @return the markdown image
     */
    @Nullable
    public static MarkdownImage matchImageMark(VirtualFile virtualFile, String lineText, int line) {
        int[] offset = resolveText(lineText);
        if (offset == null) {
            return null;
        }
        MarkdownImage markdownImage = new MarkdownImage();
        markdownImage.setFileName(virtualFile.getName());
        markdownImage.setOriginalLineText(lineText);
        markdownImage.setLineNumber(line);
        markdownImage.setLineStartOffset(offset[0]);
        markdownImage.setLineEndOffset(offset[1]);
        // 解析 markdown 图片标签
        try {
            return resolveImageMark(markdownImage, virtualFile);
        } catch (IOException e) {
            log.trace("", e);
        }
        return null;
    }

    /**
     * 是否为有效的 markdown image 标签
     * ![](xxxx) () 内不能为空
     * ![](yyyy) () 内必须是图片
     * ![](zzz.png) () 内图片必须存在
     * fixme-dong4j : (2019年03月29日 03:38 [])
     * @return the boolean
     */
    @Contract(pure = true)
    public static boolean isImageMark(String mark){
        return false;
    }

    /**
     * 从 mark 中获取图片名称
     * fixme-dong4j : (2019年03月29日 03:38 [])
     * @param mark the mark
     * @return the string
     */
    @NotNull
    @Contract(pure = true)
    public static String getImageNameFromMark(String mark){
        return "";
    }

    /**
     * Resolve text int [ ].
     *
     * @param lineText the line text
     * @return the int [ ]
     */
    public static int[] resolveText(String lineText) {
        int[] offset = new int[2];
        lineText = StringUtils.trim(lineText);
        // 匹配 '![' 字符串
        int indexPrefix = lineText.indexOf(ImageContents.IMAGE_MARK_PREFIX);
        boolean hasImageTagPrefix = indexPrefix > -1;
        if (!hasImageTagPrefix) {
            return null;
        }
        // 匹配 ']('
        int indexMiddle = lineText.indexOf(ImageContents.IMAGE_MARK_MIDDLE, indexPrefix);
        boolean hasImageTagMiddle = indexMiddle > -1;
        if (!hasImageTagMiddle) {
            return null;
        }
        // 匹配 匹配 ')'
        int indexSuffix = lineText.indexOf(ImageContents.IMAGE_MARK_SUFFIX, indexMiddle);
        boolean hasImageTagSuffix = indexSuffix > -1;
        if (!hasImageTagSuffix) {
            return null;
        }
        log.trace("image text: {}", lineText);

        offset[0] = indexPrefix;
        offset[1] = indexSuffix + 1;
        return offset;
    }

    /**
     * 解析图片标签, 拿到 title, 文件路径 或标签类型
     *
     * @param markdownImage the markdown image
     * @return the markdown image
     */
    @Contract("_, _ -> param1")
    private static MarkdownImage resolveImageMark(@NotNull MarkdownImage markdownImage, VirtualFile virtualFile) throws IOException {
        // 如果以 `<a` 开始, 以 `a>` 结束, 需要修改偏移量
        String lineText = markdownImage.getOriginalLineText();
        if (lineText.contains(ImageContents.HTML_TAG_A_START) && lineText.contains(ImageContents.HTML_TAG_A_END)) {
            markdownImage.setLineStartOffset(lineText.indexOf(ImageContents.HTML_TAG_A_START));
            markdownImage.setLineEndOffset(lineText.indexOf(ImageContents.HTML_TAG_A_END) + 2);
            // 解析标签类型
            if (lineText.contains(ImageContents.LARG_IMAGE_MARK_ID)) {
                markdownImage.setImageMarkType(ImageMarkEnum.LARGE_PICTURE);
            } else if (lineText.contains(ImageContents.COMMON_IMAGE_MARK_ID)) {
                markdownImage.setImageMarkType(ImageMarkEnum.COMMON_PICTURE);
            } else {
                markdownImage.setImageMarkType(ImageMarkEnum.CUSTOM);
            }
        } else {
            markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
        }
        // 截取 markdown image 标签
        markdownImage.setOriginalMark(lineText.substring(markdownImage.getLineStartOffset(), markdownImage.getLineEndOffset()));

        String title = lineText.substring(lineText.indexOf(ImageContents.IMAGE_MARK_PREFIX) + ImageContents.IMAGE_MARK_PREFIX.length(),
                                          lineText.indexOf(ImageContents.IMAGE_MARK_MIDDLE)).trim();

        String path = lineText.substring(lineText.indexOf(ImageContents.IMAGE_MARK_MIDDLE) + ImageContents.IMAGE_MARK_MIDDLE.length(),
                                         lineText.indexOf(ImageContents.IMAGE_MARK_SUFFIX)).trim();

        markdownImage.setTitle(title);

        // 设置图片位置类型
        if (path.startsWith(ImageContents.IMAGE_LOCATION)) {
            markdownImage.setLocation(ImageLocationEnum.NETWORK);
            // 图片 url
            markdownImage.setPath(path);
            // 解析图片名
            String imageName = path.substring(path.lastIndexOf("/") + 1);
            markdownImage.setImageName(imageName);
            markdownImage.setExtension(ImageUtils.getFileExtension(imageName));
        } else {
            markdownImage.setLocation(ImageLocationEnum.LOCAL);
            // 图片文件的相对路径
            markdownImage.setPath(path);
            markdownImage.setExtension(virtualFile.getExtension());
            markdownImage.setInputStream(virtualFile.getInputStream());
            markdownImage.setImageName(path.substring(path.lastIndexOf(File.separator) + 1));
        }
        return markdownImage;
    }

    /**
     * 递归遍历目录, 返回所有 markdown 文件
     *
     * @param virtualFile the virtual file
     * @return the list
     */
    private static List<VirtualFile> recursivelyMarkdownFile(VirtualFile virtualFile) {
        List<VirtualFile> markdownFiles = new ArrayList<>();
        /*
         * 递归遍历子文件
         *
         * @param root     the root         父文件
         * @param filter   the filter       过滤器
         * @param iterator the iterator     处理方式
         * @return the boolean
         */
        VfsUtilCore.iterateChildrenRecursively(virtualFile,
                                               file -> {
                                                   // todo-dong4j : (2019年03月15日 13:02) [从 .gitignore 中获取忽略的文件]
                                                   boolean allowAccept = file.isDirectory() && !file.getName().equals(MikContents.NODE_MODULES_FILE);
                                                   if (allowAccept || file.getName().endsWith(MarkdownContents.MARKDOWN_FILE_SUFIX)) {
                                                       log.trace("accept = {}", file.getPath());
                                                       return true;
                                                   }
                                                   return false;
                                               },
                                               fileOrDir -> {
                                                   // todo-dong4j : (2019年03月15日 13:04) [处理 markdown 逻辑实现]
                                                   if (!fileOrDir.isDirectory()) {
                                                       log.trace("processFile = {}", fileOrDir.getName());
                                                       markdownFiles.add(fileOrDir);
                                                   }
                                                   return true;
                                               });
        return markdownFiles;
    }

    /**
     * 获取需要处理的 markdown 信息
     * Document --> 需要处理的文档
     * List<MarkdownImage> --> 文档中解析后的 markdown image 信息
     *
     * @param event   the event
     * @param project the project
     * @return the process markdown info    markdown image 信息
     */
    public static Map<Document, List<MarkdownImage>> getProcessMarkdownInfo(@NotNull AnActionEvent event,
                                                                            @NotNull Project project) {

        Map<Document, List<MarkdownImage>> waitingProcessMap = new HashMap<>(20);

        log.trace("project's base path = {}", project.getBasePath());
        // 如果选中编辑器
        final DataContext dataContext = event.getDataContext();

        final Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
        if (null != editor) {
            // 解析此文件中所有的图片标签
            Document documentFromEditor = editor.getDocument();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(documentFromEditor);
            waitingProcessMap.put(documentFromEditor, MarkdownUtils.getImageInfoFromFiles(documentFromEditor, virtualFile));
        } else {
            // 获取被选中的有文件和目录
            final VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
            if (null != files) {
                for (VirtualFile file : files) {
                    if (MarkdownUtils.isMardownFile(file)) {
                        // 解析此文件中所有的图片标签
                        Document documentFromVirtualFile = FileDocumentManager.getInstance().getDocument(file);
                        waitingProcessMap.put(documentFromVirtualFile, MarkdownUtils.getImageInfoFromFiles(documentFromVirtualFile, file));
                    }
                    // 如果是目录, 则递归获取所有 markdown 文件
                    if (file.isDirectory()) {
                        List<VirtualFile> markdownFiles = MarkdownUtils.recursivelyMarkdownFile(file);
                        for (VirtualFile virtualFile : markdownFiles) {
                            Document documentFromVirtualFile = FileDocumentManager.getInstance().getDocument(virtualFile);
                            waitingProcessMap.put(documentFromVirtualFile, MarkdownUtils.getImageInfoFromFiles(documentFromVirtualFile, virtualFile));
                        }
                    }
                }
            }
        }
        return waitingProcessMap;
    }

}
