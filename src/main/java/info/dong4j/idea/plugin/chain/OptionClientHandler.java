package info.dong4j.idea.plugin.chain;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.notify.UploadNotification;
import info.dong4j.idea.plugin.util.ClientUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: 上传客户端处理</p>
 * 需要 OssClient
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Slf4j
public class OptionClientHandler extends ActionHandlerAdapter {

    /**
     * Gets name *
     *
     * @return the name
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.checking.client.title");
    }

    /**
     * 执行具体的处理逻辑
     *
     * @param data the data
     * @return 是否阻止系统的事件传递 boolean
     * @since 0.0.1
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
