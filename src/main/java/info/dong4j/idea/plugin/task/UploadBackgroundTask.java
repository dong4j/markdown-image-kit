package info.dong4j.idea.plugin.task;

import com.google.common.collect.Iterables;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.util.PsiDocumentUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 上传时显示进度条 </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-21 22:51
 */
public class UploadBackgroundTask extends Task.Backgroundable {
    private Map<Document, List<MarkdownImage>> waitingForUploadImages;
    private Project project;
    private OssClient ossClient;

    /**
     * Instantiates a new Upload background task.
     *
     * @param project                the project
     * @param title                  the title
     * @param canBeCancelled         the can be cancelled
     * @param waitingForUploadImages the waiting for upload images
     * @param ossClient              the oss client
     */
    public UploadBackgroundTask(@Nullable Project project,
                                @Nls(capitalization = Nls.Capitalization.Title)
                                @NotNull String title, boolean canBeCancelled,
                                Map<Document, List<MarkdownImage>> waitingForUploadImages,
                                OssClient ossClient) {
        super(project, title, canBeCancelled);
        this.project = project;
        this.waitingForUploadImages = waitingForUploadImages;
        this.ossClient = ossClient;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        indicator.pushState();
        indicator.setIndeterminate(false);
        uploadTask(indicator);
    }

    private void uploadTask(ProgressIndicator indicator) {
        indicator.setFraction(0.0);
        int totalProcessed = 0;
        int totalFailured = 0;
        StringBuilder notFoundImages = new StringBuilder();
        try {
            for (Map.Entry<Document, List<MarkdownImage>> entry : waitingForUploadImages.entrySet()) {
                int totalCount = entry.getValue().size();
                Document document = entry.getKey();
                for (MarkdownImage markdownImage : entry.getValue()) {
                    indicator.setText2("process file: " + markdownImage.getFileName() + " image path: " + markdownImage.getPath());
                    if (markdownImage.getLocation().equals(ImageLocationEnum.LOCAL)) {
                        String imageName = markdownImage.getPath();
                        if (StringUtils.isNotBlank(imageName)) {
                            // Read access is allowed from event dispatch thread or inside read-action only (see com.intellij.openapi.application.Application.runReadAction())
                            AtomicReference<Collection<VirtualFile>> findedFiles = new AtomicReference<>();
                            ApplicationManager.getApplication().runReadAction(() -> {
                                // todo-dong4j : (2019年03月20日 17:46) [或者通过以下 API 精准查找]
                                //  "VirtualFile fileByPath = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(imageName));"
                                findedFiles.set(FilenameIndex.getVirtualFilesByName(project, imageName, GlobalSearchScope.allScope(project)));
                            });

                            // 没有对应的图片, 则忽略
                            if (findedFiles.get().size() <= 0) {
                                notFoundImages.append("\t").append(imageName).append("\n");
                                continue;
                            }

                            // 只取第一个图片
                            VirtualFile virtualFile = Iterables.getFirst(findedFiles.get(), null);
                            assert virtualFile != null;
                            String fileType = virtualFile.getFileType().getName();
                            if (ImageContents.IMAGE_TYPE_NAME.equals(fileType)) {
                                File file = new File(virtualFile.getPath());
                                // 子类执行上传
                                String uploadedUrl = ossClient.upload(file);
                                if (StringUtils.isBlank(uploadedUrl)) {
                                    // todo-dong4j : (2019年03月18日 01:15) [提供失败的文件链接]
                                    totalFailured++;
                                    indicator.setText2("image path: " + markdownImage.getPath() + " upload failed");
                                }
                                markdownImage.setUploadedUrl(uploadedUrl);
                            }
                        }
                    }
                    // todo-dong4j : (2019年03月15日 20:02) [此处会多次修改, 考虑直接使用 setText() 一次性修改全部文本数据]
                    PsiDocumentUtils.commitAndSaveDocument(project, document, markdownImage);
                    indicator.setFraction( ++totalProcessed * 1.0 / totalCount);
                }
            }
            // ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            // consoleView.print("Processed File = " + waitingForUploadImages.size() +
            //                   "\nImage Mark = " + totalProcessed + "\n" +
            //                   "Failured = " + totalFailured + "\n" +
            //                   "Some Images Not Found: \n" + notFoundImages.toString(), ConsoleViewContentType.SYSTEM_OUTPUT);

            // MessageView messageView = MessageView.SERVICE.getInstance(project);
            // messageView.runWhenInitialized(new Runnable() {
            //     @Override
            //     public void run() {
            //         System.out.println("MessageView inited");
            //     }
            // });
        } catch (Exception ignored) {
            notifucation("Upload Error",
                         "",
                         "Processed File = " + waitingForUploadImages.size() +
                         "\nImage Mark = " + totalProcessed + "\n" +
                         "Failured = " + totalFailured + "\n",
                         NotificationType.INFORMATION);
        } finally {
            // 设置进度
            indicator.setFraction(1.0);
            indicator.popState();
        }
    }

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
}
