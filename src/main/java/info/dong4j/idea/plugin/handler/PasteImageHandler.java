package info.dong4j.idea.plugin.handler;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorTextInsertHandler;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Producer;
import com.intellij.util.containers.hash.HashMap;

import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.ImageManagerState;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.CharacterUtils;
import info.dong4j.idea.plugin.util.ImageUtils;
import info.dong4j.idea.plugin.util.MarkdownUtils;
import info.dong4j.idea.plugin.watch.ActionManager;
import info.dong4j.idea.plugin.watch.FinalActionHandler;
import info.dong4j.idea.plugin.watch.SaveAndInsertHandler;
import info.dong4j.idea.plugin.watch.UploadAndInsertHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 读取 Clipboard 图片, 上传到 OSS, 最后插入到光标位置</p>
 * todo-dong4j : (2019年03月17日 19:37) [图片压缩处理]
 *
 * @author dong4j
 * @date 2019 -03-16 12:15
 * @email sjdong3 @iflytek.com
 */
@Slf4j
public class PasteImageHandler extends EditorActionHandler implements EditorTextInsertHandler {
    private final EditorActionHandler editorActionHandler;

    /**
     * Instantiates a new Paste image handler.
     *
     * @param originalAction the original action
     */
    public PasteImageHandler(EditorActionHandler originalAction) {
        editorActionHandler = originalAction;
    }

    /**
     * 使用 paste 功能入口
     * 从 clipboard 操作图片文件
     * 1. 如果是 image 类型, 根据设置进行处理
     * 2. 如果是 file 类型, 则获取文件路径然后根据设置处理
     *
     * @param editor      the editor
     * @param caret       the caret
     * @param dataContext the data context
     */
    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        Project project = DataKeys.PROJECT.getData(dataContext);

