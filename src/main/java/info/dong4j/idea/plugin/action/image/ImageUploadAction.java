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

package info.dong4j.idea.plugin.action.image;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.FinalChainHandler;
import info.dong4j.idea.plugin.chain.ImageCompressionHandler;
import info.dong4j.idea.plugin.chain.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.ImageRenameHandler;
import info.dong4j.idea.plugin.chain.ImageUploadHandler;
import info.dong4j.idea.plugin.chain.InsertToClipboardHandler;
import info.dong4j.idea.plugin.chain.OptionClientHandler;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ClientUtils;

import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 图片右键直接上传</p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-26 15:36
 */
@Slf4j
public final class ImageUploadAction extends ImageActionBase {
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return AllIcons.Debugger.Overhead;
    }

    @Override
    protected void buildChain(AnActionEvent event, Map<Document, List<MarkdownImage>> waitingProcessMap) {
        // 使用默认 client
        CloudEnum cloudEnum = OssState.getCloudType(STATE.getCloudType());
        OssClient client = ClientUtils.getClient(cloudEnum);

        EventData data = new EventData()
            .setActionEvent(event)
            .setProject(event.getProject())
            .setClient(client)
            .setClientName(cloudEnum.title)
            .setWaitingProcessMap(waitingProcessMap);

        ActionManager manager = new ActionManager(data)
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
            // 写到 clipboard
            .addHandler(new InsertToClipboardHandler())
            .addHandler(new FinalChainHandler());

        // 开启后台任务
        new ActionTask(event.getProject(), MikBundle.message("mik.action.upload.process", cloudEnum.title), manager).queue();
    }
}
