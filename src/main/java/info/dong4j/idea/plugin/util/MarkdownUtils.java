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

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
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
import java.util.List;

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
    public static boolean isValidForFile(PsiFile file) {
        if(file == null){
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
    public static List<MarkdownImage> getImageInfoFromFiles(VirtualFile virtualFile) {
        List<MarkdownImage> markdownImageList = new ArrayList<>();
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (document != null) {
            int lineCount = document.getLineCount();
            for (int line = 0; line < lineCount; line++) {
                // 获取指定行的第一个字符在全文中的偏移量，行号的取值范围为：[0,getLineCount()-1]
                int startOffset = document.getLineStartOffset(line);
                // 获取指定行的最后一个字符在全文中的偏移量，行号的取值范围为：[0,getLineCount()-1]
                int endOffset = document.getLineEndOffset(line);
                TextRange currentLineTextRange = TextRange.create(startOffset, endOffset);
                // 保存每一行字符串
                String originalLineText = document.getText(currentLineTextRange);
                if (StringUtils.isNotBlank(originalLineText)) {
                    log.trace("originalLineText: {}", originalLineText);
                    MarkdownImage markdownImage;
                    if ((markdownImage = matchImageMark(virtualFile.getName(), originalLineText, line)) != null) {
                        markdownImageList.add(markdownImage);
                    }
                }
            }
        }
        return markdownImageList;
    }

    /**
     * 不使用正则, 因为需要记录偏移量
     *
     * @param fileName the file name
     * @param lineText the line text
     * @param line     the line
     * @return the markdown image
     */
    @Nullable
    public static MarkdownImage matchImageMark(String fileName, String lineText, int line) {
        lineText = StringUtils.trim(lineText);
        // 匹配 '![' 字符串
        int indexPrefix = lineText.indexOf(ImageContents.IMAGE_MARK_PREFIX);
        boolean hasImageTagPrefix = indexPrefix > -1;
        if (hasImageTagPrefix) {
            // 匹配 ']('
            int indexMiddle = lineText.indexOf(ImageContents.IMAGE_MARK_MIDDLE, indexPrefix);
            boolean hasImageTagMiddle = indexMiddle > -1;
            if (hasImageTagMiddle) {
                // 匹配 匹配 ')'
                int indexSuffix = lineText.indexOf(ImageContents.IMAGE_MARK_SUFFIX, indexMiddle);
                boolean hasImageTagSuffix = indexSuffix > -1;
                if (hasImageTagSuffix) {
                    log.trace("image text: {}", lineText);
                    MarkdownImage markdownImage = new MarkdownImage();
                    markdownImage.setFileName(fileName);
                    markdownImage.setOriginalLineText(lineText);
                    markdownImage.setLineNumber(line);
                    markdownImage.setLineStartOffset(indexPrefix);
                    markdownImage.setLineEndOffset(indexSuffix);
                    // 解析 markdown 图片标签
                    return resolveImageMark(markdownImage);
                }
            }
        }
        return null;
    }

    /**
     * 解析图片标签, 拿到 title, 文件路径 或标签类型
     *
     * @param markdownImage the markdown image
     * @return the markdown image
     */
    @Contract("_ -> param1")
    private static MarkdownImage resolveImageMark(@NotNull MarkdownImage markdownImage) {
        // 如果以 `<a` 开始, 以 `a>` 结束, 需要修改偏移量
        String lineText = markdownImage.getOriginalLineText();
        if (lineText.startsWith(ImageContents.HTML_TAG_A_START) && lineText.endsWith(ImageContents.HTML_TAG_A_END)) {
            markdownImage.setLineStartOffset(0);
            markdownImage.setLineEndOffset(lineText.length());
            if (lineText.contains(ImageContents.LARG_IMAGE_MARK_ID)) {
                markdownImage.setImageMarkType(ImageMarkEnum.LARGE_PICTURE);
            } else if (lineText.contains(ImageContents.COMMON_IMAGE_MARK_ID)) {
                markdownImage.setImageMarkType(ImageMarkEnum.COMMON_PICTURE);
            } else {
                markdownImage.setImageMarkType(ImageMarkEnum.CUSTOM);
            }
        }

        String title = lineText.substring(lineText.indexOf(ImageContents.IMAGE_MARK_PREFIX) + ImageContents.IMAGE_MARK_PREFIX.length(),
                                          lineText.indexOf(ImageContents.IMAGE_MARK_MIDDLE));
        String path = lineText.substring(lineText.indexOf(ImageContents.IMAGE_MARK_MIDDLE) + ImageContents.IMAGE_MARK_MIDDLE.length(),
                                         lineText.indexOf(ImageContents.IMAGE_MARK_SUFFIX));
        markdownImage.setTitle(title);
        if (path.startsWith(ImageContents.IMAGE_LOCATION)) {
            markdownImage.setLocation(ImageLocationEnum.NETWORK);
            markdownImage.setPath(path);
        } else {
            // 本地文件只需要文件名
            String imageName = path.substring(path.lastIndexOf(File.separator) + 1);
            markdownImage.setPath(imageName);
        }
        return markdownImage;
    }

    /**
     * 递归遍历目录, 返回所有 markdown 文件
     *
     * @param virtualFile the virtual file
     * @return the list
     */
    public static List<VirtualFile> recursivelyMarkdownFile(VirtualFile virtualFile) {
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
}