        Document document = editor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);

        if (virtualFile != null && MarkdownUtils.isMardownFile(virtualFile)) {
            ImageManagerState state = ImageManagerPersistenComponent.getInstance().getState();
            boolean isClipboardControl = state.isClipboardControl();

            if (isClipboardControl) {
                // todo-dong4j : (2019年03月20日 17:32) [使用如下代码获取]
                //  http://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html
                //  "PropertiesComponent.getInstance().setValue("PI__LAST_DIR_PATTERN", dirPattern);"
                if (!OssState.getStatus(state.getCloudType())) {
                    defaultAction(editor, caret, dataContext);
                    // todo-dong4j : (2019年03月20日 14:50) [消息通知]
                    return;
                }

                Map<DataFlavor, Object> clipboardData = ImageUtils.getDataFromClipboard();
                if(clipboardData != null){
                    Iterator<Map.Entry<DataFlavor, Object>> iterator = clipboardData.entrySet().iterator();
                    Map.Entry<DataFlavor, Object> entry = iterator.next();

                    Map<String, Image> imageMap = resolveClipboardData(state, entry);

                    if(imageMap.size() == 0){
                        defaultAction(editor, caret, dataContext);
                        return;
                    }
                    new ActionManager()
                        .addHandler(new SaveAndInsertHandler(editor, imageMap))
                        .addHandler(new UploadAndInsertHandler(editor, imageMap))
                        .addHandler(new FinalActionHandler())
                        .invoke();

                    return;
                }
            }
            defaultAction(editor, caret, dataContext);
        }
    }

    private Map<String, Image> resolveClipboardData(ImageManagerState state, @NotNull Map.Entry<DataFlavor, Object> entry) {
        Map<String, Image> imageMap = new HashMap<>(10);
        if (entry.getKey().equals(DataFlavor.javaFileListFlavor)) {
            // 肯定是 List<File> 类型
            @SuppressWarnings("unchecked") List<File> fileList = (List<File>) entry.getValue();

            for (File file : fileList) {
                // 先检查是否为图片类型
                Image image = null;
                try {
                    File compressedFile = new File(System.getProperty("java.io.tmpdir") + file.getName());
                    // todo-dong4j : (2019年03月20日 04:29) [判断是否启动图片压缩]
                    if (file.isFile() && file.getName().endsWith("jpg")) {
                        ImageUtils.compress(file, compressedFile, state.getCompressBeforeUploadOfPercent() - 20);
                    } else {
                        compressedFile = file;
                    }
                    image = ImageIO.read(compressedFile);
                } catch (IOException ignored) {
                }
                String fileName = file.getName();
                // 只要有一个文件不是 image, 就执行默认操作然后退出
                if (image != null) {
                    imageMap.put(fileName, image);
                }
            }
            // 如果复制的文件个数不全是 image 类型的, 则执行默认操作
            if(imageMap.size() < fileList.size()){
                imageMap.clear();
            }
        } else {
            // image 类型统一重命名, 因为获取不到文件名
            String fileName = CharacterUtils.getRandomString(6) + ".png";
            imageMap.put(fileName, (Image) entry.getValue());
        }
        return imageMap;
    }

    /**
     * 默认 paste 操作, 如果是文件的话应该粘贴文件名
     *
     * @param editor      the editor
     * @param caret       the caret
     * @param dataContext the data context
     */
    private void defaultAction(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        // 执行默认的 paste 操作
        if (editorActionHandler != null) {
            editorActionHandler.execute(editor, caret, dataContext);
        }
    }

    /**
     * 执行上传或拷贝处理
     *
     * @param editor   the editor
     * @param image    the image
     * @param fileName the file name
     */
    private void invoke(@NotNull Editor editor, Image image, String fileName) {
        ImageManagerState state = ImageManagerPersistenComponent.getInstance().getState();
        boolean isCopyToDir = state.isCopyToDir();
        boolean isUploadAndReplace = state.isUploadAndReplace();
        BufferedImage bufferedImage = ImageUtils.toBufferedImage(image);
        if (bufferedImage != null) {
            // 圆角处理
            bufferedImage = ImageUtils.toBufferedImage(ImageUtils.makeRoundedCorner(bufferedImage, 20));
            if (isUploadAndReplace) {
                uploadAndInsert(editor, bufferedImage, fileName);
            }
            if (isCopyToDir) {
                saveAndInsert(editor, bufferedImage, fileName);
            }
        }
    }

    /**
     * 保存图片并在当前光标位置插入替换后的 markdown image mark
     *
     * @param editor        the editor
     * @param bufferedImage the buffered image
     * @param imageName     the image name
     */
    private void saveAndInsert(@NotNull Editor editor,
                               BufferedImage bufferedImage,
                               String imageName) {
        // ImageManagerState state = ImageManagerPersistenComponent.getInstance().getState();
        // Document document = editor.getDocument();
        // // 保存图片
        // VirtualFile currentFile = FileDocumentManager.getInstance().getFile(document);
        // assert currentFile != null;
        // File curDocument = new File(currentFile.getPath());
        // String savepath = state.getImageSavePath();
        // File imageDir = new File(curDocument.getParent(), savepath);
        // boolean checkDir = imageDir.exists() && imageDir.isDirectory();
        // if (checkDir || imageDir.mkdirs()) {
        //     File imageFile = new File(imageDir, imageName);
        //     Runnable r = () -> {
        //         try {
        //             // 如果勾选了上传且替换, 就不再插入本地的图片标签
        //             if (!state.isUploadAndReplace()) {
        //                 File imageFileRelativizePath = curDocument.getParentFile().toPath().relativize(imageFile.toPath()).toFile();
        //                 String relImagePath = imageFileRelativizePath.toString().replace('\\', '/');
        //                 EditorModificationUtil.insertStringAtCaret(editor, "![](" + relImagePath + ")" + ImageContents.LINE_BREAK);
        //             }
        //             BufferedImage compressedImage = Thumbnails.of(bufferedImage)
        //                 .size(bufferedImage.getWidth(), bufferedImage.getHeight())
        //                 .outputQuality(state.getCompressBeforeUploadOfPercent() * 1.0 / 100).asBufferedImage();
        //             ImageIO.write(compressedImage, "png", imageFile);
        //             // 保存到文件后同步刷新缓存, 让图片显示到文件树中
        //             VirtualFileManager.getInstance().syncRefresh();
        //             // todo-dong4j : (2019年03月20日 17:42) [使用 VirtualFile.createChildData() 创建虚拟文件]
        //             //  以解决还未刷新前使用右键上传图片时找不到文件的问题.
        //
        //         } catch (IOException e) {
        //             // todo-dong4j : (2019年03月17日 15:11) [消息通知]
        //             log.trace("", e);
        //         }
        //     };
        //     WriteCommandAction.runWriteCommandAction(editor.getProject(), r);
        // }
    }

    /**
     * 上传图片并在光标位置插入上传后的 markdown image mark
     *
     * @param editor        the editor
     * @param bufferedImage the buffered image
     * @param imageName     the image name
     */
    private void uploadAndInsert(@NotNull Editor editor, BufferedImage bufferedImage, String imageName) {
        // try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
        //     ImageIO.write(bufferedImage, "png", os);
        //     InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
        //     // 上传到默认图床
        //     int index = ImageManagerPersistenComponent.getInstance().getState().getCloudType();
        //     Optional<CloudEnum> cloudType = EnumsUtils.getEnumObject(CloudEnum.class, e -> e.getIndex() == index);
        //     // 此处进行异步处理, 不然上传大图时会卡死
        //     Runnable r = () -> {
        //         OssClient client = ClientUtils.getInstance(cloudType.orElse(CloudEnum.WEIBO_CLOUD));
        //         String imageUrl = new Uploader().setUploadWay(new UploadFromPaste(client, inputStream, imageName)).upload();
        //
        //         if (StringUtils.isNotBlank(imageUrl)) {
        //             String newLineText = UploadUtils.getFinalImageMark("", imageUrl, imageUrl, ImageContents.LINE_BREAK);
        //             EditorModificationUtil.insertStringAtCaret(editor, newLineText);
        //         }
        //     };
        //     WriteCommandAction.runWriteCommandAction(editor.getProject(), r);
        // } catch (IOException e) {
        //     // todo-dong4j : (2019年03月17日 03:20) [添加通知]
        //     log.trace("", e);
        // }
    }

    /**
     * Create virtual file.
     * https://intellij-support.jetbrains.com/hc/en-us/community/posts/206144389-Create-virtual-file-from-file-path
     *
     * @param ed        the ed
     * @param imageFile the image file
     */
    private void createVirtualFile(Editor ed, File imageFile) {
        VirtualFile fileByPath = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(imageFile);
        assert fileByPath != null;
        AbstractVcs usedVcs = ProjectLevelVcsManager.getInstance(Objects.requireNonNull(ed.getProject())).getVcsFor(fileByPath);
        if (usedVcs != null && usedVcs.getCheckinEnvironment() != null) {
            usedVcs.getCheckinEnvironment().scheduleUnversionedFilesForAddition(Collections.singletonList(fileByPath));
        }
    }

    @Override
    public void execute(Editor editor, DataContext dataContext, Producer<Transferable> producer) {

    }
}
