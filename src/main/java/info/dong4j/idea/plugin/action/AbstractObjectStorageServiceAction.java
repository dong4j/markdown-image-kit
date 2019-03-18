package info.dong4j.idea.plugin.action;

import com.google.common.collect.Iterables;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;

import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.util.PsiDocumentUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019 -03-14 17:15
 * @email sjdong3 @iflytek.com
 */
@Slf4j
public abstract class AbstractObjectStorageServiceAction extends AnAction {

    private static final String NODE_MODULES_FILE = "node_modules";

    /**
     * 检查 "upload to XXX OSS" 按钮是否可用
     * 1. 相关 test 通过后
     * a. 如果全是目录则可用
     * b. 如果文件是 markdown 才可用
     *
     * @param event the event
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        final Presentation presentation = event.getPresentation();
        final DataContext dataContext = event.getDataContext();

        // 未打开 project 时, 不可用
        final Project project = event.getProject();
        if (project == null) {
            presentation.setEnabled(false);
            return;
        }

        // 如果光标选中了编辑器
        final Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
        if (null != editor) {
            final PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
            presentation.setEnabled(file != null && isValidForFile(file) && isPassedTest());
            return;
        }

        // 没有选中编辑器时, 如果是文件夹
        boolean isValid = false;
        // 如果光标选择了文件树(可多选)
        final VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
        // 如果不是空文件夹
        if (null != files) {
            for (VirtualFile file : files) {
                // 只要有一个是文件夹且不是 node_modules 文件夹 , 则可用
                if (file.isDirectory() && !NODE_MODULES_FILE.equals(file.getName())) {
                    // 文件夹可用
                    isValid = true;
                    break;
                }
                // 只要其中一个是 markdown 文件, 则可用
                if (isMardownFile(file.getName())) {
                    isValid = true;
                    break;
                }
            }
        }
        presentation.setEnabled(isValid && isPassedTest());
    }

    /**
     * 处理事件
     *
     * @param event the an action event
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Map<Document, List<MarkdownImage>> waitingForUploadImages = new HashMap<>(20);

        final Project project = event.getProject();
        if (project != null) {

            log.trace("project's base path = {}", project.getBasePath());
            // 如果选中编辑器
            final DataContext dataContext = event.getDataContext();

            final Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
            // todo-dong4j : (2019年03月15日 09:41) [如果光标选中了编辑器, upload()已经判断过是否为 markdown 文件, 此处不需再判断]
            if (null != editor) {
                // 解析此文件中所有的图片标签
                Document documentFromEditor = editor.getDocument();
                VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(documentFromEditor);
                waitingForUploadImages.put(documentFromEditor, getImageInfoFromFiles(virtualFile));
            }
            // todo-dong4j : (2019年03月15日 09:41) [没有选中编辑器]
            else {
                // 获取被选中的所有文件和目录
                final VirtualFile[] files = PlatformDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
                if (null != files) {
                    for (VirtualFile file : files) {
                        if (isMardownFile(file.getName())) {
                            // 解析此文件中所有的图片标签
                            Document documentFromVirtualFile = FileDocumentManager.getInstance().getDocument(file);
                            waitingForUploadImages.put(documentFromVirtualFile, getImageInfoFromFiles(file));
                        }
                        // 如果是目录, 则递归获取所有 markdown 文件
                        if (file.isDirectory()) {
                            List<VirtualFile> markdownFiles = recursivelyMarkdownFile(file);
                            for (VirtualFile virtualFile : markdownFiles) {
                                Document documentFromVirtualFile = FileDocumentManager.getInstance().getDocument(virtualFile);
                                waitingForUploadImages.put(documentFromVirtualFile, getImageInfoFromFiles(virtualFile));
                            }
                        }
                    }
                }
            }

            // 调用子类的 upload()
            upload(event, waitingForUploadImages);
        }
    }

    /**
     * 通过文件验证是否为 markdown 且是否可写
     *
     * @param file the file
     * @return the boolean
     */
    private boolean isValidForFile(@NotNull PsiFile file) {
        if (!isMardownFile(file)) {
            return false;
        }
        // 不可写时按钮不可用
        return file.isWritable();
    }

    /**
     * 不使用 FileType 判断是因为可能未安装 Markdown support 插件
     *
     * @param name the name
     * @return the boolean
     */
    @Contract(pure = true)
    private boolean isMardownFile(String name) {
        return name.endsWith(MarkdownContents.MARKDOWN_FILE_SUFIX);
    }

