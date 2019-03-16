package info.dong4j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;

import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.settings.OssPersistenConfig;

import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Map;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 上传到七牛云 OSS 事件</p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-14 17:09
 */
public final class QiniuObjectStorageServiceAction extends AbstractObjectStorageServiceAction {

    @Contract(pure = true)
    @Override
    boolean isPassedTest() {
        return OssPersistenConfig.getInstance().getState().getQiniuOssState().isPassedTest();
    }

    @Contract(pure = true)
    @Override
    protected void upload(AnActionEvent event, Map<Document, List<MarkdownImage>> waitingForUploadImages) {

    }
}
