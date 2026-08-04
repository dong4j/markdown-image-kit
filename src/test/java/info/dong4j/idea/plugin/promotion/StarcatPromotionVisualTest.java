package info.dong4j.idea.plugin.promotion;

import org.junit.jupiter.api.Test;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Starcat 推广卡片的响应式绘制冒烟测试。
 * <p>
 * 测试使用设置页常见窄宽度绘制真实组件，避免轮播改动再次引入截图横向溢出；
 * 调试时可通过 {@code -Dstarcat.preview.file=/tmp/starcat.png} 输出人工验收图片。
 */
class StarcatPromotionVisualTest {
    /** 设置页内容区常见宽度，用于覆盖用户截图中出现问题的窄窗口场景。 */
    private static final int SETTINGS_WIDTH = 720;
    /** 宽设置窗口用于复现 BoxLayout 对齐轴造成的左侧留白。 */
    private static final int WIDE_SETTINGS_WIDTH = 1600;

    @Test
    void shouldRenderCarouselInsideSettingsWidth() throws Exception {
        AtomicReference<JPanel> panelReference = new AtomicReference<>();
        AtomicReference<BufferedImage> previewReference = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = StarcatPromotion.createSettingsPanel();
            Dimension preferredSize = panel.getPreferredSize();
            panel.setSize(SETTINGS_WIDTH, preferredSize.height);
            layoutRecursively(panel);

            BufferedImage preview = new BufferedImage(
                SETTINGS_WIDTH,
                preferredSize.height,
                BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D graphics = preview.createGraphics();
            try {
                panel.paint(graphics);
            } finally {
                graphics.dispose();
            }
            panelReference.set(panel);
            previewReference.set(preview);
        });

        JPanel panel = panelReference.get();
        assertThat(panel.getWidth()).isEqualTo(SETTINGS_WIDTH);
        assertThat(maximumRightEdge(panel)).isLessThanOrEqualTo(SETTINGS_WIDTH);

        String previewPath = System.getProperty("starcat.preview.file");
        if (previewPath != null && !previewPath.isBlank()) {
            ImageIO.write(previewReference.get(), "png", new File(previewPath));
        }
    }

    @Test
    void shouldFillWideSettingsColumnWithoutLeftGap() throws Exception {
        AtomicReference<JPanel> promotionReference = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            JPanel settingsColumn = new JPanel();
            settingsColumn.setLayout(new BoxLayout(settingsColumn, BoxLayout.Y_AXIS));

            // 模拟设置页中原有的居中对齐宽分组；此前推广卡片使用左对齐时会被公共对齐轴推向右侧。
            JPanel existingSection = new JPanel();
            existingSection.setAlignmentX(Component.CENTER_ALIGNMENT);
            existingSection.setPreferredSize(new Dimension(WIDE_SETTINGS_WIDTH, 80));
            existingSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            settingsColumn.add(existingSection);

            JPanel promotion = StarcatPromotion.createSettingsPanel();
            settingsColumn.add(promotion);
            int settingsHeight = existingSection.getPreferredSize().height + promotion.getPreferredSize().height;
            settingsColumn.setSize(WIDE_SETTINGS_WIDTH, settingsHeight);
            layoutRecursively(settingsColumn);
            promotionReference.set(promotion);
        });

        JPanel promotion = promotionReference.get();
        assertThat(promotion.getX()).isLessThanOrEqualTo(1);
        assertThat(promotion.getWidth()).isGreaterThanOrEqualTo(WIDE_SETTINGS_WIDTH - 2);
    }

    /** 递归执行布局，确保未显示在窗口中的测试组件也获得真实子组件尺寸。 */
    private static void layoutRecursively(Container container) {
        container.doLayout();
        for (Component component : container.getComponents()) {
            if (component instanceof Container childContainer) {
                layoutRecursively(childContainer);
            }
        }
    }

    /** 计算所有直接子组件的最右边界，验证卡片内部没有横向越界。 */
    private static int maximumRightEdge(Container container) {
        int maximum = 0;
        for (Component component : container.getComponents()) {
            maximum = Math.max(maximum, component.getX() + component.getWidth());
        }
        return maximum;
    }
}
