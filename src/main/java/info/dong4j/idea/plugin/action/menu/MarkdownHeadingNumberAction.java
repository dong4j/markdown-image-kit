package info.dong4j.idea.plugin.action.menu;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;
import info.dong4j.idea.plugin.util.ActionUtils;
import info.dong4j.idea.plugin.util.PsiDocumentUtils;

import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeader;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import icons.MikIcons;
import lombok.extern.slf4j.Slf4j;

/**
 * Markdown 标题编号操作类
 * <p>
 * 用于为 Markdown 文件的标题生成/更新编号，从二级标题（##）开始编号。
 * 同时生成 TOC（目录），使用 markdown 锚点可正确跳转。
 * 每次执行都重新生成，可以覆盖以前的标题序号。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.01.XX
 * @since 1.0.0
 */
@Slf4j
public final class MarkdownHeadingNumberAction extends AnAction {

    /** 标题匹配正则表达式 */
    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{2,6})\\s+(.+)$");

    /**
     * 标题信息类
     */
    private static class HeadingInfo {
        /** 标题级别（2-6，对应 ## 到 ######） */
        final int level;
        /** 原始标题文本（不含 # 和编号） */
        final String originalText;
        /** 行号（0-based） */
        final int lineNumber;
        /** 行在文档中的起始偏移量 */
        final int startOffset;
        /** 行在文档中的结束偏移量 */
        final int endOffset;
        /** 编号（如 "1", "2.1", "2.2.1"） */
        String number;

        HeadingInfo(int level, String originalText, int lineNumber, int startOffset, int endOffset) {
            this.level = level;
            this.originalText = originalText;
            this.lineNumber = lineNumber;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        /**
         * 生成锚点 ID
         * <p>
         * 将标题文本转换为可用于锚点的 ID，只需要将空格替换为连字符。
         *
         * @return 锚点 ID
         */
        String generateAnchorId() {
            // 使用带编号的标题文本（如果编号存在）
            String text = number != null ? number + " " + originalText : originalText;

            // 只将空格替换为连字符
            return text.replaceAll("\\s+", "-");
        }

        /**
         * 生成带编号的标题行
         *
         * @return 带编号的标题行
         */
        String generateNumberedHeading() {
            String hashes = "#".repeat(level);
            String numberedText = number != null ? number + " " + originalText : originalText;
            return hashes + " " + numberedText;
        }

        /**
         * 生成 TOC 条目
         *
         * @param indent 缩进级别
         * @return TOC 条目
         */
        String generateTocEntry(int indent) {
            String indentStr = "  ".repeat(indent);
            String anchorId = generateAnchorId();
            String displayText = number != null ? number + " " + originalText : originalText;
            return indentStr + "- [" + displayText + "](#" + anchorId + ")";
        }
    }

    /**
     * 更新操作
     * <p>
     * 检查操作是否可用，设置图标和文本
     *
     * @param event 事件对象，包含操作上下文信息
     */
    @Override
    public void update(@NotNull AnActionEvent event) {
        // 检查全局开关
        MikState state = MikPersistenComponent.getInstance().getState();
        if (!state.isEnablePlugin()) {
            event.getPresentation().setEnabled(false);
            return;
        }

        // 调用基础的可用性检查
        ActionUtils.isAvailable(true, event, MikIcons.MIK, MarkdownContents.MARKDOWN_TYPE_NAME);

        // 设置菜单标题
        event.getPresentation().setText(MikBundle.message("mik.action.menu.heading.number.title"));
        event.getPresentation().setDescription(MikBundle.message("mik.action.menu.heading.number.description"));
    }

    /**
     * 处理动作事件，用于生成/更新 Markdown 标题编号和 TOC
     *
     * @param event 动作事件对象，包含触发动作的相关信息
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        // 优先从编辑器获取文档（编辑器右键）
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        Document document;
        PsiFile psiFile;

        if (editor != null) {
            document = editor.getDocument();
            psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        } else {
            // 没有编辑器时，从文件树（Project View）获取选中的 Markdown 文件
            VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
            if (virtualFile == null) {
                return;
            }

            document = FileDocumentManager.getInstance().getDocument(virtualFile);
            if (document == null) {
                return;
            }

            psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        }

        // 检查是否为 Markdown 文件
        if (psiFile == null || !MarkdownContents.MARKDOWN_TYPE_NAME.equals(psiFile.getFileType().getName())) {
            return;
        }

        try {
            // 解析所有标题（使用 PSI 树）
            List<HeadingInfo> headings = parseHeadings(psiFile, document);

            if (headings.isEmpty()) {
                log.info("未找到需要编号的标题（从二级标题开始）");
                return;
            }

            // 生成编号
            generateNumbers(headings);

            // 生成新的文档内容
            String newContent = generateNewContent(document, headings);

            // 写入文档
            PsiDocumentUtils.commitAndSaveDocument(project, document, newContent);

            log.info("成功为 {} 个标题生成了编号", headings.size());
        } catch (Exception e) {
            log.error("生成标题编号时发生错误", e);
        }
    }

    /**
     * 解析文档中的所有标题（从二级标题开始）
     * <p>
     * 优先使用 Markdown 插件的 PSI API 解析标题，如果不可用则回退到正则表达式。
     *
     * @param psiFile  PSI 文件对象
     * @param document 文档对象
     * @return 标题信息列表
     */
    private List<HeadingInfo> parseHeadings(@NotNull PsiFile psiFile,
                                            @NotNull Document document) {
        // 尝试使用 Markdown 插件的 PSI API
        try {
            // 查找 MarkdownHeader 类型的 PSI 元素
            Collection<MarkdownHeader> headerElements = PsiTreeUtil.findChildrenOfType(psiFile, MarkdownHeader.class);

            if (!headerElements.isEmpty()) {
                return parseHeadingsFromPsi(headerElements, document);
            }
        } catch (Exception e) {
            log.debug("使用 PSI API 解析标题失败，回退到正则表达式: {}", e.getMessage());
        }

        // 回退到正则表达式解析
        return parseHeadingsWithRegex(document);
    }

    /**
     * 使用 PSI 元素解析标题
     *
     * @param headerElements 标题 PSI 元素列表
     * @param document       文档对象
     * @return 标题信息列表
     */
    private List<HeadingInfo> parseHeadingsFromPsi(@NotNull Collection<MarkdownHeader> headerElements,
                                                   @NotNull Document document) {
        List<HeadingInfo> headings = new ArrayList<>();

        for (MarkdownHeader markdownHeader : headerElements) {
            try {
                final int level = markdownHeader.getLevel();

                // 只处理二级标题及以上（level >= 2）
                if (level < 2) {
                    continue;
                }

                String text = markdownHeader.getText();
                // 移除 # 符号和前后空格
                text = text.replaceAll("^#+\\s*", "").trim();
                if (text.isEmpty()) {
                    continue;
                }

                // 排除目录标题
                String normalizedText = normalizeHeadingText(text);
                if ("目录".equals(normalizedText)) {
                    continue;
                }

                // 标准化标题文本，移除所有可能的序号格式
                text = normalizedText;

                // 获取行号和偏移量
                TextRange textRange = markdownHeader.getTextRange();
                int lineNumber = document.getLineNumber(textRange.getStartOffset());
                int lineStartOffset = document.getLineStartOffset(lineNumber);
                int lineEndOffset = document.getLineEndOffset(lineNumber);

                headings.add(new HeadingInfo(level, text, lineNumber, lineStartOffset, lineEndOffset));
            } catch (Exception e) {
                log.warn("解析标题 PSI 元素失败: {}", e.getMessage());
            }
        }

        // 按行号排序
        headings.sort(Comparator.comparingInt(a -> a.lineNumber));
        return headings;
    }

    /**
     * 使用正则表达式解析标题（回退方案）
     *
     * @param document 文档对象
     * @return 标题信息列表
     */
    private List<HeadingInfo> parseHeadingsWithRegex(@NotNull Document document) {
        List<HeadingInfo> headings = new ArrayList<>();
        String content = document.getText();
        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher matcher = HEADING_PATTERN.matcher(line);
            if (matcher.matches()) {
                int level = matcher.group(1).length();
                // 只处理二级标题及以上（level >= 2）
                if (level >= 2) {
                    String text = matcher.group(2).trim();

                    // 排除目录标题（可能带编号，如 "## 目录" 或 "## 1 目录"）
                    String normalizedText = normalizeHeadingText(text);
                    if ("目录".equals(normalizedText)) {
                        continue;
                    }

                    // 标准化标题文本，移除所有可能的序号格式
                    text = normalizedText;

                    int lineStartOffset = document.getLineStartOffset(i);
                    int lineEndOffset = document.getLineEndOffset(i);

                    headings.add(new HeadingInfo(level, text, i, lineStartOffset, lineEndOffset));
                }
            }
        }

        return headings;
    }

    /**
     * 标准化标题文本，移除所有可能的序号格式
     * <p>
     * 移除的序号格式包括：
     * - 数字序号：1. , 1.1 , 1.1.1  等
     * - 中文序号：一、, 二、, 三、, （一）, （二）, （三） 等
     * - 罗马数字：I. , II. , III.  等
     * - 字母序号：A. , B. , a. , b.  等
     * - 其他格式：第1章, 第1节, 第一章, 第一节 等
     *
     * @param text 原始标题文本
     * @return 标准化后的标题文本（不含序号）
     */
    private String normalizeHeadingText(@NotNull String text) {
        if (text.isEmpty()) {
            return text;
        }

        // 移除"第X章/节"格式：第1章, 第1节, 第一章, 第一节 等（优先处理，避免与其他规则冲突）
        text = text.replaceAll("^第[\\d一二三四五六七八九十百千万]+[章节]\\s*", "");

        // 移除带括号的中文序号：（一）, （二）, （三） 等
        text = text.replaceAll("^[（(][一二三四五六七八九十百千万]+[）)]\\s*", "");

        // 移除带括号的数字序号：(1), (2), (3) 等
        text = text.replaceAll("^[（(]\\d+[）)]\\s*", "");

        // 移除带括号的字母序号：(A), (B), (a), (b) 等
        text = text.replaceAll("^[（(][A-Za-z][）)]\\s*", "");

        // 移除数字序号：1. , 1.1 , 1.1.1  等
        text = text.replaceAll("^\\d+(\\.\\d+)*\\s*[.、]?\\s*", "");

        // 移除中文序号：一、, 二、, 三、 等
        // 匹配以中文数字开头，后跟"、"或"."的格式
        // 支持：一、二、三...十、十一、十二...九十九、一百等
        // 使用通用匹配：一个或多个中文数字字符，后跟"、"或"."或空格
        text = text.replaceAll("^[一二三四五六七八九十百千万]+\\s*[、.]\\s*", "");
        // 处理没有标点的中文序号（较少见，但也要处理）
        text = text.replaceAll("^[一二三四五六七八九十百千万]+\\s+", "");

        // 移除罗马数字序号：I. , II. , III. , IV. , V.  等
        // 注意：必须要求有标点，避免误匹配单词（如 "IDEA"）
        text = text.replaceAll("^[IVX]+\\s*[.、]\\s*", "");

        // 移除字母序号：A. , B. , a. , b.  等（单个字母，必须后跟标点）
        // 注意：必须要求有标点，避免误匹配单词（如 "IDEA"）
        text = text.replaceAll("^[A-Za-z]\\s*[.、]\\s*", "");

        // 移除可能残留的前导空格和标点
        text = text.replaceAll("^\\s*[.、。，]?\\s*", "");

        return text.trim();
    }

    /**
     * 为标题生成编号
     * <p>
     * 根据标题层级生成编号，例如：
     * - ## xxx -> 1
     * - ## yyy -> 2
     * - ### zzz -> 2.1
     *
     * @param headings 标题信息列表
     */
    private void generateNumbers(@NotNull List<HeadingInfo> headings) {
        int[] counters = new int[7]; // 支持 6 级标题（索引 0 不使用，1-6 对应 # 到 ######）

        for (HeadingInfo heading : headings) {
            int level = heading.level;

            // 重置比当前级别更深的计数器
            for (int i = level + 1; i < counters.length; i++) {
                counters[i] = 0;
            }

            // 增加当前级别的计数器
            counters[level]++;

            // 生成编号
            StringBuilder numberBuilder = new StringBuilder();
            for (int i = 2; i <= level; i++) {
                if (i > 2) {
                    numberBuilder.append(".");
                }
                numberBuilder.append(counters[i]);
            }
            heading.number = numberBuilder.toString();
        }
    }

    /**
     * 生成新的文档内容（包含编号和 TOC）
     *
     * @param document 原始文档
     * @param headings 标题信息列表
     * @return 新的文档内容
     */
    private String generateNewContent(@NotNull Document document, @NotNull List<HeadingInfo> headings) {
        String originalContent = document.getText();
        String[] lines = originalContent.split("\n", -1);
        StringBuilder newContent = new StringBuilder();

        // 创建标题行号到标题信息的映射
        java.util.Map<Integer, HeadingInfo> headingMap = new java.util.HashMap<>();
        for (HeadingInfo heading : headings) {
            headingMap.put(heading.lineNumber, heading);
        }

        // 查找是否已经存在 "## 目录" 标题
        int existingTocStartLine = -1;
        int existingTocEndLine = -1;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            // 检查是否是 "## 目录" 标题（可能带编号，如 "## 1 目录"）
            if (line.matches("^##\\s+(\\d+\\s+)?目录$")) {
                existingTocStartLine = i;
                // 查找目录内容的结束位置（下一个标题之前或连续空行）
                for (int j = i + 1; j < lines.length; j++) {
                    String nextLine = lines[j].trim();
                    // 如果遇到下一个二级标题（##），目录结束
                    if (nextLine.startsWith("##") && !nextLine.matches("^##\\s+(\\d+\\s+)?目录$")) {
                        existingTocEndLine = j - 1;
                        break;
                    }
                    // 如果遇到连续两个空行，目录可能结束
                    if (j > i + 1 && nextLine.isEmpty() && lines[j - 1].trim().isEmpty()) {
                        existingTocEndLine = j - 2;
                        break;
                    }
                }
                // 如果没找到结束位置，目录到第一个内容标题之前
                if (existingTocEndLine == -1) {
                    int firstContentHeadingLine = headings.isEmpty() ? lines.length : headings.get(0).lineNumber;
                    existingTocEndLine = Math.min(firstContentHeadingLine - 1, lines.length - 1);
                }
                break;
            }
        }

        // 查找第一个内容标题的行号（排除目录标题）
        int firstHeadingLine = headings.isEmpty() ? -1 : headings.get(0).lineNumber;
        boolean tocInserted = false;

        // 按行处理
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // 如果当前行在已存在的目录范围内，替换或跳过
            if (existingTocStartLine != -1 && i >= existingTocStartLine && i <= existingTocEndLine) {
                // 如果是目录标题行，替换为新的目录
                if (i == existingTocStartLine) {
                    newContent.append(generateToc(headings));
                    tocInserted = true;
                    // 如果不是最后一行，添加换行符
                    if (i < lines.length - 1) {
                        newContent.append("\n");
                    }
                }
                // 跳过目录内容行（已经在 generateToc 中生成了）
                continue;
            }

            // 如果是第一个内容标题所在行，且还没有插入 TOC，先插入 TOC
            if (i == firstHeadingLine && !tocInserted) {
                newContent.append(generateToc(headings));
                newContent.append("\n\n");
                tocInserted = true;
            }

            // 检查当前行是否是标题
            HeadingInfo heading = headingMap.get(i);
            if (heading != null) {
                // 替换为带编号的标题
                newContent.append(heading.generateNumberedHeading());
            } else {
                // 保持原样
                newContent.append(line);
            }

            // 如果不是最后一行，添加换行符
            if (i < lines.length - 1) {
                newContent.append("\n");
            }
        }

        return newContent.toString();
    }

    /**
     * 生成 TOC（目录）
     * <p>
     * 只包含二级标题（##），不包含三级及以上的标题
     *
     * @param headings 标题信息列表
     * @return TOC 内容
     */
    private String generateToc(@NotNull List<HeadingInfo> headings) {
        if (headings.isEmpty()) {
            return "";
        }

        StringBuilder toc = new StringBuilder();
        toc.append("## 目录\n");

        for (HeadingInfo heading : headings) {
            // 只包含二级标题（level == 2）
            if (heading.level == 2) {
                // 二级标题无缩进
                toc.append(heading.generateTocEntry(0)).append("\n");
            }
        }

        return toc.toString();
    }

    /**
     * 获取动作更新线程
     *
     * <p>指定 update 方法在后台线程中执行，避免阻塞事件调度线程(EDT)。
     * 提高 UI 响应性，防止界面卡顿。
     *
     * @return ActionUpdateThread.BGT 后台线程
     */
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}

