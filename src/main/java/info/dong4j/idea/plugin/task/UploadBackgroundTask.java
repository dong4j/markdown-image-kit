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

package info.dong4j.idea.plugin.task;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.notify.UploadNotification;
import info.dong4j.idea.plugin.util.PsiDocumentUtils;
import info.dong4j.idea.plugin.util.UploadUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                                @Nls(capitalization = Nls.Capitalization.Title) @NotNull String title,
                                boolean canBeCancelled,
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
        // 当前处理的 markdown 文件和所包含的 image 关系
        Map<VirtualFile, List<String>> fileNotFoundListMap = new HashMap<>(10);
        // 当前处理的 markdown 文件和 上传失败的 image 关系
        Map<VirtualFile, List<String>> uploadFailuredListMap = new HashMap<>(10);
        try {
            Document document;
            VirtualFile virtualFileFromDocument;
            for (Map.Entry<Document, List<MarkdownImage>> entry : waitingForUploadImages.entrySet()) {
                int totalCount = entry.getValue().size();
                document = entry.getKey();
                virtualFileFromDocument = FileDocumentManager.getInstance().getFile(document);
                // 填充未找到的图片文件名
                List<String> notFoundImages = new ArrayList<>();
                // 上传失败的图片文件名
                List<String> uploadFailured = new ArrayList<>();
                for (MarkdownImage markdownImage : entry.getValue()) {
                    indicator.setText2("process file: " + markdownImage.getFileName() + " image path: " + markdownImage.getPath());
                    if (markdownImage.getLocation().equals(ImageLocationEnum.LOCAL)) {
                        String imageName = markdownImage.getImageName();
                        if (StringUtils.isNotBlank(imageName)) {
                            VirtualFile virtualFile = UploadUtils.searchVirtualFileByName(project, imageName);

                            if(virtualFile == null){
                                notFoundImages.add(imageName);
                                continue;
                            }

                            String fileType = virtualFile.getFileType().getName();
                            if (ImageContents.IMAGE_TYPE_NAME.equals(fileType)) {
                                File file = new File(virtualFile.getPath());
                                // 子类执行上传
                                String uploadedUrl = ossClient.upload(file);
                                if (StringUtils.isBlank(uploadedUrl)) {
                                    uploadFailured.add(file.getPath());
                                    indicator.setText2("image path: " + markdownImage.getPath() + " upload failed");
                                }
                                markdownImage.setUploadedUrl(uploadedUrl);
                            }
                        }
                    }
                    // todo-dong4j : (2019年03月15日 20:02) [此处会多次修改, 考虑直接使用 setText() 一次性修改全部文本数据]
                    PsiDocumentUtils.commitAndSaveDocument(project, document, markdownImage);
                    indicator.setFraction(++totalProcessed * 1.0 / totalCount);
                }
                // image 文件未找到的
                if (notFoundImages.size() > 0) {
                    fileNotFoundListMap.put(virtualFileFromDocument, notFoundImages);
                }
                // 上传失败的
                if (uploadFailured.size() > 0) {
                    uploadFailuredListMap.put(virtualFileFromDocument, uploadFailured);
                }
            }

            if (fileNotFoundListMap.size() > 0 || uploadFailuredListMap.size() > 0) {
                UploadNotification.notifyUploadFailure(fileNotFoundListMap, uploadFailuredListMap, project);
            }
        } finally {
            // 设置进度
            indicator.setFraction(1.0);
            indicator.popState();
        }
    }
}
