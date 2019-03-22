package info.dong4j.idea.plugin.watch;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.jetbrains.annotations.NotNull;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 拷贝图片到指定目录并替换为对应的 markdown image mark</p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-22 18:47
 */
@Slf4j
public class SaveAndInsertHandler extends PasteActionHandler {
    public SaveAndInsertHandler(@NotNull Editor editor, Map<String, Image> imageMap) {
        super(editor, imageMap);
    }

    @Override
    public boolean isEnabled() {
        return STATE.isCopyToDir() && STATE.isClipboardControl();
    }

    @Override
    public boolean execute() {
        log.info("CopyAndReplaceHandler");

        new Task.Backgroundable(editor.getProject(), MikBundle.message("mik.paste.save.and.insert.progress"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setFraction(0.0);
                indicator.pushState();
                indicator.setIndeterminate(false);

                int totalCount = imageMap.size();
                int totalProcessed = 0;
                for (Map.Entry<String, Image> imageEntry : imageMap.entrySet()) {
                    String imageName = imageEntry.getKey();
                    indicator.setText("Handler " + imageName);
                    BufferedImage bufferedImage = ImageUtils.toBufferedImage(imageEntry.getValue());

                    if (bufferedImage != null) {
                        ImageManagerState state = ImageManagerPersistenComponent.getInstance().getState();
                        Document document = editor.getDocument();
                        // 保存图片
                        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(document);
                        assert currentFile != null;
                        File curDocument = new File(currentFile.getPath());
                        String savepath = state.getImageSavePath();
                        // 保存图片的路径
                        File imageDir = new File(curDocument.getParent(), savepath);
                        boolean checkDir = imageDir.exists() && imageDir.isDirectory();
                        if (checkDir || imageDir.mkdirs()) {
                            File imageFile = new File(imageDir, imageName);

                            Runnable r = () -> {
                                try {
                                    // 如果勾选了上传且替换, 就不再插入本地的图片标签
                                    if (!state.isUploadAndReplace()) {
                                        File imageFileRelativizePath = curDocument.getParentFile().toPath().relativize(imageFile.toPath()).toFile();
                                        String relImagePath = imageFileRelativizePath.toString().replace('\\', '/');
                                        String mark = "![](" + relImagePath + ")" + ImageContents.LINE_BREAK;
                                        indicator.setText2("Replace " + mark);
                                        EditorModificationUtil.insertStringAtCaret(editor, mark);
                                    }
                                    indicator.setText2("Save Image");
                                    ImageIO.write(bufferedImage, ImageUtils.getFileSuffix(imageFile.getName()).replace(".", ""), imageFile);
                                    // 保存到文件后同步刷新缓存, 让图片显示到文件树中
                                    VirtualFileManager.getInstance().syncRefresh();
                                    // todo-dong4j : (2019年03月20日 17:42) [使用 VirtualFile.createChildData() 创建虚拟文件]
                                    //  以解决还未刷新前使用右键上传图片时找不到文件的问题.
                                } catch (IOException e) {
                                    // todo-dong4j : (2019年03月17日 15:11) [消息通知]
                                    log.trace("", e);
                                }
                            };
                            WriteCommandAction.runWriteCommandAction(editor.getProject(), r);
                        }
                    }
                    indicator.setFraction( ++totalProcessed * 1.0 / totalCount);
                }
                indicator.setFraction(1.0);
                indicator.popState();
                indicator.stop();
            }
        }.queue();

        return false;
    }
}
