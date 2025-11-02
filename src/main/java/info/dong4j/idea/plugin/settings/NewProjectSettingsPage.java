package info.dong4j.idea.plugin.settings;

import com.intellij.openapi.options.SearchableConfigurable;

import info.dong4j.idea.plugin.settings.panel.ImageEnhancementPanel;
import info.dong4j.idea.plugin.settings.panel.ImageProcessingPanel;
import info.dong4j.idea.plugin.settings.panel.UploadServicePanel;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import lombok.extern.slf4j.Slf4j;

/**
 * 页面设置类 - 用于构建和管理 Markdown 图片处理插件的设置界面
 * <p>
 * 该类实现了设置面板的 UI 构建、初始化、配置保存、加载和状态检查等功能，支持持久化配置。
 * 主要包含图片处理、图片增强和上传服务三个模块的配置面板，并通过布局管理器组织整体界面。
 * <p>
 * 该类遵循 SearchableConfigurable 接口规范，用于在插件设置界面中展示和管理相关配置。
 *
 * @author dong4j
 * @version 2.0.0
 * @date 2025.11.01
 * @since 2.0.0
 */
@SuppressWarnings("D")
@Slf4j
public class NewProjectSettingsPage implements SearchableConfigurable {
    /** 用于存储和管理持久化组件的配置参数 */
    private final MikPersistenComponent config;
    /** 主面板，用于承载主要界面组件 */
    private final JPanel myMainPanel;

    /** 图像处理面板组件，用于展示和操作图像处理相关功能 */
    private ImageProcessingPanel imageProcessingPanel;
    /** 图像增强面板，用于处理和显示图像增强相关功能 */
    private ImageEnhancementPanel imageEnhancementPanel;
    /** 上传服务面板组件 */
    private UploadServicePanel uploadServicePanel;

    /**
     * 构造函数，初始化NewProjectSettingsPage对象
     * <p>
     * 创建NewProjectSettingsPage实例时，初始化配置对象和主面板组件
     *
     * @since 1.0
     */
    public NewProjectSettingsPage() {
        this.config = MikPersistenComponent.getInstance();
        this.myMainPanel = buildMainPanel();
    }

    /**
     * 创建主面板组件
     * <p>
     * 初始化并构建应用程序的主面板，使用 BorderLayout 布局管理器，包含图片处理、图片增强和上传服务设定等区域。
     *
     * @return 主面板组件
     */
    @NotNull
    private JPanel buildMainPanel() {
        // 使用 BorderLayout 作为主布局
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建左侧内容面板（使用垂直布局）
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // 1. 图片处理区域
        imageProcessingPanel = new ImageProcessingPanel();
        contentPanel.add(imageProcessingPanel.getContent());

        // 添加间距
        contentPanel.add(new JPanel()); // 占位符

        // 2. 图片增强处理区域
        imageEnhancementPanel = new ImageEnhancementPanel();
        contentPanel.add(imageEnhancementPanel.getContent());

        // 添加间距
        contentPanel.add(new JPanel()); // 占位符

        // 3. 上传服务设定区域
        uploadServicePanel = new UploadServicePanel();
        contentPanel.add(uploadServicePanel.getContent());

        // 使用 BorderLayout.NORTH 而不是 CENTER，避免纵向拉伸
        // 这样内容面板会根据其内容大小决定高度，而不会填满整个可用空间
        mainPanel.add(contentPanel, BorderLayout.NORTH);
        return mainPanel;
    }


    /**
     * 获取显示名称
     * <p>
     * 返回用于显示的名称，通常用于界面展示或标识用途。
     *
     * @return 显示名称
     */
    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Markdown Image Kit";
    }

    /**
     * 创建并返回组件
     * <p>
     * 该方法用于创建主面板组件，并根据配置初始化相关设置。
     *
     * @return 主面板组件，可能为 null
     */
    @Override
    public @Nullable JComponent createComponent() {
        this.initFromSettings();
        return myMainPanel;
    }

    /**
     * 获取当前组件的唯一标识符
     * <p>
     * 返回一个固定字符串，用于唯一标识该组件
     *
     * @return 组件唯一标识符
     */
    @Override
    public @NotNull String getId() {
        return "markdown.image.kit.preview";
    }

    /**
     * 每次打开设置面板时执行初始化操作
     * <p>
     * 该方法用于从配置中读取状态信息，并初始化各个图床配置、图片处理设置和上传服务设置。
     *
     * @since 2.0.0
     */
    private void initFromSettings() {
        MikState state = this.config.getState();
        imageProcessingPanel.initImageProcessingPanel(state);
        imageEnhancementPanel.initImageEnhancementPanel(state);
        uploadServicePanel.initUploadServicePanel(state);
    }

    /**
     * 判断配置是否发生修改
     * <p>
     * 检查各个配置面板的设置是否发生变化，若所有设置均未修改，则返回 false，否则返回 true
     *
     * @return 配置是否发生修改
     */
    @Override
    public boolean isModified() {
        log.trace("isModified invoke");
        MikState state = this.config.getState();
        // 检查图片处理设置是否修改
        boolean imageProcessingModified = imageProcessingPanel.isImageProcessingModified(state);
        // 检查图片增强处理设置是否修改
        boolean imageEnhancementModified = imageEnhancementPanel.isImageEnhancementModified(state);
        // 检查上传服务设定是否修改
        boolean uploadServiceModified = uploadServicePanel.isUploadServiceModified(state);

        return imageProcessingModified || imageEnhancementModified || uploadServiceModified;
    }

    /**
     * 应用配置到各个面板和上传服务
     * <p>
     * 从配置中获取状态对象，并将其应用到图片增强处理面板、图片处理面板以及上传服务面板上。
     */
    @Override
    public void apply() {
        MikState state = this.config.getState();
        // 应用图片增强处理设置
        imageEnhancementPanel.applyImageEnhancementConfigs(state);
        // 应用图片处理设置
        imageProcessingPanel.applyImageProcessingConfigs(state);
        // 应用上传服务设定
        uploadServicePanel.applyUploadServiceConfigs(state);
    }

    /**
     * 重置所有面板和组件的配置状态
     * <p>
     * 通过获取当前配置状态，重新初始化图片处理、图片增强和上传服务相关的面板组件
     */
    @Override
    public void reset() {
        MikState state = this.config.getState();
        // 重置图片处理设置
        imageProcessingPanel.initImageProcessingPanel(state);
        // 重置图片增强处理设置
        imageEnhancementPanel.initImageEnhancementPanel(state);
        // 重置上传服务设定
        uploadServicePanel.initUploadServicePanel(state);
    }
}

