package info.dong4j.idea.plugin.entity;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

import info.dong4j.idea.plugin.client.OssClient;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Data
@Accessors(chain = true)
public class EventData {
    /** Action event */
    private AnActionEvent actionEvent;
    /** 显示当前处理进度 */
    private ProgressIndicator indicator;
    /** Project */
    private Project project;
    /** Editor */
    private Editor editor;
    /** Document */
    private Document document;
    /** 待处理的数据 */
    private Map<Document, List<MarkdownImage>> waitingProcessMap;
    /** 文件上传客户端 */
    private OssClient client;
    /** client name */
    private String clientName;
    /** chain size */
    private int size;
    /** 当前执行的节点 */
    private int index;
}
