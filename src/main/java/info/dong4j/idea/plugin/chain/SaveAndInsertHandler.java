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
// package info.dong4j.idea.plugin.chain;
//
// import com.intellij.openapi.command.WriteCommandAction;
// import com.intellij.openapi.editor.Document;
// import com.intellij.openapi.editor.Editor;
// import com.intellij.openapi.editor.EditorModificationUtil;
// import com.intellij.openapi.fileEditor.FileDocumentManager;
// import com.intellij.openapi.progress.ProgressIndicator;
// import com.intellij.openapi.progress.Task;
// import com.intellij.openapi.vfs.VirtualFile;
// import com.intellij.openapi.vfs.VirtualFileManager;
//
// import info.dong4j.idea.plugin.MikBundle;
// import info.dong4j.idea.plugin.content.ImageContents;
// import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
// import info.dong4j.idea.plugin.settings.ImageManagerState;
//
// import org.apache.commons.io.FileUtils;
// import org.jetbrains.annotations.NotNull;
//
// import java.io.*;
// import java.util.Map;
//
// import lombok.extern.slf4j.Slf4j;
//
// /**
//  * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
//  * <p>Description: 拷贝图片到指定目录并替换为对应的 markdown image mark</p>
//  *
//  * @author dong4j
//  * @email sjdong3@iflytek.com
//  * @since 2019-03-22 18:47
//  */
// @Slf4j
// public class SaveAndInsertHandler extends PasteActionHandler {
//     public SaveAndInsertHandler(@NotNull Editor editor, Map<String, File> imageMap) {
//         super(editor, imageMap);
//     }
//
//     @Override
//     public boolean isEnabled() {
//         return STATE.isCopyToDir() && STATE.isClipboardControl();
//     }
//
//     @Override
//     public boolean execute() {
//         log.trace("CopyAndReplaceHandler");
//
//         new Task.Backgroundable(editor.getProject(), MikBundle.message("mik.paste.save.and.insert.progress"), true) {
//             @Override
//             public void run(@NotNull ProgressIndicator indicator) {
//                 indicator.setFraction(0.0);
//                 indicator.pushState();
//                 indicator.setIndeterminate(false);
//
//                 int totalCount = imageMap.size();
//                 int totalProcessed = 0;
//                 for (Map.Entry<String, File> imageEntry : imageMap.entrySet()) {
//                     String imageName = imageEntry.getKey();
//                     indicator.setText("Processing " + imageName);
//
//                     // 1. 生成将要保存到的文件
//                     // 2. 保存文件
//                     // 3. 写 markdown image 标签
//
//                     ImageManagerState state = ImageManagerPersistenComponent.getInstance().getState();
//                     Document document = editor.getDocument();
//                     VirtualFile currentFile = FileDocumentManager.getInstance().getFile(document);
//                     assert currentFile != null;
//                     File curDocument = new File(currentFile.getPath());
//                     String savepath = state.getImageSavePath();
//                     // 被保存图片的路径
//                     File imageDir = new File(curDocument.getParent(), savepath);
//                     boolean checkDir = imageDir.exists() && imageDir.isDirectory();
//                     if (checkDir || imageDir.mkdirs()) {
//                         // 保存的文件路径
//                         File imageFile = new File(imageDir, imageName);
//                         Runnable r = () -> {
//                             try {
//                                 // 如果勾选了上传且替换, 就不再插入本地的图片标签
//                                 if (!state.isUploadAndReplace()) {
//                                     File imageFileRelativizePath = curDocument.getParentFile().toPath().relativize(imageFile.toPath()).toFile();
//                                     String relImagePath = imageFileRelativizePath.toString().replace('\\', '/');
//                                     String mark = "![](" + relImagePath + ")" + ImageContents.LINE_BREAK;
//                                     indicator.setText2("Replace " + mark);
//                                     EditorModificationUtil.insertStringAtCaret(editor, mark);
//                                 }
//                                 indicator.setText2("Save Image");
//                                 // 写入到文件
//                                 FileUtils.copyFile(imageEntry.getValue(), imageFile);
//                                 // 保存到文件后同步刷新缓存, 让图片显示到文件树中
//                                 VirtualFileManager.getInstance().syncRefresh();
//                                 // todo-dong4j : (2019年03月20日 17:42) [使用 VirtualFile.createChildData() 创建虚拟文件]
//                                 //  以解决还未刷新前使用右键上传图片时找不到文件的问题.
//                             } catch (IOException e) {
//                                 // todo-dong4j : (2019年03月17日 15:11) [消息通知]
//                                 log.trace("", e);
//                             }
//                         };
//                         WriteCommandAction.runWriteCommandAction(editor.getProject(), r);
//                     }
//
//                     indicator.setFraction(++totalProcessed * 1.0 / totalCount);
//                 }
//                 indicator.setFraction(1.0);
//                 indicator.popState();
//                 indicator.stop();
//             }
//         }.queue();
//
//         return false;
//     }
// }
