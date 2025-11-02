package info.dong4j.idea.plugin.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Markdown 工具类
 * <p>
 * 提供与 Markdown 文件相关的辅助功能，包括验证文件是否为 Markdown、解析 Markdown 中的图片信息、
 * 遍历目录获取 Markdown 文件等操作。该类主要用于处理 Markdown 文本中的图片标记，提取图片路径、
 * 标题、文件名等信息，并支持对图片位置（本地或网络）的判断。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@SuppressWarnings("D")
@Slf4j
public final class MarkdownUtils {
    /**
     * 判断文件是否为 Markdown 文件且是否可写
     * <p>
     * 该方法首先检查传入的文件对象是否为 null，若为 null 则直接返回 false。
     * 然后判断文件是否为 Markdown 类型，若不是则返回 false。
     * 最后判断文件是否可写，返回对应的布尔值。
     *
     * @param file 文件对象
     * @return 如果文件是 Markdown 且可写，返回 true；否则返回 false
     * @since 0.0.1
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
     * 判断给定文件是否为Markdown文件
     * <p>
     * 通过检查文件类型或文件名后缀来判断是否为Markdown文件
     *
     * @param file 要判断的文件对象
     * @return 如果是Markdown文件返回true，否则返回false
     * @since 0.0.1
     */
    private static boolean isMardownFile(PsiFile file) {
        return file.getFileType().getName().equals(MarkdownContents.MARKDOWN_TYPE_NAME)
               || file.getName().endsWith(MarkdownContents.MARKDOWN_FILE_SUFIX);
    }

    /**
     * 判断给定的文件是否为Markdown文件
     * <p>
     * 通过检查文件类型名称或文件名后缀来判断是否为Markdown文件
     *
     * @param file 文件对象
     * @return 如果是Markdown文件返回true，否则返回false
     * @since 0.0.1
     */
    public static boolean isMardownFile(VirtualFile file) {
        return file.getFileType().getName().equals(MarkdownContents.MARKDOWN_TYPE_NAME)
               || file.getName().endsWith(MarkdownContents.MARKDOWN_FILE_SUFIX);
    }

