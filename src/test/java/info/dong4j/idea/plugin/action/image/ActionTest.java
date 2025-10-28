package info.dong4j.idea.plugin.action.image;

import com.intellij.testFramework.LightPlatformTestCase;

import org.junit.jupiter.api.DisplayName;

/**
 * Action 测试
 * <p>
 * 测试所有 Markdown Image Action 的功能
 * 使用 IntelliJ Platform 测试框架
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.28
 * @since 1.0.0
 */
public class ActionTest extends LightPlatformTestCase {

    @DisplayName("ImageUploadAction 应该能够被创建")
    public void testImageUploadActionCreation() {
        ImageUploadAction action = new ImageUploadAction();
        assertNotNull(action);
    }

    @DisplayName("ImageCompressAction 应该能够被创建")
    public void testImageCompressActionCreation() {
        ImageCompressAction action = new ImageCompressAction();
        assertNotNull(action);
    }

    @DisplayName("ImageUploadAction 应该有图标")
    public void testImageUploadActionHasIcon() {
        ImageUploadAction action = new ImageUploadAction();
        assertNotNull(action.getIcon());
    }

    @DisplayName("ImageCompressAction 应该有图标")
    public void testImageCompressActionHasIcon() {
        ImageCompressAction action = new ImageCompressAction();
        assertNotNull(action.getIcon());
    }
}
