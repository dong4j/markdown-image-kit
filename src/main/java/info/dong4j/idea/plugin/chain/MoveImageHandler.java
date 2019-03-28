// /*
//  * MIT License
//  *
//  * Copyright (c) 2019 dong4j
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
// package info.dong4j.idea.plugin.chain;
//
// import com.intellij.openapi.editor.Document;
// import com.intellij.openapi.progress.ProgressIndicator;
//
// import info.dong4j.idea.plugin.client.OssClient;
// import info.dong4j.idea.plugin.entity.EventData;
// import info.dong4j.idea.plugin.entity.MarkdownImage;
// import info.dong4j.idea.plugin.enums.ImageLocationEnum;
// import info.dong4j.idea.plugin.util.ImageUtils;
// import info.dong4j.idea.plugin.util.PsiDocumentUtils;
//
// import org.apache.commons.io.FileUtils;
// import org.apache.commons.lang.StringUtils;
//
// import java.io.*;
// import java.net.*;
// import java.util.List;
// import java.util.Map;
//
// /**
//  * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
//  * <p>Description: 图床迁移</p>
//  *
//  * @author dong4j
//  * @email sjdong3@iflytek.com
//  * @since 2019-03-28 01:00
//  */
// public class MoveImageHandler extends BaseActionHandler {
//     private static final int CONNECTION_TIMEOUT = 5 * 1000;
//     private static final int READ_TIMEOUT = 10 * 1000;
//
//     @Override
//     public String getName() {
//         return "迁移图片";
//     }
//
//     /**
//      * 是否符合该处理类的处理范围
//      *
//      * @param data the data
//      * @return 是否符合 boolean
//      */
//     @Override
//     public boolean isEnabled(EventData data) {
//         return true;
//     }
//
//     /**
//      * 执行具体的处理逻辑
//      *
//      * @param data the data
//      * @return 是否阻止系统的事件传递 boolean
//      */
//     @Override
//     public boolean execute(EventData data) {
//         ProgressIndicator indicator = data.getIndicator();
//         int totalProcessed = 0;
//
//         for (Map.Entry<Document, List<MarkdownImage>> entry : data.getWaitingProcessMap().entrySet()) {
//             Document document = entry.getKey();
//             int totalCount = entry.getValue().size();
//             for (MarkdownImage markdownImage : entry.getValue()) {
//                 indicator.setText2("processing " + markdownImage.getImageName());
//                 indicator.setFraction(++totalProcessed * 1.0 / totalCount);
//                 String url = markdownImage.getPath();
//                 OssClient client = data.getClient();
//                 // 如果 url 就是当前图床, 则不处理
//                 if (url.contains(client.getCloudType().feature)) {
//                     continue;
//                 }
//
//                 File file = ImageUtils.buildTempFile(markdownImage.getImageName());
//                 try {
//                     FileUtils.copyURLToFile(new URL(url), file, CONNECTION_TIMEOUT, READ_TIMEOUT);
//                 } catch (IOException e) {
//                     continue;
//                 }
//                 String uploadedUrl = data.getClient().upload(file);
//                 if (StringUtils.isBlank(uploadedUrl)) {
//                     continue;
//                 }
//                 file.deleteOnExit();
//                 markdownImage.setPath(uploadedUrl);
//                 // 这里设置为 LOCAL, 是为了替换标签
//                 markdownImage.setLocation(ImageLocationEnum.LOCAL);
//                 PsiDocumentUtils.commitAndSaveDocument(data.getProject(), document, markdownImage);
//             }
//         }
//         return false;
//     }
// }
