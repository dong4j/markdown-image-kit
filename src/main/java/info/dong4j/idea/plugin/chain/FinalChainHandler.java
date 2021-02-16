/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
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

import com.intellij.openapi.editor.Document;

import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>Company: no company</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
public class FinalChainHandler extends ActionHandlerAdapter {
    /**
     * Gets name *
     *
     * @return the name
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return "扫尾工作";
    }

    /**
     * Is enabled
     *
     * @param data data
     * @return the boolean
     * @since 0.0.1
     */
    @Override
    public boolean isEnabled(EventData data) {
        return STATE.isRename();
    }

    /**
     * 根据配置重新设置 imageName
     *
     * @param data the data
     * @return the boolean
     * @since 0.0.1
     */
    @Override
    public boolean execute(EventData data) {
        Map<Document, List<MarkdownImage>> processededData = data.getWaitingProcessMap();
        for (Map.Entry<Document, List<MarkdownImage>> imageEntry : processededData.entrySet()) {
            List<MarkdownImage> markdownImages = imageEntry.getValue();
            for (MarkdownImage markdownImage : markdownImages) {

                if (markdownImage.getInputStream() != null) {
                    try {
                        markdownImage.getInputStream().close();
                    } catch (IOException ignored) {
                    }
                }
            }
            markdownImages.clear();
        }
        processededData.clear();
        return true;
    }
}
