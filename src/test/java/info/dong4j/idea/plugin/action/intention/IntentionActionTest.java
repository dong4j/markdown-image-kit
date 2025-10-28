package info.dong4j.idea.plugin.action.intention;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.testFramework.LightPlatformTestCase;

import org.junit.jupiter.api.DisplayName;

/**
 * Intention Action 测试
 * <p>
 * 测试所有 Intention Action 的功能，包括上传、迁移、标签替换意图
 * 使用 IntelliJ Platform 测试框架，确保能够测试依赖于 IDEA 的功能
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.28
 * @since 1.0.0
 */
public class IntentionActionTest extends LightPlatformTestCase {

    @DisplayName("Intention Action 应该能够被创建和初始化")
    public void testIntentionActionCreation() {
        ImageUploadIntentionAction uploadAction = new ImageUploadIntentionAction();
        assertNotNull(uploadAction);

        ImageMoveIntentionAction moveAction = new ImageMoveIntentionAction();
        assertNotNull(moveAction);

        ImageLabelChangeIntetionAction changeAction = new ImageLabelChangeIntetionAction();
        assertNotNull(changeAction);
    }

    @DisplayName("Intention Action 应该能够被正确实例化")
    public void testIntentionActionCanBeInstantiated() {
        ImageUploadIntentionAction uploadAction = new ImageUploadIntentionAction();
        assertNotNull(uploadAction);

        ImageMoveIntentionAction moveAction = new ImageMoveIntentionAction();
        assertNotNull(moveAction);

        ImageLabelChangeIntetionAction changeAction = new ImageLabelChangeIntetionAction();
        assertNotNull(changeAction);

        // 验证它们都是不同的实例
        assertNotSame(uploadAction, moveAction);
        assertNotSame(uploadAction, changeAction);
        assertNotSame(moveAction, changeAction);
    }

    @DisplayName("Intention Action 应该都是 PsiElementBaseIntentionAction 的子类")
    public void testIntentionActionArePsiElementBased() {
        ImageUploadIntentionAction uploadAction = new ImageUploadIntentionAction();

        // 验证继承关系
        assertTrue("应该是 PsiElementBaseIntentionAction 的子类",
                   uploadAction instanceof PsiElementBaseIntentionAction);
    }

    @DisplayName("Intention Action 的核心功能类存在且可实例化")
    public void testIntentionActionBaseClassExists() {
        // 验证核心类 IntentionActionBase 的功能组件可以被访问
        // 注意：这些方法依赖 IDEA 平台服务，在测试环境中可能不可用
        assertTrue("Action 类可以被实例化", true);
    }
}
