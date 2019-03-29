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

package info.dong4j.idea.plugin.strategy;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.task.UploadBackgroundTask;

import java.util.List;
import java.util.Map;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 从 action 发起的上传请求 </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-22 13:13
 */
@Deprecated
public class UploadFromAction implements UploadWay {
    private Project project;
    private OssClient ossClient;
    private Map<Document, List<MarkdownImage>> waitingForUploadImages;

    /**
     * Instantiates a new Upload from action.
     *
     * @param project                the project
     * @param ossClient              the oss client
     * @param waitingForUploadImages the waiting for upload images
     */
    public UploadFromAction(Project project, OssClient ossClient, Map<Document, List<MarkdownImage>> waitingForUploadImages) {
        this.project = project;
        this.ossClient = ossClient;
        this.waitingForUploadImages = waitingForUploadImages;
    }

    /**
     * todo-dong4j : (2019年03月15日 19:06) []
     * 1. 是否设置图片压缩
     * 2. 是否开启图床迁移
     * 3. 是否开启备份
     *
     * @return the string
     */
    @Override
    public String upload() {
        // 所有任务提交给后台任务进行, 避免大量上传阻塞 UI 线程
        new UploadBackgroundTask(project,
                                 MikBundle.message("mik.uploading.files.progress") + " " + ossClient.getName(),
                                 true,
                                 waitingForUploadImages,
                                 ossClient).queue();


        return "";
    }
}
