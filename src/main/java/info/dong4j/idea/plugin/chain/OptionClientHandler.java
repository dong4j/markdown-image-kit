/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j
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

import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.notify.UploadNotification;
import info.dong4j.idea.plugin.util.ClientUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 上传客户端处理</p>
 * 需要 OssClient
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-27 23:44
 */
@Slf4j
public class OptionClientHandler extends BaseActionHandler {
    public OptionClientHandler(String name) {
        handlerName = name;
    }

    @Override
    public String getName() {
        return handlerName;
    }

    /**
     * 指定的 client 不能为 null 且可用
     *
     * @param data the data
     * @return the boolean
     */
    @Override
    public boolean isEnabled(EventData data) {
        return true;
    }

    /**
     * 执行具体的处理逻辑
     *
     * @param data the data
     * @return 是否阻止系统的事件传递 boolean
     */
    @Override
    public boolean execute(EventData data){
        OssClient ossClient = data.getClient();
        if (ClientUtils.isNotEnable(ossClient)) {
            UploadNotification.notifyConfigurableError(data.getProject(), data.getClientName());
            return false;
        }
        return true;
    }
}