    /**
     * 解析文件中的每一行数据，仅对有效的图片标记进行解析
     * <p>
     * 遍历文档中的每一行文本，检查是否为有效的图片标记。如果是，则解析并添加到结果列表中。
     *
     * @param project     当前项目对象
     * @param document    当前处理的文档对象
     * @param virtualFile 当前处理的虚拟文件对象
     * @return 包含解析出的图片标记对象的列表
     * @since 0.0.1
     */
    private static List<MarkdownImage> getImageInfoFromFiles(Project project, Document document, VirtualFile virtualFile) {
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

                if (illegalImageMark(project, originalLineText)) {
                    continue;
                }
                log.trace("originalLineText: {}", originalLineText);
                MarkdownImage markdownImage;
                if ((markdownImage = analysisImageMark(virtualFile, originalLineText, line)) != null) {
                    markdownImageList.add(markdownImage);
                }
            }
        }
        return markdownImageList;
    }

    /**
     * 分析Markdown图片标记信息
     * <p>
     * 该方法用于解析Markdown文本中的图片标记，提取文件名、行号、偏移量、标题、路径等信息，并根据路径类型设置图片位置为网络或本地。
     * 若文本中包含HTML标签<a>，则调整偏移量以匹配HTML结构。同时根据图片标记类型设置图片类型为大图、普通图或自定义。
     *
     * @param virtualFile 当前处理的文件
     * @param lineText    当前处理的文本行
     * @param line        文本中的行号
     * @return 解析后的Markdown图片对象，若解析失败则返回null
     * @since 0.0.1
     */
    @Nullable
    public static MarkdownImage analysisImageMark(VirtualFile virtualFile, String lineText, int line) {
        int[] offset = resolveText(lineText);
        if (offset == null) {
            return null;
        }
        MarkdownImage markdownImage = new MarkdownImage();
        markdownImage.setFilename(virtualFile.getName());
        markdownImage.setOriginalLineText(lineText);
        markdownImage.setLineNumber(line);
        markdownImage.setLineStartOffset(offset[0]);
        markdownImage.setLineEndOffset(offset[1]);


        // 解析 markdown 图片标签
        try {
            // 如果以 `<a` 开始, 以 `a>` 结束, 需要修改偏移量
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
                String imagename = path.substring(path.lastIndexOf(File.separator) + 1);

                Project project = ProjectUtil.guessProjectForFile(virtualFile);
                VirtualFile imageVirtualFile = UploadUtils.searchVirtualFileByName(project, imagename);

                markdownImage.setExtension(imageVirtualFile.getExtension());
                markdownImage.setInputStream(imageVirtualFile.getInputStream());
                markdownImage.setVirtualFile(imageVirtualFile);
                markdownImage.setImageName(imagename);
            }
            return markdownImage;
        } catch (IOException e) {
            log.trace("markdown imge mark analysis error", e);
        }
        return null;
    }

    /**
     * 判断给定的字符串是否为非法的 markdown 图片标签
     * <p>
     * 该方法会检查字符串是否符合 markdown 图片标签的格式，并验证相关路径和文件是否存在。
     *
     * @param project 项目对象，用于文件查找
     * @param mark    要检查的字符串内容
     * @return 如果是非法的 markdown 图片标签，返回 true；否则返回 false
     * @since 0.0.1
     */
    public static boolean illegalImageMark(Project project, String mark) {
        // 整行数据是否有 markdown 标签
        int[] offset = resolveText(mark);
        if (offset == null) {
            return true;
        }

        // ![]() path 不能为空
        String path = getImagePath(mark);
        if (StringUtils.isBlank(path)) {
            return true;
        }

        // 图片名不能为空
        String imageName = getImageName(mark);
        if (StringUtils.isBlank(imageName)) {
            return true;
        }

        // 如果是 url, 则不在本地查询文件
        if (path.startsWith(ImageContents.IMAGE_LOCATION)) {
            return false;
        }

        // 严格验证图片文件是否存在
        VirtualFile virtualFiles = UploadUtils.searchVirtualFileByName(project, imageName);
        if (virtualFiles == null) {
            return true;
        }

        // 文件不是图片
        return !ImageUtils.isImageFile(virtualFiles);
    }

    /**
     * 从 markdown 标签中提取图片名称
     * <p>
     * 根据传入的 markdown 图片标签，解析并获取图片名称。支持两种图片路径格式：以 {@link ImageContents#IMAGE_LOCATION} 开头的路径和系统文件路径。
     * <p>
     * 示例输入和预期输出：
     * 示例 1：网络图片
     * 输入: ![示例图片](https://example.com/images/logo.png)
     * 输出: logo.png
     * 说明: 识别为网络图片，从 URL 中提取最后的文件名
     * <p>
     * 示例 2：本地图片（正斜杠）
     * 输入: ![本地图片](./images/photo.jpg)
     * 输出: photo.jpg
     * 说明: 识别为本地图片，从路径中提取文件名
     * <p>
     * 示例 3：本地图片（反斜杠）
     * 输入: ![本地图片](.\images\photo.jpg)
     * 输出: photo.jpg
     * 说明: Windows 路径格式，使用反斜杠分隔符
     * <p>
     * 示例 4：带括号的图片标题
     * 输入: ![图片 (带括号)](./images/special1.png)
     * 输出: special1.png
     * 说明: 标题中包含括号，但不影响路径提取
     * <p>
     * 示例 5：空路径
     * 输入: ![图片]()
     * 输出: "" (空字符串)
     * 说明: 路径为空，直接返回空字符串
     * <p>
     * 示例 6：无效格式
     * 输入: 这是普通文本，不是图片标记
     * 输出: "" (空字符串)
     * 说明: 无法提取路径，返回空字符串
     *
     * @param mark markdown 图片标签，必须是有效的 markdown 图片语法格式
     * @return 提取的图片名称，若解析失败或路径为空则返回空字符串
     * @since 0.0.1
     */
    @NotNull
    @Contract(pure = true)
    public static String getImageName(String mark) {
        String path = getImagePath(mark);
        if (StringUtils.isBlank(path)) {
            return "";
        }
        String imageName = "";
        // 设置图片位置类型
        try {
            if (path.startsWith(ImageContents.IMAGE_LOCATION)) {
                imageName = path.substring(path.lastIndexOf("/") + 1);
            } else {
                imageName = path.substring(path.lastIndexOf(File.separator) + 1);
            }
        } catch (Exception e) {
            log.trace("get iamge name from path error. path = {}", path);
        }
        return imageName;
    }

    /**
     * 根据标记字符串获取图片路径
     * <p>
     * 从传入的标记字符串中提取图片路径部分，若标记字符串为空或不合规，则返回空字符串
     * 支持处理路径中包含括号的情况，如：![图片 (带括号)](./images/special1.png)
     *
     * @param mark 标记字符串，用于提取图片路径
     * @return 提取后的图片路径字符串
     * @since 0.0.1
     */
    @NotNull
    private static String getImagePath(String mark) {
        if (StringUtils.isBlank(mark)) {
            return "";
        }
        log.trace("find mark: {}", mark);
        try {
            // 找到最后一个 "](" 的位置
            int start = mark.lastIndexOf(ImageContents.IMAGE_MARK_MIDDLE);
            if (start == -1) {
                return "";
            }
            // 从 "](" 后面开始找第一个 ")" 的位置
            int end = mark.indexOf(ImageContents.IMAGE_MARK_SUFFIX, start + ImageContents.IMAGE_MARK_MIDDLE.length());
            if (end == -1) {
                return "";
            }
            return mark.substring(start + ImageContents.IMAGE_MARK_MIDDLE.length(), end).trim();
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * 解析文本并返回图像标记的起始和结束偏移量
     * <p>
     * 该方法用于识别文本中的图像标记格式，并返回其起始和结束位置的偏移量。
     * 支持的图像标记格式为：'!['...']('...')'，方法会查找该格式中的起始和结束位置。
     *
     * @param lineText 要解析的文本内容
     * @return 图像标记的起始和结束偏移量数组，若未找到有效标记则返回 null
     * @since 0.0.1
     */
    @Nullable
    private static int[] resolveText(String lineText) {
        if (StringUtils.isBlank(lineText)) {
            return null;
        }
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
     * 递归遍历目录，收集所有Markdown文件
     * <p>
     * 该方法通过递归方式遍历指定目录及其子目录，筛选出所有Markdown文件并添加到结果列表中。
     *
     * @param virtualFile 要遍历的虚拟文件对象，代表目录起点
     * @return 包含所有Markdown文件的列表
     * @since 0.0.1
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
                                                   boolean allowAccept =
                                                       file.isDirectory() && !file.getName().equals(MikContents.NODE_MODULES_FILE);
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
     * <p>
     * 根据当前选中的编辑器或文件列表，解析其中的 markdown 文档，并提取其中的图片信息。
     * 支持从单个编辑器文档或多个选中的文件/目录中获取 markdown 图片信息。
     *
     * @param event   事件对象，用于获取当前选中的编辑器或文件信息
     * @param project 项目对象，用于获取项目基础路径和相关资源
     * @return 包含文档与对应 markdown 图片信息的映射
     * @since 0.0.1
     */
    public static Map<Document, List<MarkdownImage>> getProcessMarkdownInfo(@NotNull AnActionEvent event,
                                                                            @NotNull Project project) {

        Map<Document, List<MarkdownImage>> waitingProcessMap = new HashMap<>(20);

        log.trace("project's base path = {}", project.getBasePath());
        // 如果选中编辑器
        DataContext dataContext = event.getDataContext();

        Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
        if (null != editor) {
            // 解析此文件中所有的图片标签
            Document documentFromEditor = editor.getDocument();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(documentFromEditor);
            waitingProcessMap.put(documentFromEditor, MarkdownUtils.getImageInfoFromFiles(project, documentFromEditor, virtualFile));
        } else {
            // 获取被选中的有文件和目录
            VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
            if (null != files) {
                for (VirtualFile file : files) {
                    if (MarkdownUtils.isMardownFile(file)) {
                        // 解析此文件中所有的图片标签
                        ApplicationManager.getApplication().runReadAction(() -> {
                            Document documentFromVirtualFile = FileDocumentManager.getInstance().getDocument(file);
                            waitingProcessMap.put(documentFromVirtualFile, MarkdownUtils.getImageInfoFromFiles(project,
                                                                                                               documentFromVirtualFile,
                                                                                                               file));
                        });

                    }
                    // 如果是目录, 则递归获取所有 markdown 文件
                    if (file.isDirectory()) {
                        List<VirtualFile> markdownFiles = MarkdownUtils.recursivelyMarkdownFile(file);
                        for (VirtualFile virtualFile : markdownFiles) {
                            ApplicationManager.getApplication().runReadAction(() -> {
                                Document documentFromVirtualFile = FileDocumentManager.getInstance().getDocument(virtualFile);
                                waitingProcessMap.put(documentFromVirtualFile, MarkdownUtils.getImageInfoFromFiles(project,
                                                                                                                   documentFromVirtualFile, virtualFile));
                            });
                        }
                    }
                }
            }
        }
        return waitingProcessMap;
    }

}
