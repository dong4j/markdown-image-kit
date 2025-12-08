package info.dong4j.idea.plugin.util;

import com.intellij.util.ui.UIUtil;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

/**
 * Swing 辅助工具类
 * <p>
 * 提供对 {@link javax.swing.border.TitledBorder} 的统一配置方法, 自动设置标题字体为系统默认 Label 字体, 并使用 UI 主题的前景色作为标题颜色.
 * 通过 {@link #configureTitledBorder(javax.swing.border.TitledBorder)} 可以直接对已有边框进行配置,
 * 通过 {@link #configureTitledBorder(String)} 可以创建并配置一个新的标题边框.
 *
 * @author zeka.stack.team
 * @version 1.0.0
 * @email "mailto:zeka.stack@gmail.com"
 * @date 2025.12.08
 * @since 1.0.0
 */
public class SwingUtils {

    /**
     * 配置 TitledBorder 的字体和颜色
     * <p>
     * 显式设置字体和颜色，确保在 2025 版本中正常显示。
     * 使用 UIUtil 获取主题感知的文本颜色，自动适配浅色和深色主题。
     *
     * @param titledBorder 要配置的 TitledBorder
     */
    public static void configureTitledBorder(@NotNull TitledBorder titledBorder) {
        titledBorder.setTitleFont(UIManager.getFont("Label.font"));
        Color titleColor = UIUtil.getLabelForeground();
        titledBorder.setTitleColor(titleColor);
    }

    public static TitledBorder configureTitledBorder(@NotNull String title) {
        final TitledBorder titledBorder = BorderFactory.createTitledBorder(title);
        configureTitledBorder(titledBorder);
        return titledBorder;
    }
}
