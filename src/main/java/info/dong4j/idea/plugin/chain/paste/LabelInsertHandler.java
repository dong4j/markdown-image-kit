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

package info.dong4j.idea.plugin.chain.paste;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.PasteActionHandler;
import info.dong4j.idea.plugin.entity.EventData;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 插入标签</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-26 12:40
 */
@Slf4j
public class LabelInsertHandler extends PasteActionHandler {

    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isClipboardControl() && (STATE.isCopyToDir() || STATE.isUploadAndReplace());
    }

    /**
     * 如果只是 save, 则插入 saveMarkList, 如果是 upload, 则插入 uploadedMarkList
     *
     * @return the boolean
     */
    @Override
    public boolean execute(EventData data) {
        log.trace("insert mark");

        Editor editor = data.getEditor();

        new Task.Backgroundable(editor.getProject(), MikBundle.message("mik.paste.insert.progress"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                startBackgroupTask(indicator);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int totalCount = data.getImageMap().size();
                int totalProcessed = 0;
                try {
                    List<String> markList;
                    // 以 isUploadAndReplace 优先
                    if (STATE.isUploadAndReplace()) {
                        indicator.setText2("Use Uploaded Mark: ");
                        markList = data.getUploadedMarkList();
                    } else {
                        indicator.setText2("Use Saved Mark: ");
                        markList = data.getSaveMarkList();
                    }

                    for (String mark : markList) {
                        indicator.setText2("Insert Mark: ");
                        Runnable r = () -> EditorModificationUtil.insertStringAtCaret(editor, mark);
                        WriteCommandAction.runWriteCommandAction(editor.getProject(), r);
                        indicator.setFraction(++totalProcessed * 1.0 / totalCount);
                    }
                } finally {
                    endBackgroupTask(indicator);
                }
            }
        }.queue();

        return false;
    }
}
