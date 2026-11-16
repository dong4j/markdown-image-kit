package info.dong4j.idea.plugin.task;

/**
 * 上传任务成功后的回调。
 * <p>
 * 2026.3 起 {@code intellij.platform.externalSystem} 使用独立 classloader。
 * 任务链只需要一个成功通知，不再引用平台的 {@code TaskCallback}，
 * 避免加载上传任务时连带外部系统模块。
 *
 * @author dong4j
 * @version 2026.3.0
 * @since 2026.3.0
 */
public interface MikTaskCallback {
    /**
     * 任务成功后通知调用方。
     */
    void onSuccess();
}
