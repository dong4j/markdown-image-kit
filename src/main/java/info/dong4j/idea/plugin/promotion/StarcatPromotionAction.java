package info.dong4j.idea.plugin.promotion;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

import info.dong4j.idea.plugin.MikBundle;

import org.jetbrains.annotations.NotNull;

/**
 * Help 菜单中的 Starcat 常驻入口。
 * <p>
 * 非 macOS 15+ 用户不会看到此动作，确保推广与目标用户范围严格一致。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2026.08.04
 * @since 2026.2.0
 */
public final class StarcatPromotionAction extends DumbAwareAction {
    public StarcatPromotionAction() {
        super(MikBundle.message("starcat.promotion.action.text"),
              MikBundle.message("starcat.promotion.action.description"),
              null);
    }

    /** 打开包含应用截图与 App Store CTA 的详情对话框。 */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        StarcatPromotion.showDialog(event.getProject());
    }

    /** 根据操作系统条件控制菜单项可见性。 */
    @Override
    public void update(@NotNull AnActionEvent event) {
        boolean eligible = StarcatPromotion.isEligible();
        event.getPresentation().setEnabledAndVisible(eligible);
    }

    /** 菜单可见性判断不依赖 Swing 状态，可安全在后台线程执行。 */
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
