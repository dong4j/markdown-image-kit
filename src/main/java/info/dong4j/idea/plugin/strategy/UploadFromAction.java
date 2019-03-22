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
