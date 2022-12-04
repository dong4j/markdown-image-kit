/*
 * MIT License
 *
 * Copyright (c) 2022 dong4j <dong4j@gmail.com>
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
 */

package info.dong4j.idea.plugin.chain;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.editor.Document;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.util.MarkdownUtils;

import java.util.List;
import java.util.Map;

import lombok.Setter;

/**
 * <p>Description: 解析 markdown 文件</p>
 * 需要 AnActionEvent(不是必须的) 和 Project
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Setter
public class ResolveMarkdownFileHandler extends ActionHandlerAdapter {
    /** File filter */
    private MarkdownFileFilter fileFilter;

    /**
     * Gets name *
     *
     * @return the name
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.resolve.markdown.file.title");
    }

    /**
     * 优先使用 EventData 中的数据, 如果没有再解析
     *
     * @param data the data
     * @return the boolean
     * @since 0.0.1
     */
    @Override
    public boolean execute(EventData data) {
        // 优先处理设置的数据, 用于 ImageMoveIntentionAction 和 ImageUploadIntentionAction
        Map<Document, List<MarkdownImage>> waitingProcessMap = data.getWaitingProcessMap();
        if (waitingProcessMap == null || waitingProcessMap.size() == 0) {
            // 解析当前文档或者选择的文件树中的所有 markdown 文件.
            waitingProcessMap = MarkdownUtils.getProcessMarkdownInfo(data.getActionEvent(), data.getProject());
            data.setWaitingProcessMap(waitingProcessMap);
        }

        if (this.fileFilter != null) {
            PropertiesComponent propComp = PropertiesComponent.getInstance();
            this.fileFilter.filter(waitingProcessMap, propComp.getValue(MarkdownFileFilter.FILTER_KEY));
        }

        // 有数据才执行后面的 handler
        return waitingProcessMap.size() > 0;
    }
}
