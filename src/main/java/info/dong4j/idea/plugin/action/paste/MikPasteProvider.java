package info.dong4j.idea.plugin.action.paste;

import com.intellij.ide.PasteProvider;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
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
 * Markdown Image Kit 自定义粘贴处理器
 * <p>
 * 该类实现了 {@link PasteProvider} 接口，用于在 Markdown 编辑器中优先处理图片/文件粘贴操作。
 * 通过注册为 {@code customPasteProvider} 扩展点并设置 {@code order="first"}，确保在 JetBrains 官方
 * Markdown 插件的粘贴处理器之前被调用，从而能够拦截并处理图片粘贴逻辑。
 * <p>
 * <b>工作原理：</b>
 * <ol>
 *   <li>IntelliJ IDEA 的粘贴处理流程会优先遍历所有 {@code customPasteProvider} 扩展点</li>
 *   <li>对于每个 provider，先调用 {@link #isPasteEnabled(DataContext)} 判断是否应该处理</li>
 *   <li>如果返回 true，则调用 {@link #performPaste(DataContext)} 执行粘贴逻辑</li>
 *   <li>一旦某个 provider 处理了粘贴，流程就会终止，不会继续后续的 handler 链</li>
 * </ol>
 * <p>
 * <b>处理场景：</b>
 * <ul>
 *   <li>剪贴板包含图片（{@link DataFlavor#imageFlavor}）</li>
 *   <li>剪贴板包含图片文件列表（{@link DataFlavor#javaFileListFlavor}）</li>
 *   <li>剪贴板包含网络图片 URL（{@link DataFlavor#stringFlavor}，且光标在图片路径中）</li>
 *   <li>粘贴文件为纯文本（启用此功能时）</li>
 * </ul>
 * <p>
 * <b>注意事项：</b>
 * <ul>
 *   <li>只在 Markdown 文件中启用，其他文件类型不处理</li>
 *   <li>需要插件启用且配置了插入图片操作（非 NONE）才会处理图片</li>
 *   <li>网络图片 URL 粘贴需要光标位于图片标签的路径部分才会处理</li>
 * </ul>
 *
 * @author dong4j
 * @version 2.4.0
 * @see PasteProvider
 * @see PasteImageAction
 * @since 2.4.0
 */
public class MikPasteProvider implements PasteProvider {

    /**
     * 执行粘贴操作
     * <p>
     * 当 {@link #isPasteEnabled(DataContext)} 返回 true 时，此方法会被调用以执行实际的粘贴逻辑。
     * 该方法会根据剪贴板数据类型，选择相应的处理方式：
     * <ul>
     *   <li>如果是文件列表且启用了"粘贴文件为纯文本"，则只粘贴文件名</li>
     *   <li>否则，调用 {@link PasteImageAction#doExecute} 执行完整的图片处理链路</li>
     * </ul>
     * <p>
     * <b>处理流程：</b>
     * <ol>
     *   <li>从 DataContext 获取 Editor 对象</li>
     *   <li>从剪贴板获取数据</li>
     *   <li>判断数据类型，如果是文件列表，先尝试处理"粘贴为纯文本"</li>
     *   <li>调用 {@link PasteImageAction#doExecute} 执行图片处理（压缩、重命名、上传等）</li>
     * </ol>
     *
     * @param dataContext 数据上下文，包含编辑器、文件等上下文信息
     * @see PasteImageAction#doExecute
     */
    @Override
    public void performPaste(@NotNull DataContext dataContext) {
        // 获取当前编辑器实例
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if (editor == null) {
            // 如果没有编辑器上下文，无法处理，直接返回
            return;
        }

        // 获取插件状态配置
        MikState state = MikPersistenComponent.getInstance().getState();

        // 从剪贴板获取数据
        Map<DataFlavor, Object> clipboardData = ImageUtils.getDataFromClipboard();
        if (clipboardData == null || clipboardData.isEmpty()) {
            // 剪贴板为空，无法处理
            return;
        }

        // 获取剪贴板数据的类型（通常只有一个类型）
        DataFlavor flavor = clipboardData.keySet().iterator().next();

        // 如果是文件列表类型，先检查是否需要处理为纯文本
        if (DataFlavor.javaFileListFlavor.equals(flavor)) {
            // 如果启用了"粘贴文件为纯文本"功能，且处理成功，则直接返回
            if (handleFileListPlainText(editor, clipboardData.get(flavor), state)) {
                return;
            }
        }

        // 走 MIK 图片处理链路
        // 注意：传入 null 作为 EditorActionHandler，因为此时不需要回退到默认 handler
        // 如果处理失败，doExecute 内部会处理（通常不会失败，因为已经在 isPasteEnabled 中判断过）
        Caret caret = editor.getCaretModel().getCurrentCaret();
        new PasteImageAction(null).doExecute(editor, caret, dataContext);
    }

    /**
     * 判断粘贴操作是否可能
     * <p>
     * 此方法用于判断在当前上下文中是否可以进行粘贴操作。
     * 由于我们的处理逻辑已经在 {@link #isPasteEnabled(DataContext)} 中进行了详细判断，
     * 这里直接返回 true，表示粘贴操作在技术上是可能的。
     * <p>
     * <b>注意：</b>实际的业务判断在 {@link #isPasteEnabled(DataContext)} 中进行。
     *
     * @param dataContext 数据上下文
     * @return 总是返回 true，表示粘贴操作在技术上是可能的
     * @see #isPasteEnabled(DataContext)
     */
    @Override
    public boolean isPastePossible(@NotNull DataContext dataContext) {
        return true;
    }

    /**
     * 获取动作更新线程
     * <p>
     * 指定 {@link #isPasteEnabled(DataContext)} 和 {@link #isPastePossible(DataContext)} 方法
     * 在后台线程（BGT）中执行，避免阻塞事件调度线程（EDT），提高 UI 响应性，防止界面卡顿。
     * <p>
     * <b>选择 BGT 的原因：</b>
     * <ul>
     *   <li>{@link #isPasteEnabled(DataContext)} 需要读取剪贴板数据，可能涉及 I/O 操作</li>
     *   <li>需要检查文件类型、验证文件列表等操作，可能比较耗时</li>
     *   <li>这些操作不应该阻塞 UI 线程，影响用户体验</li>
     * </ul>
     *
     * @return {@link ActionUpdateThread#BGT} 后台线程
     * @see ActionUpdateThread#BGT
     * @see ActionUpdateThread#EDT
     */
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        // 在后台线程中执行更新操作，避免阻塞 EDT
        return ActionUpdateThread.BGT;
    }

    /**
     * 判断是否应该启用粘贴处理
     * <p>
     * 这是粘贴处理的关键判断方法。IntelliJ IDEA 会遍历所有注册的 {@code customPasteProvider}，
     * 对每个 provider 调用此方法。如果返回 true，则会调用 {@link #performPaste(DataContext)} 执行粘贴。
     * <p>
     * <b>判断条件（必须全部满足）：</b>
     * <ol>
     *   <li>当前上下文有编辑器且文件是 Markdown 类型</li>
     *   <li>插件已启用（{@link MikState#isEnablePlugin()}）</li>
     *   <li>剪贴板包含数据</li>
     *   <li>根据剪贴板数据类型，满足相应的业务条件</li>
     * </ol>
     * <p>
     * <b>不同数据类型的处理逻辑：</b>
     * <ul>
     *   <li><b>图片类型（{@link DataFlavor#imageFlavor}）：</b>
     *       需要配置了插入图片操作（非 NONE）</li>
     *   <li><b>文件列表类型（{@link DataFlavor#javaFileListFlavor}）：</b>
     *       <ul>
     *         <li>如果启用了"粘贴文件为纯文本"，直接返回 true</li>
     *         <li>否则，需要配置了插入图片操作且所有文件都是图片</li>
     *       </ul>
     *   </li>
     *   <li><b>字符串类型（{@link DataFlavor#stringFlavor}）：</b>
     *       <ul>
     *         <li>需要启用了"应用到网络图片"功能</li>
     *         <li>字符串是 HTTP/HTTPS URL</li>
     *         <li>光标位于图片标签的路径部分</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * @param dataContext 数据上下文，包含编辑器、文件等上下文信息
     * @return 如果应该处理粘贴返回 true，否则返回 false
     */
    @Override
    public boolean isPasteEnabled(@NotNull DataContext dataContext) {
        // 获取编辑器和文件对象
        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        VirtualFile virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);

        // 必须满足：有编辑器、有文件、且文件是 Markdown 类型
        if (editor == null || virtualFile == null || !MarkdownUtils.isMardownFile(virtualFile)) {
            return false;
        }

        // 获取插件状态配置
        MikState state = MikPersistenComponent.getInstance().getState();

        // 插件必须启用
        if (!state.isEnablePlugin()) {
            return false;
        }

        // 从剪贴板获取数据
        Map<DataFlavor, Object> clipboardData = ImageUtils.getDataFromClipboard();
        if (clipboardData == null || clipboardData.isEmpty()) {
            return false;
        }

        // 获取剪贴板数据的类型（通常只有一个类型）
        DataFlavor flavor = clipboardData.keySet().iterator().next();

        // 处理图片类型（从剪贴板直接粘贴的图片）
        if (DataFlavor.imageFlavor.equals(flavor)) {
            // 需要配置了插入图片操作（非 NONE）
            return state.getInsertImageAction() != InsertImageActionEnum.NONE;
        }

        // 处理文件列表类型（从文件管理器复制的文件）
        if (DataFlavor.javaFileListFlavor.equals(flavor)) {
            // 如果启用了"粘贴文件为纯文本"功能，直接返回 true
            // 这样即使不是图片文件，也可以处理为纯文本
            if (state.isPasteFileAsPlainText()) {
                return true;
            }

            // 如果没有配置插入图片操作，不处理
            if (state.getInsertImageAction() == InsertImageActionEnum.NONE) {
                return false;
            }

            // 只有当所有文件都是图片时，才处理
            return isAllImageFiles(clipboardData.get(flavor));
        }

        // 处理字符串类型（可能是网络图片 URL）
        if (DataFlavor.stringFlavor.equals(flavor)) {
            // 需要启用了"应用到网络图片"功能
            if (!state.isApplyToNetworkImages()) {
                return false;
            }

            // 检查是否是字符串类型
            Object value = clipboardData.get(flavor);
            if (!(value instanceof String text)) {
                return false;
            }

            // 检查是否是 HTTP/HTTPS URL
            String trimmed = text.trim();
            if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                return false;
            }

            // 光标必须位于图片标签的路径部分，才会处理
            // 这样可以避免误处理普通的 URL 文本
            return isCaretInImagePath(editor);
        }

        // 其他类型不处理
        return false;
    }

    /**
     * 处理"粘贴文件为纯文本"功能
     * <p>
     * 当用户启用了"粘贴文件为纯文本"功能时，从文件管理器复制的文件/目录应该只粘贴文件名（纯文本），
     * 而不是被 IDEA 自动转换成 Markdown 链接格式 {@code [name](path)}。
     * <p>
     * <b>处理逻辑：</b>
     * <ol>
     *   <li>检查是否启用了"粘贴文件为纯文本"功能</li>
     *   <li>验证文件列表的有效性</li>
     *   <li>如果配置了插入图片操作且所有文件都是图片，则不处理（走图片处理逻辑）</li>
     *   <li>否则，提取所有文件名，用换行符连接</li>
     *   <li>在当前光标位置插入文件名文本</li>
     * </ol>
     * <p>
     * <b>示例：</b>
     * <ul>
     *   <li>复制文件：{@code file1.txt, file2.txt}</li>
     *   <li>粘贴结果：{@code file1.txt\nfile2.txt}</li>
     * </ul>
     *
     * @param editor        编辑器实例
     * @param fileListValue 文件列表对象（从剪贴板获取）
     * @param state         插件状态配置
     * @return 如果处理成功返回 true，否则返回 false
     */
    private static boolean handleFileListPlainText(@NotNull Editor editor, Object fileListValue, MikState state) {
        // 检查是否启用了"粘贴文件为纯文本"功能
        if (!state.isPasteFileAsPlainText()) {
            return false;
        }

        // 验证文件列表的有效性
        if (!(fileListValue instanceof List<?> list) || list.isEmpty() || !(list.get(0) instanceof File)) {
            return false;
        }

        // 如果配置了插入图片操作且所有文件都是图片，则不处理
        // 让图片文件走正常的图片处理逻辑（上传、保存等）
        if (state.getInsertImageAction() != InsertImageActionEnum.NONE && isAllImageFiles(list)) {
            return false;
        }

        // 提取所有文件名，用换行符连接
        StringBuilder textToPaste = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (!(item instanceof File file)) {
                continue;
            }
            // 多个文件之间用换行符分隔
            if (i > 0) {
                textToPaste.append("\n");
            }
            // 只添加文件名，不包含路径
            textToPaste.append(file.getName());
        }

        // 如果没有有效的文件名，不处理
        if (textToPaste.isEmpty()) {
            return false;
        }

        // 在当前光标位置插入文件名文本
        Caret caret = editor.getCaretModel().getCurrentCaret();
        int offset = caret.getOffset();
        Document document = editor.getDocument();

        // 必须在 WriteAction 中执行文档修改操作
        ApplicationManager.getApplication().runWriteAction(() -> {
            document.insertString(offset, textToPaste.toString());
            // 移动光标到插入文本的末尾
            caret.moveToOffset(offset + textToPaste.length());
        });

        return true;
    }

    /**
     * 判断文件列表中的所有文件是否都是图片文件
     * <p>
     * 用于验证从剪贴板获取的文件列表是否全部为图片文件。
     * 只有当所有文件都是图片时，才会走图片处理逻辑；如果包含非图片文件或目录，则不处理。
     * <p>
     * <b>判断条件：</b>
     * <ul>
     *   <li>文件列表不为空</li>
     *   <li>所有元素都是 File 对象</li>
     *   <li>所有文件都不是目录</li>
     *   <li>所有文件都是图片文件（通过 {@link ImageUtils#isImageFile(File)} 判断）</li>
     * </ul>
     * <p>
     * <b>用途：</b>
     * <ul>
     *   <li>在 {@link #isPasteEnabled(DataContext)} 中判断是否应该处理文件列表</li>
     *   <li>在 {@link #handleFileListPlainText} 中判断是否应该走图片处理逻辑</li>
     * </ul>
     *
     * @param fileListValue 文件列表对象（从剪贴板获取）
     * @return 如果所有文件都是图片文件返回 true，否则返回 false
     * @see ImageUtils#isImageFile(File)
     */
    private static boolean isAllImageFiles(Object fileListValue) {
        // 验证文件列表的基本有效性
        if (!(fileListValue instanceof List<?> list) || list.isEmpty() || !(list.get(0) instanceof File)) {
            return false;
        }

        // 遍历所有文件，检查是否都是图片文件
        for (Object item : list) {
            if (!(item instanceof File file)) {
                // 包含非 File 对象，不是有效的文件列表
                return false;
            }
            // 目录或非图片文件都不符合条件
            if (file.isDirectory() || !ImageUtils.isImageFile(file)) {
                return false;
            }
        }

        // 所有文件都是图片文件
        return true;
    }

    /**
     * 判断光标是否位于 Markdown 图片标签的路径部分
     * <p>
     * 用于验证光标是否在图片标签的路径部分（即 {@code ![](光标在这里)} 的情况）。
     * 只有当光标位于路径部分时，粘贴网络图片 URL 才会被处理，这样可以避免误处理普通的 URL 文本。
     * <p>
     * <b>判断逻辑：</b>
     * <ol>
     *   <li>获取光标所在行的文本</li>
     *   <li>查找图片标签的三个关键部分：
     *       <ul>
     *         <li>{@code ![} - 图片标签前缀</li>
     *         <li>{@code ](} - 图片标签中间部分（标题和路径的分隔符）</li>
     *         <li>{@code )} - 图片标签后缀</li>
     *       </ul>
     *   </li>
     *   <li>计算路径部分的起始和结束位置</li>
     *   <li>判断光标是否在路径部分范围内</li>
     * </ol>
     * <p>
     * <b>示例：</b>
     * <ul>
     *   <li>{@code ![图片](https://example.com/image.png)} - 光标在 URL 中，返回 true</li>
     *   <li>{@code ![图片](https://example.com/image.png)} - 光标在标题中，返回 false</li>
     *   <li>{@code 普通文本 https://example.com/image.png} - 光标在普通文本中，返回 false</li>
     * </ul>
     * <p>
     * <b>用途：</b>
     * 在 {@link #isPasteEnabled(DataContext)} 中判断是否应该处理网络图片 URL 粘贴。
     * 只有当光标位于图片路径中时，才会处理，避免误处理普通的 URL 文本。
     *
     * @param editor 编辑器实例
     * @return 如果光标位于图片标签的路径部分返回 true，否则返回 false
     * @see ImageContents#IMAGE_MARK_PREFIX
     * @see ImageContents#IMAGE_MARK_MIDDLE
     * @see ImageContents#IMAGE_MARK_SUFFIX
     */
    private static boolean isCaretInImagePath(@NotNull Editor editor) {
        // 获取当前光标和文档对象
        Caret caret = editor.getCaretModel().getCurrentCaret();
        Document document = editor.getDocument();

        // 计算光标所在行的位置信息
        int caretOffset = caret.getOffset();
        int documentLine = document.getLineNumber(caretOffset);
        int lineStartOffset = document.getLineStartOffset(documentLine);
        int lineEndOffset = document.getLineEndOffset(documentLine);

        // 获取光标所在行的完整文本
        String lineText = document.getText(new TextRange(lineStartOffset, lineEndOffset));

        // 查找图片标签的前缀 "!["
        int prefixIndex = lineText.indexOf(ImageContents.IMAGE_MARK_PREFIX);
        if (prefixIndex == -1) {
            // 行中没有图片标签前缀，不是图片标签
            return false;
        }

        // 查找图片标签的中间部分 "]("（标题和路径的分隔符）
        int middleIndex = lineText.indexOf(ImageContents.IMAGE_MARK_MIDDLE, prefixIndex);
        if (middleIndex == -1) {
            // 没有找到中间部分，不是完整的图片标签
            return false;
        }

        // 查找图片标签的后缀 ")"
        int suffixIndex = lineText.indexOf(ImageContents.IMAGE_MARK_SUFFIX, middleIndex);
        if (suffixIndex == -1) {
            // 没有找到后缀，不是完整的图片标签
            return false;
        }

        // 计算路径部分的起始位置（"](" 之后）
        int pathStartOffset = middleIndex + ImageContents.IMAGE_MARK_MIDDLE.length();

        // 计算光标在行内的相对位置
        int caretOffsetInLine = caretOffset - lineStartOffset;

        // 判断光标是否在路径部分范围内（从 "](" 之后到 ")" 之前）
        return caretOffsetInLine >= pathStartOffset && caretOffsetInLine <= suffixIndex;
    }
}
