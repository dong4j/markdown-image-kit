package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 图片存储处理类
 * <p>
 * 用于处理文档中图片的保存操作，支持从输入流中复制图片到指定路径，并更新相关图片信息。
 * 该类继承自 ActionHandlerAdapter，主要用于在文档编辑过程中执行图片保存逻辑。
 * <p>
 * 主要功能包括：获取操作名称、判断是否启用、执行保存操作。
 * 在执行过程中，会遍历等待处理的图片集合，逐个复制图片并设置保存路径、图片标记类型、位置等信息。
 * 若保存失败，则设置错误标记并保留原始信息。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.0.0
 */
@Slf4j
public class ImageStorageHandler extends ActionHandlerAdapter {
    /**
     * 获取名称
     * <p>
     * 返回预定义的名称字符串，用于表示存储操作的标题
     *
     * @return 名称字符串
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.store.title");
    }

    /**
     * 判断是否启用功能
     * <p>
     * 根据传入的事件数据判断当前功能是否启用，实际通过检查STATE对象的copyToDir属性值来确定。
     *
     * @param data 事件数据
     * @return 是否启用功能
     * @since 0.0.1
     */
    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isCopyToDir();
    }

    /**
     * 执行处理逻辑，将 Markdown 图片保存为本地文件并更新相关属性
     * <p>
     * 该方法从传入的 EventData 中获取待处理的图片信息，依次处理每张图片，将其从输入流中保存为本地文件。
     * 处理过程中会更新图片的路径、标记类型、位置等信息，并在出现异常时设置相应的错误标记。
     *
     * @param data 包含处理所需数据的事件数据对象，包含待处理图片信息、进度指示器等
     * @return 始终返回 true，表示处理成功
     */
    @SuppressWarnings("D")
    @Override
    public boolean execute(EventData data) {
        ProgressIndicator indicator = data.getIndicator();
        int size = data.getSize();
        int totalProcessed = 0;

        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : data.getWaitingProcessMap().entrySet()) {
            int totalCount = imageEntry.getValue().size();
            Document document = imageEntry.getKey();
            VirtualFile currentFile = FileDocumentManager.getInstance().getFile(document);
            if (currentFile == null) {
                continue;
            }

            File curDocument = new File(currentFile.getPath());
            String savepath = STATE.getImageSavePath();

            for (MarkdownImage markdownImage : imageEntry.getValue()) {
                String imageName = markdownImage.getImageName();
                indicator.setText2("Processing " + imageName);
                indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);

                // 将 inputstream 转成 file
                File saveFile = null;
                File imageDir = new File(curDocument.getParent(), savepath);
                boolean checkDir = imageDir.exists() && imageDir.isDirectory();
                if (checkDir || imageDir.mkdirs()) {
                    // 保存的文件路径
                    saveFile = new File(imageDir, imageName);
                }
                if (saveFile == null) {
                    markdownImage.setFinalMark("copy error");
                    markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
                    markdownImage.setLocation(ImageLocationEnum.LOCAL);
                    continue;
                }

                try {
                    // todo-dong4j : (2019年03月29日 16:00) [如果覆盖 inputstream 所属文件将导致拷贝的文件错误]
                    FileUtil.copy(markdownImage.getInputStream(), new FileOutputStream(saveFile));
                } catch (IOException e) {
                    log.trace("", e);
                    markdownImage.setFinalMark("copy error");
                    markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
                    markdownImage.setLocation(ImageLocationEnum.LOCAL);
                    continue;
                }

                // 保存标签
                File imageFileRelativizePath = curDocument.getParentFile().toPath().relativize(saveFile.toPath()).toFile();
                String relImagePath = imageFileRelativizePath.toString().replace('\\', '/');
                markdownImage.setTitle("");
                markdownImage.setPath(relImagePath);
                try {
                    markdownImage.setInputStream(new FileInputStream(saveFile));
                } catch (FileNotFoundException e) {
                    log.trace("", e);
                }
                String mark = "![](" + relImagePath + ")";
                markdownImage.setOriginalLineText(mark);
                markdownImage.setOriginalMark(mark);
                markdownImage.setImageMarkType(ImageMarkEnum.ORIGINAL);
                markdownImage.setLocation(ImageLocationEnum.LOCAL);
                markdownImage.setFinalMark(mark);
            }
        }
        return true;
    }
}
