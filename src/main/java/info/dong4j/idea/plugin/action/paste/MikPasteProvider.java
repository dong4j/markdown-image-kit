package info.dong4j.idea.plugin.action.paste;

import com.intellij.ide.PasteProvider;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.content.ImageContents;
import info.dong4j.idea.plugin.enums.InsertImageActionEnum;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.util.ImageUtils;
import info.dong4j.idea.plugin.util.MarkdownUtils;

import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 自定义粘贴处理器：在 Markdown 编辑器中优先处理图片/文件粘贴，避免被默认逻辑拦截。
 */
public class MikPasteProvider implements PasteProvider {

    @Override
    public void performPaste(@NotNull DataContext dataContext) {
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            return;
        }

        MikState state = MikPersistenComponent.getInstance().getState();
        Map<DataFlavor, Object> clipboardData = ImageUtils.getDataFromClipboard();
        if (clipboardData == null || clipboardData.isEmpty()) {
            return;
        }

        DataFlavor flavor = clipboardData.keySet().iterator().next();
        if (DataFlavor.javaFileListFlavor.equals(flavor)) {
            if (handleFileListPlainText(editor, clipboardData.get(flavor), state)) {
                return;
            }
        }

        // 走 MIK 图片处理链路
        Caret caret = editor.getCaretModel().getCurrentCaret();
        new PasteImageAction(null).doExecute(editor, caret, dataContext);
    }

    @Override
    public boolean isPastePossible(@NotNull DataContext dataContext) {
        return true;
    }

    @Override
    public boolean isPasteEnabled(@NotNull DataContext dataContext) {
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
        if (editor == null || virtualFile == null || !MarkdownUtils.isMardownFile(virtualFile)) {
            return false;
        }

        MikState state = MikPersistenComponent.getInstance().getState();
        if (!state.isEnablePlugin()) {
            return false;
        }

        Map<DataFlavor, Object> clipboardData = ImageUtils.getDataFromClipboard();
        if (clipboardData == null || clipboardData.isEmpty()) {
            return false;
        }

        DataFlavor flavor = clipboardData.keySet().iterator().next();
        if (DataFlavor.imageFlavor.equals(flavor)) {
            return state.getInsertImageAction() != InsertImageActionEnum.NONE;
        }

        if (DataFlavor.javaFileListFlavor.equals(flavor)) {
            if (state.isPasteFileAsPlainText()) {
                return true;
            }
            if (state.getInsertImageAction() == InsertImageActionEnum.NONE) {
                return false;
            }
            return isAllImageFiles(clipboardData.get(flavor));
        }

        if (DataFlavor.stringFlavor.equals(flavor)) {
            if (!state.isApplyToNetworkImages()) {
                return false;
            }
            Object value = clipboardData.get(flavor);
            if (!(value instanceof String text)) {
                return false;
            }
            String trimmed = text.trim();
            if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                return false;
            }
            return isCaretInImagePath(editor);
        }

        return false;
    }

    private static boolean handleFileListPlainText(@NotNull Editor editor, Object fileListValue, MikState state) {
        if (!state.isPasteFileAsPlainText()) {
            return false;
        }

        if (!(fileListValue instanceof List<?> list) || list.isEmpty() || !(list.get(0) instanceof File)) {
            return false;
        }

        if (state.getInsertImageAction() != InsertImageActionEnum.NONE && isAllImageFiles(list)) {
            return false;
        }

        StringBuilder textToPaste = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (!(item instanceof File file)) {
                continue;
            }
            if (i > 0) {
                textToPaste.append("\n");
            }
            textToPaste.append(file.getName());
        }

        if (textToPaste.isEmpty()) {
            return false;
        }

        Caret caret = editor.getCaretModel().getCurrentCaret();
        int offset = caret.getOffset();
        Document document = editor.getDocument();
        ApplicationManager.getApplication().runWriteAction(() -> {
            document.insertString(offset, textToPaste.toString());
            caret.moveToOffset(offset + textToPaste.length());
        });
        return true;
    }

    private static boolean isAllImageFiles(Object fileListValue) {
        if (!(fileListValue instanceof List<?> list) || list.isEmpty() || !(list.get(0) instanceof File)) {
            return false;
        }

        for (Object item : list) {
            if (!(item instanceof File file)) {
                return false;
            }
            if (file.isDirectory() || !ImageUtils.isImageFile(file)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isCaretInImagePath(@NotNull Editor editor) {
        Caret caret = editor.getCaretModel().getCurrentCaret();
        Document document = editor.getDocument();
        int caretOffset = caret.getOffset();
        int documentLine = document.getLineNumber(caretOffset);
        int lineStartOffset = document.getLineStartOffset(documentLine);
        int lineEndOffset = document.getLineEndOffset(documentLine);
        String lineText = document.getText(new TextRange(lineStartOffset, lineEndOffset));

        int prefixIndex = lineText.indexOf(ImageContents.IMAGE_MARK_PREFIX);
        if (prefixIndex == -1) {
            return false;
        }

        int middleIndex = lineText.indexOf(ImageContents.IMAGE_MARK_MIDDLE, prefixIndex);
        if (middleIndex == -1) {
            return false;
        }

        int suffixIndex = lineText.indexOf(ImageContents.IMAGE_MARK_SUFFIX, middleIndex);
        if (suffixIndex == -1) {
            return false;
        }

        int pathStartOffset = middleIndex + ImageContents.IMAGE_MARK_MIDDLE.length();
        int caretOffsetInLine = caretOffset - lineStartOffset;
        return caretOffsetInLine >= pathStartOffset && caretOffsetInLine <= suffixIndex;
    }
}
