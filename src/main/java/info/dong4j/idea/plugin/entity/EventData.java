package info.dong4j.idea.plugin.entity;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

import info.dong4j.idea.plugin.chain.ProgressTracker;
import info.dong4j.idea.plugin.client.OssClient;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 事件数据类
 * <p>
 * 用于封装处理事件过程中所需的各种数据和状态信息，包括动作事件、进度指示器、项目、编辑器、文档、待处理数据、文件上传客户端等。
 * 该类主要用于事件驱动的处理流程中，提供统一的数据结构以支持事件的传递和处理。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Data
@Accessors(chain = true)
public class EventData {
    private String action;
    /** Action 事件对象，用于传递和处理用户操作事件 */
    private AnActionEvent actionEvent;
    /** 显示当前处理进度 */
    private ProgressIndicator indicator;
    /** 项目对象，用于表示当前操作的项目信息 */
    private Project project;
    /** 编辑器实例 */
    private Editor editor;
    /** 文档对象，用于存储和操作文档内容 */
    private Document document;
    /** 待处理的数据，键为文档对象，值为对应的Markdown图片列表 */
    private Map<Document, List<MarkdownImage>> waitingProcessMap;
    /** 文件上传客户端 */
    private OssClient client;
    /** 客户端名称 */
    private String clientName;
    /** 链表节点数量 */
    private int size;
    /** 当前执行的节点索引 */
    private int index;
    /** 进度跟踪器，用于统一管理进度展示 */
    private ProgressTracker progressTracker;

    /**
     * 临时存储路径
     * <p>
     * 用于图床迁移场景下的临时路径配置。当用户选择"本地存储"进行图床迁移，
     * 但全局设置中未配置有效的图片存储路径时，通过此字段传递用户在迁移对话框中
     * 输入的临时路径，供 {@link info.dong4j.idea.plugin.chain.handler.ImageStorageHandler} 使用。
     * <p>
     * 该字段优先级高于全局配置，仅在当前任务执行期间有效，不会影响用户的持久化配置。
     * 任务结束后应在 {@link info.dong4j.idea.plugin.chain.handler.FinalChainHandler} 中清理。
     * <p>
     * 使用场景：
     * <ul>
     *   <li>图床迁移到本地存储 ({@link info.dong4j.idea.plugin.action.menu.ImageMigrationAction})</li>
     *   <li>图床迁移意图操作 ({@link info.dong4j.idea.plugin.action.intention.ImageMigrationIntentionAction})</li>
     * </ul>
     *
     * @see info.dong4j.idea.plugin.settings.MoveToOtherOssSettingsDialog#getStoragePath()
     * @see info.dong4j.idea.plugin.chain.handler.ImageStorageHandler#execute(EventData)
     * @see info.dong4j.idea.plugin.chain.handler.FinalChainHandler#execute(EventData)
     * @since 2.0.0
     */
    private String temporaryStoragePath;
}
