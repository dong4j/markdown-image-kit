// /*
//  * MIT License
//  *
//  * Copyright (c) 2019 dong4j <dong4j@gmail.com>
//  *
//  * Permission is hereby granted, free of charge, to any person obtaining a copy
//  * of this software and associated documentation files (the "Software"), to deal
//  * in the Software without restriction, including without limitation the rights
//  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  * copies of the Software, and to permit persons to whom the Software is
//  * furnished to do so, subject to the following conditions:
//  *
//  * The above copyright notice and this permission notice shall be included in all
//  * copies or substantial portions of the Software.
//  *
//  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//  * SOFTWARE.
//  *
//  */
//
// package info.dong4j.idea.plugin.task;
//
// import com.intellij.openapi.editor.Document;
// import com.intellij.openapi.progress.ProgressIndicator;
// import com.intellij.openapi.progress.Task;
// import com.intellij.openapi.project.Project;
//
// import info.dong4j.idea.plugin.client.OssClient;
// import info.dong4j.idea.plugin.entity.MarkdownImage;
// import info.dong4j.idea.plugin.enums.ImageLocationEnum;
// import info.dong4j.idea.plugin.notify.UploadNotification;
// import info.dong4j.idea.plugin.util.ClientUtils;
// import info.dong4j.idea.plugin.util.ImageUtils;
// import info.dong4j.idea.plugin.util.PsiDocumentUtils;
//
// import org.apache.commons.io.FileUtils;
// import org.apache.commons.lang.StringUtils;
// import org.jetbrains.annotations.Nls;
// import org.jetbrains.annotations.NotNull;
// import org.jetbrains.annotations.Nullable;
//
// import java.io.*;
// import java.net.*;
// import java.util.List;
// import java.util.Map;
//
// import lombok.extern.slf4j.Slf4j;
//
// /**
//  * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
//  * <p>Description: </p>
//  *
//  * @author dong4j
//  * @email sjdong3@iflytek.com
//  * @since 2019-03-27 19:13
//  */
// @Slf4j
// public class IntentionBackgroupTask extends Task.Backgroundable {
//     private Project project;
//     private Map<Document, List<MarkdownImage>> waitingForMoveMap;
//     private OssClient client;
//
//     public IntentionBackgroupTask(@Nullable Project project,
//                                   @Nls(capitalization = Nls.Capitalization.Title) @NotNull String title,
//                                   Map<Document, List<MarkdownImage>> waitingForMoveMap,
//                                   OssClient client) {
//         super(project, title);
//         this.project = project;
//         this.client = client;
//         this.waitingForMoveMap = waitingForMoveMap;
//     }
//
//     @Override
//     public void run(@NotNull ProgressIndicator indicator) {
//         indicator.pushState();
//         indicator.setIndeterminate(false);
//         try {
//             indicator.setFraction(0.0);
//             if (ClientUtils.isNotEnable(client)) {
//                 UploadNotification.notifyConfigurableError(project, client == null ? "" : client.getName());
//                 return;
//             }
//             int totalProcessed = 0;
//             for (Map.Entry<Document, List<MarkdownImage>> entry : waitingForMoveMap.entrySet()) {
//                 Document document = entry.getKey();
//                 int totalCount = entry.getValue().size();
//                 for (MarkdownImage markdownImage : entry.getValue()) {
//                     indicator.setText2("processing " + markdownImage.getImageName());
//                     indicator.setFraction(++totalProcessed * 1.0 / totalCount);
//                     String url = markdownImage.getPath();
//
//                     File file = ImageUtils.buildTempFile(markdownImage.getImageName());
//                     try {
//                         FileUtils.copyURLToFile(new URL(url), file);
//                     } catch (IOException e) {
//                         continue;
//                     }
//                     String uploadedUrl = client.upload(file);
//                     if (StringUtils.isBlank(uploadedUrl)) {
//                         continue;
//                     }
//                     file.deleteOnExit();
//                     markdownImage.setUploadedUrl(uploadedUrl);
//                     // 这里设置为 LOCAL, 是为了替换标签
//                     markdownImage.setLocation(ImageLocationEnum.LOCAL);
//                     PsiDocumentUtils.commitAndSaveDocument(project, document, markdownImage);
//                 }
//             }
//         } finally {
//             indicator.setFraction(1.0);
//             indicator.popState();
//         }
//     }
// }
