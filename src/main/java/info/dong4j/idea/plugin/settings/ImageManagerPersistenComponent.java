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
@State(name = "ImageManagerSetting", storages = @Storage(file = "image.manager.configs.xml"))
public class ImageManagerPersistenComponent implements PersistentStateComponent<ImageManagerState> {

    /**
     * Get the instance of this service.
     *
     * @return the unique {@link ImageManagerPersistenComponent} instance.
     */
    public static ImageManagerPersistenComponent getInstance() {
        log.trace("get OssPersistenConfig getInstance");
        return ServiceManager.getService(ImageManagerPersistenComponent.class);
    }

    private ImageManagerState myState = new ImageManagerState();

    @Override
    public void loadState(@NotNull ImageManagerState state) {
        this.myState = state;
    }

    @NotNull
    @Override
    public ImageManagerState getState() {
        return this.myState;
    }
}