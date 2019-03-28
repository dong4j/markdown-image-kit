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

package info.dong4j.idea.plugin.chain;

import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.progress.ProgressIndicator;

import info.dong4j.idea.plugin.entity.EventData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-22 18:50
 */
@Slf4j
public class ActionManager {
    private List<IActionHandler> handlersChain = new LinkedList<>();
    private List<TaskCallback> callbacks = new ArrayList<>();

    private EventData data;

    /**
     * Instantiates a new Action manager.
     *
     * @param data the data
     */
    public ActionManager(EventData data) {
        this.data = data;
    }

    /**
     * Add handler action manager.
     *
     * @param handler the handler
     * @return the action manager
     */
    public ActionManager addHandler(IActionHandler handler) {
        this.handlersChain.add(handler);
        return this;
    }

    /**
     * Get callbacks list.
     *
     * @return the list
     */
    public List<TaskCallback> getCallbacks(){
        return callbacks;
    }

    /**
     * Add callback action manager.
     *
     * @param callback the callback
     * @return the action manager
     */
    public ActionManager addCallback(TaskCallback callback) {
        this.callbacks.add(callback);
        return this;
    }

    /**
     * Invoke.
     *
     * @param indicator the indicator
     */
    public void invoke(ProgressIndicator indicator) {
        int totalProcessed = 0;
        data.setIndicator(indicator);
        data.setSize(handlersChain.size());
        int index = 0;
        for (IActionHandler handler : handlersChain) {
            data.setIndex(index++);
            if (handler.isEnabled(data)) {
                log.trace("invoke {}", handler.getName());
                indicator.setText2(handler.getName());
                if (!handler.execute(data)) {
                    break;
                }
            }
            indicator.setFraction(++totalProcessed * 1.0 / handlersChain.size());
        }
    }
}
