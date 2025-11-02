package info.dong4j.idea.plugin.chain.handler;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.console.MikConsoleView;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.notify.UploadNotification;
import info.dong4j.idea.plugin.util.ClientUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 上传客户端处理类
 * <p>
 * 用于处理与OssClient相关的上传操作，主要负责检查OssClient是否可用，并在不可用时通知配置错误。
 * 该类继承自ActionHandlerAdapter，实现了具体的处理逻辑。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2021.02.14
 * @since 0.0.1
 */
@Slf4j
public class CheckAvailableClientHandler extends ActionHandlerAdapter {
    /**
     * 获取名称
     * <p>
     * 返回预定义的名称字符串，用于表示检查客户端的标题
     *
     * @return 名称字符串
     * @since 0.0.1
     */
    @Override
    public String getName() {
        return MikBundle.message("mik.action.checking.client.title");
    }

    /**
     * 执行具体的处理逻辑
     * <p>
     * 根据传入的事件数据执行处理逻辑，检查OSS客户端是否启用，若未启用则通知配置错误并返回false，否则返回true。
     *
     * @param data 事件数据，包含客户端信息和项目信息
     * @return 是否阻止系统的事件传递，true表示允许事件传递，false表示阻止
     * @since 0.0.1
     */
    @Override
    public boolean execute(EventData data){
        OssClient ossClient = data.getClient();
        if (ClientUtils.isNotEnable(ossClient)) {
            UploadNotification.notifyConfigurableError(data.getProject(), data.getClientName());
            MikConsoleView.printSmart(data.getProject(), "  [✗] 客户端配置错误: " + data.getClientName());
            return false;
        }
        MikConsoleView.printSmart(data.getProject(), "  客户端检查通过: " + data.getClientName());
        return true;
    }

}
