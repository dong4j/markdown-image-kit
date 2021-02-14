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

import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;

import java.util.Iterator;

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
public abstract class BaseActionHandler implements IActionHandler {
    /**
     * 是否符合该处理类的处理范围
     *
     * @param data the data
     * @return 是否符合 boolean
     * @since 0.0.1
     */
    @Override
    public boolean isEnabled(EventData data){
        return true;
    }

    /**
     * 执行具体的处理逻辑
     *
     * @param data the data
     * @return 是否阻止系统的事件传递 boolean
     * @since 0.0.1
     */
    @Override
    public boolean execute(EventData data){
        return true;
    }

    /**
     * 子类实现具体逻辑
     *
     * @param data          the data
     * @param imageIterator the image iterator
     * @param markdownImage the markdown image
     * @since 0.0.1
     */
    abstract void invoke(EventData data, Iterator<MarkdownImage> imageIterator, MarkdownImage markdownImage);
}
