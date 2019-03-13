package info.dong4j.idea.plugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import org.jetbrains.annotations.NotNull;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 配置持久化 </p>
 *
 * @author dong4j
 * @date 2019 -03-13 11:56
 * @email sjdong3 @iflytek.com
 */
@Slf4j
@State(name = "AliyunOssUploadSettings", storages = @Storage(file = "aliyun-oss-upload.xml"))
public class AliyunOssSettings implements PersistentStateComponent<AliyunOssState> {

    /**
     * Get the instance of this service.
     *
     * @return the unique {@link AliyunOssSettings} instance.
     */
    public static AliyunOssSettings getInstance() {
        log.trace("get AliyunOssSettings getInstance");
        return ServiceManager.getService(AliyunOssSettings.class);
    }

    private AliyunOssState myState = new AliyunOssState();

    @Override
    public void loadState(@NotNull AliyunOssState state) {
        log.trace("loadState = {}", state);
        this.myState = state;
    }

    @NotNull
    @Override
    public AliyunOssState getState() {
        log.trace("get state = {}", myState);
        return this.myState;
    }
}