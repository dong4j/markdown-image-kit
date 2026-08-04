package info.dong4j.idea.plugin.promotion;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import info.dong4j.idea.plugin.MikBundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Starcat 应用截图轮播组件。
 * <p>
 * 组件使用自适应绘制而非固定尺寸 {@code ImageIcon}，确保设置页缩放时截图始终完整可见；
 * 同时提供自动轮播、手动切换、键盘操作和可访问说明。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2026.08.04
 * @since 2026.2.0
 */
final class StarcatScreenshotCarousel extends JPanel {
    /** 自动切换间隔，既保证曝光又避免频繁打断阅读。 */
    private static final int AUTO_ADVANCE_DELAY_MILLIS = 5000;
    /** 截图切换的交叉淡入淡出时长。 */
    private static final int FADE_DURATION_MILLIS = 280;
    /** 动画帧间隔约为 60 FPS，兼顾流畅度与 EDT 开销。 */
    private static final int FADE_FRAME_DELAY_MILLIS = 16;
    /** 截图资源顺序与文案顺序保持一致。 */
    private static final String[] SCREENSHOT_PATHS = {
        "/images/starcat/1.png",
        "/images/starcat/2.png",
        "/images/starcat/3.png",
        "/images/starcat/4.png"
    };
    private static final String[] CAPTION_KEYS = {
        "starcat.promotion.slide.1",
        "starcat.promotion.slide.2",
        "starcat.promotion.slide.3",
        "starcat.promotion.slide.4"
    };
    private static final JBColor ACTIVE_INDICATOR = JBColor.namedColor("Link.activeForeground", JBColor.BLUE);
    private static final JBColor INACTIVE_INDICATOR = JBColor.namedColor("Label.disabledForeground", JBColor.GRAY);

    /** 保持与资源数量一致，资源缺失时对应位置为 {@code null}。 */
    private final List<BufferedImage> screenshots = new ArrayList<>(SCREENSHOT_PATHS.length);
    private final List<JButton> indicators = new ArrayList<>(SCREENSHOT_PATHS.length);
    private final ScreenshotViewport viewport;
    private final JBLabel captionLabel = new JBLabel();
    private final Timer autoAdvanceTimer;
    private int currentIndex;

