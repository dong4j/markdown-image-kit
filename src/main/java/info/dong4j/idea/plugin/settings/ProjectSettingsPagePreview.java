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
 * 设置页面预览 - 使用 DLS 方式构建 UI
 * <p>
 * 这是一个重构版本，按照 todo.md 的需求重新组织布局：
 * 1. 图片处理（插入图片时的选项）
 * 2. 图片增强处理（压缩、WebP、重命名、水印等）
 * 3. 上传服务设定
 * <p>
 * 该版本实现了设置保存和加载逻辑，支持持久化配置
 *
 * @author dong4j
 * @version 2.0.0
 * @date 2025.11.01
 * @since 2.0.0
 */
@SuppressWarnings("D")
@Slf4j
public class ProjectSettingsPagePreview implements SearchableConfigurable {
    /**
     * 配置信息
     * <p>
     * 用于存储和管理持久化组件的配置参数
     *
     * @see MikPersistenComponent
     */
    private final MikPersistenComponent config;
    private final JPanel myMainPanel;

    private ImageProcessingPanel imageProcessingPanel;
    private ImageEnhancementPanel imageEnhancementPanel;
    private UploadServicePanel uploadServicePanel;

    /**
     * 构造函数
     */
    public ProjectSettingsPagePreview() {
        this.config = MikPersistenComponent.getInstance();
        this.myMainPanel = buildMainPanel();
    }

    /**
     * 创建主面板组件
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


    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Markdown Image Kit (Preview)";
    }

    @Override
    public @Nullable JComponent createComponent() {
        this.initFromSettings();
        return myMainPanel;
    }

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

        return !(imageEnhancementModified && uploadServiceModified && imageProcessingModified);
    }

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

