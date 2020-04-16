/*
 * MIT License
 *
 * Copyright (c) 2020 dong4j <dong4j@gmail.com>
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

package info.dong4j.idea.plugin.chain;

import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MikState;

/**
 * <p>Company: no company</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email dong4j@gmail.com
 * @since 2019-03-22 18:35
 */
public interface IActionHandler {
    /**
     * The constant STATE.
     */
    MikState STATE = MikPersistenComponent.getInstance().getState();

    /**
     * Gets name.
     *
     * @return the name
     */
    String getName();

    /**
     * 是否符合该处理类的处理范围
     *
     * @param data the data
     * @return 是否符合 boolean false 则当前 handler 不执行
     */
    boolean isEnabled(EventData data);

    /**
     * 执行具体的处理逻辑
     *
     * @param data the data
     * @return 是否阻止系统的事件传递 boolean  为 false 时后一个 handler 不自信, 整个 chain 中断
     */
    boolean execute(EventData data);
}
