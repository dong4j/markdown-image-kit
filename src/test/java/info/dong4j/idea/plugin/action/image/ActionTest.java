package info.dong4j.idea.plugin.action.image;

import com.intellij.testFramework.LightPlatformTestCase;

import org.junit.jupiter.api.DisplayName;

/**
 * Action 测试类
 * <p>
 * 用于测试所有 Markdown 图像相关 Action 的功能，包括 Action 的创建和图标属性验证
 * 使用 IntelliJ Platform 测试框架进行单元测试
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.28
 * @since 1.0.0
 */
public class ActionTest extends LightPlatformTestCase {

    /**
     * 验证 ImageUploadAction 实例可以被成功创建
     * <p>
     * 该测试用例用于确保 ImageUploadAction 类的实例化过程正常，能够返回非空对象
     */
    @DisplayName("ImageUploadAction 应该能够被创建")
    public void testImageUploadActionCreation() {
        ImageUploadAction action = new ImageUploadAction();
        assertNotNull(action);
    }

    /**
     * 验证 ImageCompressAction 对象可以成功创建
     * <p>
     * 该测试用例用于确保 ImageCompressAction 类的实例化过程正常，能够返回非空对象
     */
    @DisplayName("ImageCompressAction 应该能够被创建")
    public void testImageCompressActionCreation() {
        ImageCompressAction action = new ImageCompressAction();
        assertNotNull(action);
    }

    /**
     * 验证 ImageUploadAction 是否具有图标
     * <p>
     * 创建 ImageUploadAction 实例并检查其图标属性是否不为空
     *
     * @since 1.0
     */
    @DisplayName("ImageUploadAction 应该有图标")
    public void testImageUploadActionHasIcon() {
        ImageUploadAction action = new ImageUploadAction();
        assertNotNull(action.getIcon());
    }

    /**
     * 验证 ImageCompressAction 是否具有图标
     * <p>
     * 创建 ImageCompressAction 实例并检查其图标属性是否不为空
     *
     * @since 1.0
     */
    @DisplayName("ImageCompressAction 应该有图标")
    public void testImageCompressActionHasIcon() {
        ImageCompressAction action = new ImageCompressAction();
        assertNotNull(action.getIcon());
    }
}
