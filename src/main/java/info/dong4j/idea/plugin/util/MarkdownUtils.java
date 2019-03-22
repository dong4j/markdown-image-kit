package info.dong4j.idea.plugin.util;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import info.dong4j.idea.plugin.content.MarkdownContents;

import org.jetbrains.annotations.NotNull;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-22 12:52
 */
public final class MarkdownUtils {
    /**
     * 通过文件验证是否为 markdown 且是否可写
     *
     * @param file the file
     * @return the boolean
     */
    public static boolean isValidForFile(@NotNull PsiFile file) {
        if (!isMardownFile(file)) {
            return false;
        }
        // 不可写时按钮不可用
        return file.isWritable();
    }

    /**
     * Is mardown file boolean.
     *
     * @param file the file
     * @return the boolean
     */
    public static boolean isMardownFile(PsiFile file) {
        return file.getFileType().getName().equals(MarkdownContents.MARKDOWN_FILE_TYPE)
               || file.getName().endsWith(MarkdownContents.MARKDOWN_FILE_SUFIX);
    }

    /**
     * Is mardown file boolean.
     *
     * @param file the file
     * @return the boolean
     */
    public static boolean isMardownFile(VirtualFile file) {
        return file.getFileType().getName().equals(MarkdownContents.MARKDOWN_FILE_TYPE)
               || file.getName().endsWith(MarkdownContents.MARKDOWN_FILE_SUFIX);
    }
}
