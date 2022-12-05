package info.dong4j.idea.plugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import org.jetbrains.annotations.NotNull;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: 配置持久化 </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.13 11:56
 * @since 0.0.1
 */
@Slf4j
@State(name = "MikSettings", storages = @Storage("markdown.image.kit.configs.xml"))
public class MikPersistenComponent implements PersistentStateComponent<MikState> {

    /** My state */
    private MikState myState = new MikState();

    /**
     * Get the instance of this service.
     *
     * @return the unique {@link MikPersistenComponent} instance.
     * @since 0.0.1
     */
    public static MikPersistenComponent getInstance() {
        return ServiceManager.getService(MikPersistenComponent.class);
    }

    /**
     * Gets state *
     *
     * @return the state
     * @since 0.0.1
     */
    @NotNull
    @Override
    public MikState getState() {
        return this.myState;
    }

    /**
     * Load state
     *
     * @param state state
     * @since 0.0.1
     */
    @Override
    public void loadState(@NotNull MikState state) {
        this.myState = state;
    }
}
