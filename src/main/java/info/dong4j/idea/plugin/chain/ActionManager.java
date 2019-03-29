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

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.externalSystem.task.TaskCallback;
import com.intellij.openapi.progress.ProgressIndicator;

import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    public List<TaskCallback> getCallbacks() {
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

    /**
     * Build upload chain action manager.
     *
     * @param data the data
     * @return the action manager
     */
    public static ActionManager buildUploadChain(EventData data) {
        return new ActionManager(data)
            // 解析 markdown 文件
            .addHandler(new ResolveMarkdownFileHandler())
            // 图片压缩
            .addHandler(new ImageCompressionHandler())
            // 图片重命名
            .addHandler(new ImageRenameHandler())
            // 处理 client
            .addHandler(new OptionClientHandler())
            // 图片上传
            .addHandler(new ImageUploadHandler())
            // 标签转换
            .addHandler(new ImageLabelChangeHandler())
            // 写入标签
            .addHandler(new ReplaceToDocument())
            .addHandler(new FinalChainHandler());
    }

    /**
     * 生成图床迁移任务
     * 右键批量迁移和意图迁移需要的解析不同的数据
     * 右键批量迁移是直接解析当前文件中的图片标签, 只需要处理用户指定的标签,其他全部过滤点
     * 意图迁移只需要解析光标所在行的标签, 当标签所在图床与设置图床一致则不处理
     *
     * @param data    the data
     * @return the action manager
     */
    public static ActionManager buildMoveImageChain(EventData data) {
        // 过滤掉 LOCAL 和用户输入不匹配的标签
        ResolveMarkdownFileHandler resolveMarkdownFileHandler = new ResolveMarkdownFileHandler();
        resolveMarkdownFileHandler.setFileFilter((waitingProcessMap, filterString) -> {
            if (waitingProcessMap != null && waitingProcessMap.size() > 0) {
                for (Map.Entry<Document, List<MarkdownImage>> entry : waitingProcessMap.entrySet()) {
                    log.trace("old waitingProcessMap = {}", waitingProcessMap);

                    Iterator<MarkdownImage> iterator = entry.getValue().iterator();
                    while (iterator.hasNext()) {
                        MarkdownImage markdownImage = iterator.next();
                        OssClient client = data.getClient();
                        // 排除 LOCAL 和用户输入不匹配的标签和
                        if (markdownImage.getLocation().name().equals(ImageLocationEnum.LOCAL.name())
                            || !markdownImage.getPath().contains(filterString)
                            || markdownImage.getPath().contains(client.getCloudType().feature)) {

                            iterator.remove();
                        } else {
                            // 将 URL 图片转成 inputstream
                            try {
                                byte[] temp = IOUtils.toByteArray(new URL(markdownImage.getPath()));
                                InputStream inputStream = new ByteArrayInputStream(temp);
                                markdownImage.setInputStream(inputStream);
                                // 这里设置为本地图片, 才会在 uploadhandler 中上传
                                markdownImage.setLocation(ImageLocationEnum.LOCAL);
                            } catch (IOException e) {
                                log.trace("", e);
                                iterator.remove();
                            }
                        }
                    }
                    log.trace("new waitingProcessMap = {}", waitingProcessMap);
                }
            }
        });
        return new ActionManager(data)
            .addHandler(resolveMarkdownFileHandler)
            // 处理 client
            .addHandler(new OptionClientHandler())
            // 图片上传
            .addHandler(new ImageUploadHandler())
            // 标签转换
            .addHandler(new ImageLabelChangeHandler())
            // 写入标签
            .addHandler(new ReplaceToDocument())
            .addHandler(new FinalChainHandler());
    }
}
