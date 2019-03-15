package info.dong4j.idea.plugin.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;

import info.dong4j.idea.plugin.entity.MarkdownImage;

import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Map;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 上传到微博 OSS 事件</p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-14 16:39
 */
public final class WeiboObjectStorageServiceAction extends AbstractObjectStorageService {
    @Contract(pure = true)
    @Override
    boolean isPassedTest() {
        // weibo 图床不需要测试
        return true;
    }

    @Contract(pure = true)
    @Override
    protected void upload(AnActionEvent event, Map<Document, List<MarkdownImage>> waitingForUploadImages) {
    }
}
