package info.dong4j.idea.plugin.promotion;

import com.intellij.ide.util.RunOnceUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.util.concurrency.AppExecutorUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import kotlin.Unit;
import kotlin.coroutines.Continuation;

/**
 * Starcat 一次性启动推广活动。
 * <p>
 * 延迟展示轻量通知，避免与 IDE 启动过程及插件核心操作竞争；活动 ID 在 dong4j 插件间共享，
 * 即使用户同时安装多个插件也只会看到一次。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2026.08.04
 * @since 2026.2.0
 */
public final class StarcatPromotionStartupActivity implements ProjectActivity {
    /** 防止同一扩展实例被多个项目重复调度。 */
    private final AtomicBoolean scheduled = new AtomicBoolean(false);

    /** 在满足系统条件时延迟调度一次推广通知。 */
    @Override
    public @Nullable Object execute(@NotNull Project project,
                                    @NotNull Continuation<? super Unit> continuation) {
        if (project.isDefault()
            || ApplicationManager.getApplication().isUnitTestMode()
            || !StarcatPromotion.isEligible()
            || !scheduled.compareAndSet(false, true)) {
            return Unit.INSTANCE;
        }

        AppExecutorUtil.getAppScheduledExecutorService().schedule(() -> {
            if (project.isDisposed()) {
                return;
            }
            ApplicationManager.getApplication().invokeLater(() -> {
                if (!project.isDisposed()) {
                    RunOnceUtil.runOnceForApp(StarcatPromotion.CAMPAIGN_ID,
                                              () -> StarcatPromotion.notify(project));
                }
            });
        }, 15L, TimeUnit.SECONDS);
        return Unit.INSTANCE;
    }
}
