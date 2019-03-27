/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package info.dong4j.idea.plugin.action.intention;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.ClientUtils;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 将已上传的图片迁移到当前 OSS</p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-27 13:39
 */
public class ImageLabelMoveIntentionAction extends IntentionActionBase {
    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        // 1. 获取 Map<Document, List<MarkdownImage>> waitingForUploadImages = new HashMap<>(1); 单个标签
        // 2. 排除 LOCAL 的 MarkdownImage
        // 3. 替换标签
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return false;
    }

    @Nls
    @NotNull
    @Override
    public String getText() {
        String clientName;
        CloudEnum cloudEnum = OssState.getCloudType(state.getCloudType());
        OssClient client = ClientUtils.getInstance(cloudEnum);
        if (client != null) {
            clientName = client.getName();
        } else {
            clientName = "OSS";
        }
        return MikBundle.message("mik.intention.move.message", clientName);
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }
}