    /**
     * Is mardown file boolean.
     *
     * @param psiFile the psi file
     * @return the boolean
     */
    @Contract("null -> false")
    private boolean isMardownFile(PsiFile psiFile) {
        return psiFile != null && isMardownFile(psiFile.getOriginalFile().getName());
    }

    /**
     * 是否通过测试
     *
     * @return the boolean
     */
    abstract boolean isPassedTest();

    /**
     * 显示提示对话框
     *
     * @param title            the title
     * @param subTitle         the sub title
     * @param text             the text
     * @param notificationType the notification type
     */
    void notifucation(String title, String subTitle, String text, NotificationType notificationType) {
        Notification notification = new Notification("Upload to OSS", null, notificationType);
        // 可使用 HTML 标签
        notification.setContent(text);
        notification.setTitle(title);
        notification.setSubtitle(subTitle);
        notification.setImportant(true);
        Notifications.Bus.notify(notification);
    }

    /**
     * 由子类实现具体上传方式
     *
     * @param event                  the event
     * @param waitingForUploadImages the waiting for upload images
     * @return the string   url
     */
    @Contract(pure = true)
    protected void upload(AnActionEvent event, Map<Document, List<MarkdownImage>> waitingForUploadImages) {
        // todo-dong4j : (2019年03月15日 19:06) []
        //  1. 是否设置图片压缩
        //  2. 是否开启图床迁移
        //  3. 是否开启备份
        final Project project = event.getProject();
        if (project != null) {
            if (waitingForUploadImages.size() > 0) {
                int totalProcessed = 0;
                int totalFailured = 0;
                StringBuilder notFoundImages = new StringBuilder();
                for (Map.Entry<Document, List<MarkdownImage>> entry : waitingForUploadImages.entrySet()) {
                    Document document = entry.getKey();
                    for (MarkdownImage markdownImage : entry.getValue()) {
                        if (markdownImage.getLocation().equals(ImageLocationEnum.LOCAL)) {
                            String imageName = markdownImage.getPath();
                            if (StringUtils.isNotBlank(imageName)) {
                                Collection<VirtualFile> findedFiles = FilenameIndex.getVirtualFilesByName(project, imageName, GlobalSearchScope.allScope(project));

                                // 没有对应的图片, 则忽略
                                if (findedFiles.size() <= 0) {
                                    notFoundImages.append("\t").append(imageName).append("\n");
                                    continue;
                                }

                                // 只取第一个图片
                                VirtualFile virtualFile = Iterables.getFirst(findedFiles, null);
                                assert virtualFile != null;
                                String fileType = virtualFile.getFileType().getName();
                                if(ImageContents.IMAGE_TYPE_NAME.equals(fileType)){
                                    File file = new File(virtualFile.getPath());
                                    // 子类执行上传
                                    String uploadedUrl = upload(file);
                                    if (StringUtils.isBlank(uploadedUrl)) {
                                        // todo-dong4j : (2019年03月18日 01:15) [提供失败的文件链接]
                                        totalFailured++;
                                    }
                                    markdownImage.setUploadedUrl(uploadedUrl);
                                }
                            }
                        }
                        // todo-dong4j : (2019年03月15日 20:02) [此处会多次修改, 考虑直接使用 setText() 一次性修改全部文本数据]
                        PsiDocumentUtils.commitAndSaveDocument(project, document, markdownImage);
                        totalProcessed++;
                    }
                }
                notifucation("Upload Completed",
                             "",
                             "Processed File = " + waitingForUploadImages.size() +
                             "\nImage Mark = " + totalProcessed + "\n" +
                             "Failured = " + totalFailured + "\n" +
                             "Some Images Not Found: \n" + notFoundImages.toString(),
                             NotificationType.INFORMATION);

                // ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
                //
                // consoleView.print("Processed File = " + waitingForUploadImages.size() +
                //                   "\nImage Mark = " + totalProcessed + "\n" +
                //                   "Failured = " + totalFailured + "\n" +
                //                   "Some Images Not Found: \n" + notFoundImages.toString(), ConsoleViewContentType.SYSTEM_OUTPUT);
            }
        }
    }

    /**
     * Upload string.
     *
     * @param file the file
     * @return the string
     */
    abstract String upload(File file);