    /**
     * 创建截图轮播。
     *
     * @param viewportHeight 截图视口逻辑高度
     */
    StarcatScreenshotCarousel(int viewportHeight) {
        super(new BorderLayout(0, JBUI.scale(8)));
        setOpaque(false);
        setFocusable(true);

        loadScreenshots();
        viewport = new ScreenshotViewport(JBUI.scale(viewportHeight));
        add(viewport, BorderLayout.CENTER);
        add(createControls(), BorderLayout.SOUTH);
        installKeyboardNavigation();

        autoAdvanceTimer = new Timer(AUTO_ADVANCE_DELAY_MILLIS, event -> {
            // 鼠标停留时保持当前画面，避免用户阅读截图时内容突然切换。
            if (isShowing() && getMousePosition() == null) {
                showSlide(currentIndex + 1);
            }
        });
        autoAdvanceTimer.setInitialDelay(AUTO_ADVANCE_DELAY_MILLIS);
        addHierarchyListener(event -> {
            if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) {
                    autoAdvanceTimer.start();
                } else {
                    autoAdvanceTimer.stop();
                }
            }
        });

        showSlide(0);
    }

    /** 创建包含前后切换、标题和分页圆点的紧凑控制栏。 */
    @NotNull
    private JPanel createControls() {
        JPanel controls = new JPanel(new BorderLayout(JBUI.scale(10), 0));
        controls.setOpaque(false);

        JButton previousButton = createNavigationButton(
            "\u2039",
            MikBundle.message("starcat.promotion.carousel.previous")
                                                       );
        previousButton.addActionListener(event -> showSlide(currentIndex - 1));
        controls.add(previousButton, BorderLayout.WEST);

        JPanel statusPanel = new JPanel();
        statusPanel.setOpaque(false);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));

        captionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        captionLabel.setAlignmentX(CENTER_ALIGNMENT);
        captionLabel.setFont(captionLabel.getFont().deriveFont(Font.PLAIN, captionLabel.getFont().getSize2D() - 0.5f));
        statusPanel.add(captionLabel);

        JPanel indicatorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, JBUI.scale(3), 0));
        indicatorPanel.setOpaque(false);
        Dimension indicatorSize = new Dimension(JBUI.scale(16), JBUI.scale(16));
        for (int index = 0; index < SCREENSHOT_PATHS.length; index++) {
            int slideIndex = index;
            JButton indicator = new JButton("○");
            indicator.setBorder(JBUI.Borders.empty(0, 2));
            indicator.setContentAreaFilled(false);
            indicator.setFocusPainted(false);
            // JetBrains 按钮 UI 默认带较大的最小宽度，显式限制尺寸才能让分页圆点紧凑聚拢。
            indicator.setMinimumSize(indicatorSize);
            indicator.setPreferredSize(indicatorSize);
            indicator.setMaximumSize(indicatorSize);
            indicator.setToolTipText(MikBundle.message("starcat.promotion.carousel.go.to", index + 1));
            indicator.getAccessibleContext().setAccessibleName(indicator.getToolTipText());
            indicator.addActionListener(event -> showSlide(slideIndex));
            indicators.add(indicator);
            indicatorPanel.add(indicator);
        }
        statusPanel.add(indicatorPanel);
        controls.add(statusPanel, BorderLayout.CENTER);

        JButton nextButton = createNavigationButton(
            "\u203A",
            MikBundle.message("starcat.promotion.carousel.next")
                                                   );
        nextButton.addActionListener(event -> showSlide(currentIndex + 1));
        controls.add(nextButton, BorderLayout.EAST);
        return controls;
    }

    /** 创建在任意 IDE 主题和版本下都清晰可见的轻量轮播按钮。 */
    @NotNull
    private static JButton createNavigationButton(@NotNull String arrow, @NotNull String tooltip) {
        JButton button = new JButton(arrow);
        button.setToolTipText(tooltip);
        button.getAccessibleContext().setAccessibleName(tooltip);
        button.setFont(button.getFont().deriveFont(Font.PLAIN, button.getFont().getSize2D() + 8.0f));
        button.setBorder(JBUI.Borders.empty(0, 10));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setFocusable(true);
        return button;
    }

    /** 注册左右方向键，保证不依赖鼠标也能浏览全部截图。 */
    private void installKeyboardNavigation() {
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "starcat.previous");
        getActionMap().put("starcat.previous", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showSlide(currentIndex - 1);
            }
        });
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "starcat.next");
        getActionMap().put("starcat.next", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showSlide(currentIndex + 1);
            }
        });
    }

    /** 切换截图并同步标题、分页状态与无障碍文本。 */
    private void showSlide(int requestedIndex) {
        currentIndex = Math.floorMod(requestedIndex, SCREENSHOT_PATHS.length);
        String caption = MikBundle.message(CAPTION_KEYS[currentIndex]);
        captionLabel.setText(MikBundle.message(
            "starcat.promotion.carousel.caption",
            currentIndex + 1,
            SCREENSHOT_PATHS.length,
            caption
                                              ));
        viewport.setScreenshot(screenshots.get(currentIndex), caption);

        for (int index = 0; index < indicators.size(); index++) {
            JButton indicator = indicators.get(index);
            boolean selected = index == currentIndex;
            indicator.setText(selected ? "●" : "○");
            indicator.setForeground(selected ? ACTIVE_INDICATOR : INACTIVE_INDICATOR);
        }
    }

    /** 从插件资源读取压缩后的截图；单张资源异常不会影响其他页面。 */
    private void loadScreenshots() {
        for (String resourcePath : SCREENSHOT_PATHS) {
            screenshots.add(readScreenshot(resourcePath));
        }
    }

    /** 读取单张截图。 */
    @Nullable
    private static BufferedImage readScreenshot(@NotNull String resourcePath) {
        try (InputStream inputStream = StarcatScreenshotCarousel.class.getResourceAsStream(resourcePath)) {
            return inputStream == null ? null : ImageIO.read(inputStream);
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * 自适应截图画布。
     * <p>
     * 每次绘制都按可用宽高计算等比缩放，保证窄设置页和高 DPI 屏幕均不会裁切或横向溢出。
     */
    private static final class ScreenshotViewport extends JPanel {
        private static final int CORNER_RADIUS = 12;
        private static final JBColor VIEWPORT_BACKGROUND = new JBColor(new Color(246, 247, 249), new Color(25, 26, 28));
        private static final JBColor VIEWPORT_BORDER = JBColor.namedColor("Borders.color", JBColor.border());
        private final Timer fadeTimer;
        private BufferedImage previousScreenshot;
        private BufferedImage screenshot;
        private String accessibleName = "";
        private float fadeProgress = 1.0f;
        private long fadeStartedAtNanos;

        private ScreenshotViewport(int viewportHeight) {
            setOpaque(false);
            setPreferredSize(new Dimension((int) Math.round(viewportHeight * 1.68), viewportHeight));
            setMinimumSize(new Dimension(JBUI.scale(320), JBUI.scale(190)));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, viewportHeight));

            fadeTimer = new Timer(FADE_FRAME_DELAY_MILLIS, event -> updateFadeProgress());
            fadeTimer.setCoalesce(true);
        }

        /** 更新当前截图，并在已有画面时启动交叉淡入淡出。 */
        private void setScreenshot(@Nullable BufferedImage screenshot, @NotNull String name) {
            if (this.screenshot == null || this.screenshot == screenshot) {
                fadeTimer.stop();
                previousScreenshot = null;
                fadeProgress = 1.0f;
            } else {
                previousScreenshot = this.screenshot;
                fadeProgress = 0.0f;
                fadeStartedAtNanos = System.nanoTime();
                fadeTimer.restart();
            }
            this.screenshot = screenshot;
            accessibleName = name;
            getAccessibleContext().setAccessibleName(name);
            repaint();
        }

        /** 依据真实经过时间推进动画，避免 EDT 短暂繁忙时动画速度失真。 */
        private void updateFadeProgress() {
            long elapsedNanos = System.nanoTime() - fadeStartedAtNanos;
            fadeProgress = Math.min(1.0f, elapsedNanos / (FADE_DURATION_MILLIS * 1_000_000.0f));
            if (fadeProgress >= 1.0f) {
                fadeTimer.stop();
                previousScreenshot = null;
            }
            repaint();
        }

        /** 按组件实时尺寸完整绘制截图，并使用圆角裁剪避免生硬边缘。 */
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            try {
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int inset = JBUI.scale(1);
                int width = Math.max(0, getWidth() - inset * 2);
                int height = Math.max(0, getHeight() - inset * 2);
                int arc = JBUI.scale(CORNER_RADIUS);
                RoundRectangle2D frame = new RoundRectangle2D.Float(inset, inset, width, height, arc, arc);

                graphics2D.setColor(VIEWPORT_BACKGROUND);
                graphics2D.fill(frame);
                graphics2D.clip(frame);

                if (width > 0 && height > 0 && (screenshot != null || previousScreenshot != null)) {
                    if (previousScreenshot != null && fadeProgress < 1.0f) {
                        drawFittedImage(graphics2D, previousScreenshot, width, height, 1.0f - fadeProgress);
                    }
                    if (screenshot != null) {
                        float opacity = previousScreenshot == null ? 1.0f : fadeProgress;
                        drawFittedImage(graphics2D, screenshot, width, height, opacity);
                    }
                } else {
                    graphics2D.setColor(INACTIVE_INDICATOR);
                    graphics2D.drawString(accessibleName, JBUI.scale(16), getHeight() / 2);
                }

                graphics2D.setClip(null);
                graphics2D.setColor(VIEWPORT_BORDER);
                graphics2D.setStroke(new BasicStroke(JBUI.scale(1)));
                graphics2D.draw(frame);
            } finally {
                graphics2D.dispose();
            }
        }

        /** 将单张截图等比居中绘制，并应用当前交叉淡入淡出透明度。 */
        private void drawFittedImage(@NotNull Graphics2D graphics2D,
                                     @NotNull BufferedImage image,
                                     int availableWidth,
                                     int availableHeight,
                                     float opacity) {
            double scale = Math.min((double) availableWidth / image.getWidth(),
                                    (double) availableHeight / image.getHeight());
            int drawWidth = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int drawHeight = Math.max(1, (int) Math.round(image.getHeight() * scale));
            int drawX = (getWidth() - drawWidth) / 2;
            int drawY = (getHeight() - drawHeight) / 2;
            Composite originalComposite = graphics2D.getComposite();
            graphics2D.setComposite(AlphaComposite.SrcOver.derive(Math.max(0.0f, Math.min(1.0f, opacity))));
            graphics2D.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
            graphics2D.setComposite(originalComposite);
        }
    }
}
