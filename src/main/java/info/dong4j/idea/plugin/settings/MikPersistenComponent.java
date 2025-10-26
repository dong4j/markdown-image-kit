package info.dong4j.idea.plugin.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import org.jetbrains.annotations.NotNull;

import lombok.extern.slf4j.Slf4j;

/**
 * 配置持久化组件
 * <p>
 * 用于管理配置信息的持久化操作，支持从指定存储位置加载和保存配置状态。
 * 该组件实现了 {@link PersistentStateComponent} 接口，用于与配置状态进行交互。
 * <p>
 * 支持的存储格式为 XML，存储路径为 "markdown.image.kit.configs.xml"。
 * 提供获取组件实例的方法，以及加载和获取配置状态的功能。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2019.03.13
 * @since 0.0.1
 */
@Slf4j
@State(name = "MikSettings", storages = @Storage("markdown.image.kit.configs.xml"))
public class MikPersistenComponent implements PersistentStateComponent<MikState> {
    /** 我的状态对象，用于存储和管理当前实例的相关状态信息 */
    private MikState myState = new MikState();

    /**
     * 获取该服务的实例。
     * <p>
     * 通过服务管理器获取唯一的 {@link MikPersistenComponent} 实例。
     *
     * @return 唯一的 {@link MikPersistenComponent} 实例
     * @since 0.0.1
     */
    public static MikPersistenComponent getInstance() {
        return ServiceManager.getService(MikPersistenComponent.class);
    }

    /**
     * 获取当前状态
     * <p>
     * 返回该对象当前的状态信息
     *
     * @return 当前状态
     * @since 0.0.1
     */
    @NotNull
    @Override
    public MikState getState() {
        return this.myState;
    }

    /**
     * 加载指定的状态对象
     * <p>
     * 将传入的状态对象赋值给当前实例的 myState 字段
     *
     * @param state 要加载的状态对象，不能为空
     * @since 0.0.1
     */
    @Override
    public void loadState(@NotNull MikState state) {
        this.myState = state;
    }
}
