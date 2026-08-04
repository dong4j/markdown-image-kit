package info.dong4j.idea.plugin.promotion;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import info.dong4j.idea.plugin.MikBundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Starcat 推广入口。
 * <p>
 * 推广仅对 macOS 15 及以上系统开放，并统一管理截图卡片、详情对话框和 App Store 跳转，
 * 避免营销逻辑侵入 Markdown Image Kit 的核心功能。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2026.08.04
 * @since 2026.2.0
 */
public final class StarcatPromotion {
    /** 跨 dong4j 插件共享的一次性推广活动标识，避免安装多个插件时重复曝光。 */
    public static final String CAMPAIGN_ID = "dong4j.starcat.promotion.2026.08";
    /** Starcat 的 Mac App Store 深链。 */
    private static final String APP_STORE_URL = "macappstore://itunes.apple.com/app/id6788809803";
    /** Starcat 正式产品落地页。 */
    private static final String WEBSITE_URL = "https://starcat.ink";
    /** 推广支持的最低 macOS 主版本。 */
    private static final int MINIMUM_MACOS_MAJOR_VERSION = 15;
    /** 设置页轮播视口高度，确保截图可读且不会挤占整个设置页。 */
    private static final int SETTINGS_CAROUSEL_HEIGHT = 340;
    /** 详情对话框提供更大的截图视口。 */
    private static final int DIALOG_CAROUSEL_HEIGHT = 440;
    /** 详情对话框卡片宽度。 */
    private static final int DIALOG_CARD_WIDTH = 800;

    private StarcatPromotion() {
    }

    /**
     * 判断当前系统是否允许展示 Starcat 推广。
     *
     * @return 仅 macOS 15 及以上返回 {@code true}
     */
    public static boolean isEligible() {
        return isEligible(SystemInfo.isMac, SystemInfo.OS_VERSION);
    }

    /** 将平台标识与系统版本合并为可独立测试的推广门槛判断。 */
    static boolean isEligible(boolean isMac, @Nullable String osVersion) {
        return isMac && parseMajorVersion(osVersion) >= MINIMUM_MACOS_MAJOR_VERSION;
    }

    /**
     * 创建设置页中的常驻推广卡片。
     * <p>
     * 卡片不会改变任何插件配置，只提供应用截图、简短说明和用户主动触发的下载入口。
     *
     * @return Starcat 推广卡片
     */
    @NotNull
    public static JPanel createSettingsPanel() {
        return createPanel(SETTINGS_CAROUSEL_HEIGHT, true);
    }

    /**
     * 显示包含应用截图的推广详情。
     *
     * @param project 当前项目，可为空
     */
    public static void showDialog(@Nullable Project project) {
        new StarcatPromotionDialog(project).show();
    }

