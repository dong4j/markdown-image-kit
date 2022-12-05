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
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public final class MarkdownUtils {


    /**
     * 通过文件验证是否为 markdown 且是否可写
     *
     * @param file the file
     * @return the boolean
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
     * Is mardown file boolean.
     *
     * @param file the file
     * @return the boolean
     * @since 0.0.1
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
     * @since 0.0.1
     */
    public static boolean isMardownFile(VirtualFile file) {
        return file.getFileType().getName().equals(MarkdownContents.MARKDOWN_TYPE_NAME)
               || file.getName().endsWith(MarkdownContents.MARKDOWN_FILE_SUFIX);
    }

    /**
     * 解析每一行数据, 是有效的 Image mark 才解析
     *
     * @param project     the project           当前项目
     * @param document    the document          当前文本
     * @param virtualFile the virtual file      当前处理的文件
     * @return the list
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
     * 不使用正则, 因为需要记录偏移量
     *
     * @param virtualFile the virtual file 当前处理的文件
     * @param lineText    the line text    当前处理的文本行
     * @param line        the line         在文本中的行数
     * @return the markdown image
     * @since 0.0.1
     */
    @Nullable
    public static MarkdownImage analysisImageMark(VirtualFile virtualFile, String lineText, int line) {
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
     * 是否为有效的 markdown image 标签
     *
     * @param project the project
     * @param mark    the mark
     * @return the boolean
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
        if(StringUtils.isBlank(path)){
            return true;
        }

        // 图片名不能为空
        String imageName = getImageName(mark);
        if (StringUtils.isBlank(imageName)) {
            return true;
        }

        // 如果是 url, 则不在本地查询文件
        if(path.startsWith(ImageContents.IMAGE_LOCATION)){
            return false;
        }

        // 严格验证图片文件是否存在
        VirtualFile virtualFiles = UploadUtils.searchVirtualFileByName(project, imageName);
        if (virtualFiles == null) {
            return true;
        }

        // 文件不是图片
        return !ImageContents.IMAGE_TYPE_NAME.equals(virtualFiles.getFileType().getName());
    }

    /**
     * 从 mark 中获取图片名称
     *
     * @param mark the mark     必须是正确的 markdown image 标签
     * @return the string
     * @since 0.0.1
     */
    @NotNull
    @Contract(pure = true)
    public static String getImageName(String mark) {
        String path = getImagePath(mark);
        if(StringUtils.isBlank(path)){
            return "";
        }
        String imageName = "";
        // 设置图片位置类型
        try{
            if (path.startsWith(ImageContents.IMAGE_LOCATION)) {
                imageName = path.substring(path.lastIndexOf("/") + 1);
            } else {
                imageName = path.substring(path.lastIndexOf(File.separator) + 1);
            }
        }catch (Exception e){
            log.trace("get iamge name from path error. path = {}", path);
        }
        return imageName;
    }

    /**
     * Get image path string.
     *
     * @param mark the mark
     * @return the string
     * @since 0.0.1
     */
    @NotNull
    private static String getImagePath(String mark){
        if (StringUtils.isBlank(mark)) {
            return "";
        }

        return mark.substring(mark.indexOf(ImageContents.IMAGE_MARK_MIDDLE) + ImageContents.IMAGE_MARK_MIDDLE.length(),
                              mark.indexOf(ImageContents.IMAGE_MARK_SUFFIX)).trim();
    }

    /**
     * Resolve text int [ ].
     *
     * @param lineText the line text
     * @return the int [ ]
     * @since 0.0.1
     */
    @Nullable
    private static int[] resolveText(String lineText) {
        if(StringUtils.isBlank(lineText)){
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
     * 递归遍历目录, 返回所有 markdown 文件
     *
     * @param virtualFile the virtual file
     * @return the list
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
                                waitingProcessMap.put(documentFromVirtualFile, MarkdownUtils.getImageInfoFromFiles(project, documentFromVirtualFile, virtualFile));
                            });
                        }
                    }
                }
            }
        }
        return waitingProcessMap;
    }

}