    /**
     * 从 markdown 文件中获取图片信息
     * Document 操作
     *
     * @param virtualFile the virtual file
     * @return the list 避免
     */
    private List<MarkdownImage> getImageInfoFromFiles(VirtualFile virtualFile) {
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
                    if ((markdownImage = matchImageMark(virtualFile, originalLineText, line)) != null) {
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
     * @param virtualFile the virtual file
     * @param lineText    the line text
     * @param line        the line
     * @return the markdown image
     */
    private MarkdownImage matchImageMark(VirtualFile virtualFile, String lineText, int line) {
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
                    markdownImage.setFileName(virtualFile.getName());
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
    private MarkdownImage resolveImageMark(MarkdownImage markdownImage) {
        // 如果以 `<a` 开始, 以 `a>` 结束, 需要修改偏移量
        String lineText = markdownImage.getOriginalLineText();
        if (lineText.startsWith(ImageContents.HTML_TAG_A_START) && lineText.endsWith(ImageContents.HTML_TAG_A_END)) {
            markdownImage.setLineStartOffset(0);
            markdownImage.setLineEndOffset(lineText.length());
            if(lineText.contains(ImageContents.LARG_IMAGE_MARK_ID)){
                markdownImage.setImageMarkType(ImageMarkEnum.LARGE_PICTURE);
            } else if(lineText.contains(ImageContents.COMMON_IMAGE_MARK_ID)){
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
     * 获取 VirtualFile 的几种方式
     *
     * @param e the e
     */
    private void getVirtualFile(AnActionEvent e) {
        // 获取 VirtualFile 方式一:
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        // 递归子目录
        recursivelyMarkdownFile(virtualFile);
        // 获取多个 VirtualFile
        VirtualFile[] virtualFiles = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        // 方式二: 从本地文件系统路径获取
        VirtualFile virtualFileFromLocalFileSystem = LocalFileSystem.getInstance().findFileByIoFile(new File("path"));
        // 方式三: 从 PSI 文件 (如果 PSI 文件仅存在内存中, 则可能返回 null)
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile != null) {
            psiFile.getVirtualFile();
        }
        // 方式四: 从 document 中
        Document document = Objects.requireNonNull(e.getData(PlatformDataKeys.EDITOR)).getDocument();
        VirtualFile virtualFileFromDocument = FileDocumentManager.getInstance().getFile(document);

        // 获取 document
        getDocument(e);
    }

    /**
     * 递归遍历目录, 返回所有 markdown 文件
     *
     * @param virtualFile the virtual file
     */
    private List<VirtualFile> recursivelyMarkdownFile(VirtualFile virtualFile) {
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
                                                   boolean allowAccept = file.isDirectory() && !file.getName().equals(NODE_MODULES_FILE);
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
     * 获取 document 的几种方式
     *
     * @param e the e
     */
    private void getDocument(AnActionEvent e) {
        // 从当前编辑器中获取
        Document documentFromEditor = Objects.requireNonNull(e.getData(PlatformDataKeys.EDITOR)).getDocument();
        // 从 VirtualFile 获取 (如果之前未加载文档内容，则此调用会强制从磁盘加载文档内容)
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile != null) {
            Document documentFromVirtualFile = FileDocumentManager.getInstance().getDocument(virtualFile);
            // 从缓存中获取
            Document documentFromVirtualFileCache = FileDocumentManager.getInstance().getCachedDocument(virtualFile);

            // 从 PSI 中获取
            Project project = e.getProject();
            if (project != null) {
                // 获取 PSI (一)
                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                // 获取 PSI (二)
                psiFile = e.getData(CommonDataKeys.PSI_FILE);
                if (psiFile != null) {
                    Document documentFromPsi = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                    // 从缓存中获取
                    Document documentFromPsiCache = PsiDocumentManager.getInstance(project).getCachedDocument(psiFile);
                }
            }
        }
    }

    /**
     * 获取 PSI 的几种方式
     *
     * @param e the e
     */
    private void getPsiFile(AnActionEvent e) {
        // 从 action 中获取
        PsiFile psiFileFromAction = e.getData(LangDataKeys.PSI_FILE);
        Project project = e.getProject();
        if (project != null) {
            VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
            if (virtualFile != null) {
                // 从 VirtualFile 获取
                PsiFile psiFileFromVirtualFile = PsiManager.getInstance(project).findFile(virtualFile);

                // 从 document
                Document documentFromEditor = Objects.requireNonNull(e.getData(PlatformDataKeys.EDITOR)).getDocument();
                PsiFile psiFileFromDocument = PsiDocumentManager.getInstance(project).getPsiFile(documentFromEditor);

                // 在 project 范围内查找特定 PsiFile
                FilenameIndex.getFilesByName(project, "fileName", GlobalSearchScope.projectScope(project));
            }
        }

        // 找到特定 PSI 元素的使用位置
        // ReferencesSearch.search();

    }
}