    /**
     * 显示一次性推广通知。
     * <p>
     * 通知本身保持轻量，只有用户点击后才展示带截图的详情对话框。
     *
     * @param project 当前项目
     */
    public static void notify(@NotNull Project project) {
        Notification notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("Markdown Image Kit")
            .createNotification(MikBundle.message("starcat.promotion.notification.title"),
                                MikBundle.message("starcat.promotion.notification.content"),
                                NotificationType.INFORMATION);
        notification.addAction(new NotificationAction(MikBundle.message("starcat.promotion.learn.more")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent event, @NotNull Notification currentNotification) {
                showDialog(project);
                currentNotification.expire();
            }
        });
        notification.notify(project);
    }

    /** 打开 Starcat 的 Mac App Store 下载页。 */
    private static void openAppStore() {
        BrowserUtil.browse(APP_STORE_URL);
    }

    /** 使用系统默认浏览器打开 Starcat 产品落地页。 */
    private static void openWebsite() {
        BrowserUtil.browse(WEBSITE_URL);
    }

    /**
     * 构建推广内容面板。
     *
     * @param carouselHeight   轮播截图视口高度
     * @param includeCtaButton 是否在面板内显示下载按钮
     * @return 推广内容面板
     */
    @NotNull
    private static JPanel createPanel(int carouselHeight, boolean includeCtaButton) {
        JPanel panel = new RoundedCardPanel();
        panel.setLayout(new BorderLayout(0, JBUI.scale(14)));
        panel.setBorder(JBUI.Borders.empty(16));
        // 设置页的其他分组使用居中对齐；保持一致才能让 BoxLayout 将卡片扩展到完整内容宽度，
        // 避免混用 LEFT_ALIGNMENT 后以公共对齐轴计算出大块左侧留白。
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout(JBUI.scale(12), 0));
        header.setOpaque(false);
        JPanel copyPanel = createCopyPanel();
        header.add(copyPanel, BorderLayout.CENTER);

        JComponent badge = createBadge();
        header.add(badge, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        StarcatScreenshotCarousel carousel = new StarcatScreenshotCarousel(carouselHeight);
        panel.add(carousel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(JBUI.scale(12), 0));
        footer.setOpaque(false);
        JBLabel valueProposition = new JBLabel(MikBundle.message("starcat.promotion.value.proposition"));
        valueProposition.setForeground(JBColor.namedColor("Label.infoForeground", JBColor.GRAY));
        footer.add(valueProposition, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, JBUI.scale(8), 0));
        actionPanel.setOpaque(false);
        JButton websiteButton = new JButton(MikBundle.message("starcat.promotion.website"));
        websiteButton.setToolTipText(MikBundle.message("starcat.promotion.website.tooltip"));
        websiteButton.getAccessibleContext().setAccessibleName(websiteButton.getToolTipText());
        websiteButton.addActionListener(event -> openWebsite());
        actionPanel.add(websiteButton);
        if (includeCtaButton) {
            JButton appStoreButton = new JButton(MikBundle.message("starcat.promotion.app.store"));
            appStoreButton.setToolTipText(MikBundle.message("starcat.promotion.app.store.tooltip"));
            appStoreButton.getAccessibleContext().setAccessibleName(appStoreButton.getToolTipText());
            appStoreButton.putClientProperty("JButton.buttonType", "default");
            appStoreButton.addActionListener(event -> openAppStore());
            actionPanel.add(appStoreButton);
        }
        footer.add(actionPanel, BorderLayout.EAST);
        panel.add(footer, BorderLayout.SOUTH);

        Dimension preferredSize = panel.getPreferredSize();
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredSize.height));
        return panel;
    }

    /** 创建标题与简短说明，避免营销文案挤占截图空间。 */
    @NotNull
    private static JPanel createCopyPanel() {
        JPanel copyPanel = new JPanel();
        copyPanel.setOpaque(false);
        copyPanel.setLayout(new BoxLayout(copyPanel, BoxLayout.Y_AXIS));

        JBLabel title = new JBLabel(MikBundle.message("starcat.promotion.title"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 2.0f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        copyPanel.add(title);
        copyPanel.add(Box.createVerticalStrut(JBUI.scale(4)));

        JBLabel description = new JBLabel(MikBundle.message("starcat.promotion.description"));
        description.setForeground(JBColor.namedColor("Label.infoForeground", JBColor.GRAY));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        copyPanel.add(description);
        return copyPanel;
    }

    /** 创建区别于按钮的胶囊标签，突出系统要求但不抢占主操作视觉层级。 */
    @NotNull
    private static JComponent createBadge() {
        JPanel badgePanel = new RoundedBadgePanel();
        badgePanel.setLayout(new BorderLayout());
        badgePanel.setBorder(JBUI.Borders.empty(4, 10));
        badgePanel.setToolTipText(MikBundle.message("starcat.promotion.badge.tooltip"));

        JBLabel badgeLabel = new JBLabel(MikBundle.message("starcat.promotion.badge"));
        badgeLabel.setFont(badgeLabel.getFont().deriveFont(Font.PLAIN, badgeLabel.getFont().getSize2D() - 1.0f));
        badgeLabel.setForeground(RoundedBadgePanel.BADGE_FOREGROUND);
        badgePanel.add(badgeLabel, BorderLayout.CENTER);
        badgePanel.getAccessibleContext().setAccessibleName(badgeLabel.getText());
        return badgePanel;
    }

    /**
     * 提取系统版本中的主版本号，对异常格式安全降级为 0。
     *
     * @param version 系统版本字符串
     * @return 主版本号
     */
    static int parseMajorVersion(@Nullable String version) {
        if (version == null || version.isBlank()) {
            return 0;
        }
        int separator = version.indexOf('.');
        String major = separator >= 0 ? version.substring(0, separator) : version;
        try {
            return Integer.parseInt(major.trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /** 用户主动打开的 Starcat 详情对话框。 */
    private static final class StarcatPromotionDialog extends DialogWrapper {
        private StarcatPromotionDialog(@Nullable Project project) {
            super(project);
            setTitle(MikBundle.message("starcat.promotion.dialog.title"));
            setOKButtonText(MikBundle.message("starcat.promotion.app.store"));
            setCancelButtonText(MikBundle.message("starcat.promotion.not.now"));
            setResizable(false);
            init();
        }

        /** 创建带真实应用截图的对话框内容。 */
        @Override
        protected @Nullable JComponent createCenterPanel() {
            JPanel panel = createPanel(DIALOG_CAROUSEL_HEIGHT, false);
            panel.setPreferredSize(new Dimension(JBUI.scale(DIALOG_CARD_WIDTH), panel.getPreferredSize().height));
            return panel;
        }

        /** 仅在用户主动点击主按钮后跳转 App Store。 */
        @Override
        protected void doOKAction() {
            openAppStore();
            super.doOKAction();
        }
    }

    /** 使用主题感知颜色绘制轻量圆角卡片，避免生硬的矩形黑边。 */
    private static final class RoundedCardPanel extends JPanel {
        private static final int ARC = 14;
        private static final JBColor CARD_BACKGROUND = new JBColor(new Color(250, 250, 251), new Color(43, 45, 48));
        private static final JBColor CARD_BORDER = JBColor.namedColor("Borders.color", JBColor.border());

        private RoundedCardPanel() {
            setOpaque(false);
        }

        /** 先绘制卡片底色，再交由 Swing 绘制子组件。 */
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            try {
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setColor(CARD_BACKGROUND);
                int inset = JBUI.scale(1);
                int arc = JBUI.scale(ARC);
                graphics2D.fill(new RoundRectangle2D.Float(
                    inset,
                    inset,
                    Math.max(0, getWidth() - inset * 2),
                    Math.max(0, getHeight() - inset * 2),
                    arc,
                    arc
                ));
            } finally {
                graphics2D.dispose();
            }
            super.paintComponent(graphics);
        }

        /** 绘制一像素主题边框，深浅主题下都保持清晰但不过度抢眼。 */
        @Override
        protected void paintBorder(Graphics graphics) {
            super.paintBorder(graphics);
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            try {
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setColor(CARD_BORDER);
                graphics2D.setStroke(new BasicStroke(JBUI.scale(1)));
                int inset = JBUI.scale(1);
                int arc = JBUI.scale(ARC);
                graphics2D.draw(new RoundRectangle2D.Float(
                    inset,
                    inset,
                    Math.max(0, getWidth() - inset * 2),
                    Math.max(0, getHeight() - inset * 2),
                    arc,
                    arc
                ));
            } finally {
                graphics2D.dispose();
            }
        }
    }

    /** 使用柔和强调色绘制 macOS 版本胶囊标签。 */
    private static final class RoundedBadgePanel extends JPanel {
        private static final JBColor BADGE_BACKGROUND = new JBColor(new Color(236, 244, 255), new Color(48, 58, 74));
        private static final JBColor BADGE_FOREGROUND = new JBColor(new Color(43, 91, 148), new Color(174, 203, 245));

        private RoundedBadgePanel() {
            setOpaque(false);
        }

        /** 以组件高度作为圆角直径，形成稳定的胶囊轮廓。 */
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D graphics2D = (Graphics2D) graphics.create();
            try {
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setColor(BADGE_BACKGROUND);
                graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            } finally {
                graphics2D.dispose();
            }
            super.paintComponent(graphics);
        }
    }
}
