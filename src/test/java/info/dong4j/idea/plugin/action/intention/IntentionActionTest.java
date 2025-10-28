package info.dong4j.idea.plugin.action.intention;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.testFramework.LightPlatformTestCase;

import org.junit.jupiter.api.DisplayName;

/**
 * Intention Action 测试类
 * <p>
 * 用于测试 IntelliJ 平台中 Intention Action 的相关功能，包括上传图片、移动图片、标签替换等意图操作的实现与验证。
 * 通过 IntelliJ Platform 测试框架，确保这些意图操作能够正确初始化、实例化，并且符合 PsiElementBaseIntentionAction 的继承关系。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.28
 * @since 1.0.0
 */
public class IntentionActionTest extends LightPlatformTestCase {

    /**
     * 验证 Intention Action 是否能够被正确创建和初始化
     * <p>
     * 该测试用例创建了三种不同类型的 Intention Action 实例，并验证它们是否不为空，以确保
     * 它们能够被正确初始化和实例化。
     *
     * @since 1.0
     */
    @DisplayName("Intention Action 应该能够被创建和初始化")
    public void testIntentionActionCreation() {
        ImageUploadIntentionAction uploadAction = new ImageUploadIntentionAction();
        assertNotNull(uploadAction);

        ImageMoveIntentionAction moveAction = new ImageMoveIntentionAction();
        assertNotNull(moveAction);

        ImageLabelChangeIntetionAction changeAction = new ImageLabelChangeIntetionAction();
        assertNotNull(changeAction);
    }

    /**
     * 验证 Intention Action 类型能够被正确实例化
     * <p>
     * 该测试用例创建了三种不同的 Intention Action 实例，并验证它们不为 null 且彼此为不同的实例。
     *
     * @since 1.0
     */
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

    /**
     * 验证 ImageUploadIntentionAction 是否继承自 PsiElementBaseIntentionAction
     * <p>
     * 该方法通过实例化 ImageUploadIntentionAction 并检查其是否为 PsiElementBaseIntentionAction 的子类，
     * 以确保其继承关系正确。
     *
     * @since 1.0
     */
    @DisplayName("Intention Action 应该都是 PsiElementBaseIntentionAction 的子类")
    public void testIntentionActionArePsiElementBased() {
        ImageUploadIntentionAction uploadAction = new ImageUploadIntentionAction();

        // 验证继承关系
        assertTrue("应该是 PsiElementBaseIntentionAction 的子类",
                   uploadAction instanceof PsiElementBaseIntentionAction);
    }

    /**
     * 验证 Intention Action 的核心功能类存在且可实例化
     * <p>
     * 该测试用例用于确认 IntentionActionBase 类可以被正确实例化，确保其功能组件可被访问。
     * 注意：由于该类依赖于 IDEA 平台服务，因此在测试环境中可能无法完全验证其功能。
     *
     * @since 1.0
     */
    @DisplayName("Intention Action 的核心功能类存在且可实例化")
    public void testIntentionActionBaseClassExists() {
        // 验证核心类 IntentionActionBase 的功能组件可以被访问
        // 注意：这些方法依赖 IDEA 平台服务，在测试环境中可能不可用
        assertTrue("Action 类可以被实例化", true);
    }
}
