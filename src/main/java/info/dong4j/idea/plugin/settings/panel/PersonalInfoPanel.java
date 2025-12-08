package info.dong4j.idea.plugin.settings.panel;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import info.dong4j.idea.plugin.util.SwingUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import lombok.Getter;

/**
 * 个人信息面板
 * <p>
 * 可复用的个人信息展示组件，用于在插件设置页面展示作者信息。
 * 包含头像、姓名、简介、社交媒体链接等信息。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.12.07
 * @since 2.2.0
 */
public class PersonalInfoPanel {
    /** 面板内容 */
    @Getter
    private JPanel content;

    /**
     * 个人信息配置类
     */
    public static class PersonalInfo {
        /** 姓名 */
        private final String name;
        /** 职位/角色 */
        private final String role;
        /** 个人简介 */
        private final String bio;
        /** 头像图标 */
        private final ImageIcon avatar;
        /** 悬停时的头像图标 */
        private final ImageIcon hoverAvatar;
        /** GitHub 链接 */
        private final String githubUrl;
        /** 个人网站链接 */
        private final String websiteUrl;
        /** 邮箱 */
        private final String email;
        /** Twitter/X 链接 */
        private final String twitterUrl;
        /** 博客链接 */
        private final String blogUrl;
        /** NPX Card 链接 */
        private final String npxCardUrl;
        /** Chat 链接 */
        private final String chatUrl;
        /** 命令行命令 */
        private final String command;
        /** 底部提示的 GitHub 链接 */
        private final String footerGitHubUrl;

        private PersonalInfo(Builder builder) {
            this.name = builder.name;
            this.role = builder.role;
            this.bio = builder.bio;
            this.avatar = builder.avatar;
            this.hoverAvatar = builder.hoverAvatar;
            this.githubUrl = builder.githubUrl;
            this.websiteUrl = builder.websiteUrl;
            this.email = builder.email;
            this.twitterUrl = builder.twitterUrl;
            this.blogUrl = builder.blogUrl;
            this.npxCardUrl = builder.npxCardUrl;
            this.chatUrl = builder.chatUrl;
            this.command = builder.command;
            this.footerGitHubUrl = builder.footerGitHubUrl;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String name;
            private String role;
            private String bio;
            private ImageIcon avatar;
            private ImageIcon hoverAvatar;
            private String githubUrl;
            private String websiteUrl;
            private String email;
            private String twitterUrl;
            private String blogUrl;
            private String npxCardUrl;
            private String chatUrl;
            private String command;
            private String footerGitHubUrl;

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder role(String role) {
                this.role = role;
                return this;
            }

            public Builder bio(String bio) {
                this.bio = bio;
                return this;
            }

            public Builder avatar(ImageIcon avatar) {
                this.avatar = avatar;
                return this;
            }

            public Builder hoverAvatar(ImageIcon hoverAvatar) {
                this.hoverAvatar = hoverAvatar;
                return this;
            }

            public Builder githubUrl(String githubUrl) {
                this.githubUrl = githubUrl;
                return this;
            }

            public Builder websiteUrl(String websiteUrl) {
                this.websiteUrl = websiteUrl;
                return this;
            }

            public Builder email(String email) {
                this.email = email;
                return this;
            }

            public Builder twitterUrl(String twitterUrl) {
                this.twitterUrl = twitterUrl;
                return this;
            }

            public Builder blogUrl(String blogUrl) {
                this.blogUrl = blogUrl;
                return this;
            }

            public Builder npxCardUrl(String npxCardUrl) {
                this.npxCardUrl = npxCardUrl;
                return this;
            }

            public Builder chatUrl(String chatUrl) {
                this.chatUrl = chatUrl;
                return this;
            }

            public Builder command(String command) {
                this.command = command;
                return this;
            }

            public Builder footerGitHubUrl(String footerGitHubUrl) {
                this.footerGitHubUrl = footerGitHubUrl;
                return this;
            }

            public PersonalInfo build() {
                return new PersonalInfo(this);
            }
        }
    }

    /**
     * 构造函数
     *
     * @param info 个人信息配置
     */
    public PersonalInfoPanel(@NotNull PersonalInfo info) {
        createPersonalInfoPanel(info);
    }

    /**
     * 创建个人信息面板
     *
     * @param info 个人信息配置
     */
    private void createPersonalInfoPanel(@NotNull PersonalInfo info) {
        content = new JPanel();
        content.setLayout(new BorderLayout());

        // 创建可折叠的标题栏
        JPanel titlePanel = createCollapsibleTitle("👨‍💻 About Me");

        // 主内容面板（居中布局）
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        mainPanel.setBorder(JBUI.Borders.empty(15));

        // 默认折叠：隐藏内容面板
        mainPanel.setVisible(false);

        // 使用包装面板确保内容居中
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.add(mainPanel, BorderLayout.NORTH);
        contentWrapper.setOpaque(false);

        // 将标题栏和内容面板添加到主面板
        content.add(titlePanel, BorderLayout.NORTH);
        content.add(contentWrapper, BorderLayout.CENTER);

        // 存储内容面板的引用，以便在标题栏点击时切换显示
        final JPanel contentPanel = mainPanel;

        // 为标题栏添加点击事件
        final String titleText = "👨‍💻 About Me";
        titlePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean isVisible = contentPanel.isVisible();
                contentPanel.setVisible(!isVisible);
                updateCollapsibleTitle(titlePanel, titleText, !isVisible);
                content.revalidate();
                content.repaint();
            }
        });

        // 头像（圆形，居中）
        if (info.avatar != null) {
            JLabel avatarLabel = createCircularAvatarLabel(info.avatar, info.hoverAvatar);

            // 使用容器确保头像完全居中
            JPanel avatarContainer = new JPanel(new BorderLayout());
            avatarContainer.setOpaque(false);
            avatarContainer.add(avatarLabel, BorderLayout.CENTER);
            avatarContainer.setAlignmentX(JPanel.CENTER_ALIGNMENT);
            mainPanel.add(avatarContainer);
            mainPanel.add(Box.createVerticalStrut(15));
        }

        // 姓名（大字体，居中）
        if (info.name != null) {
            JBLabel nameLabel = new JBLabel(info.name, SwingConstants.CENTER);
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 24f));
            nameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            mainPanel.add(nameLabel);
            mainPanel.add(Box.createVerticalStrut(8));
        }

        // 角色（灰色，居中）
        if (info.role != null) {
            JBLabel roleLabel = new JBLabel(info.role, SwingConstants.CENTER);
            roleLabel.setFont(roleLabel.getFont().deriveFont(Font.PLAIN, 14f));
            roleLabel.setForeground(UIUtil.getLabelDisabledForeground());
            roleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            mainPanel.add(roleLabel);
            mainPanel.add(Box.createVerticalStrut(15));
        }

        // 简介（居中，多行）
        if (info.bio != null) {
            JBLabel bioLabel = new JBLabel("<html><div style='text-align: center; max-width: 500px;'>" +
                                           info.bio + "</div></html>", SwingConstants.CENTER);
            bioLabel.setFont(bioLabel.getFont().deriveFont(Font.PLAIN, 13f));
            bioLabel.setForeground(UIUtil.getLabelForeground());
            bioLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            mainPanel.add(bioLabel);
            mainPanel.add(Box.createVerticalStrut(15));
        }

        // 命令行代码块
        if (info.command != null) {
            JPanel codeBlockPanel = createCodeBlockPanel(info.command);
            codeBlockPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
            mainPanel.add(codeBlockPanel);
            mainPanel.add(Box.createVerticalStrut(20));
        }

        // 社交媒体链接（居中，图标形式）
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        linksPanel.setOpaque(false);
        linksPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);

        if (info.websiteUrl != null) {
            linksPanel.add(createIconLink(loadSvgIcon("/icons/personal/home.svg"), "Home", info.websiteUrl));
        }
        if (info.githubUrl != null) {
            linksPanel.add(createIconLink(loadSvgIcon("/icons/personal/github.svg"), "GitHub", info.githubUrl));
        }
        if (info.blogUrl != null) {
            linksPanel.add(createIconLink(loadSvgIcon("/icons/personal/blog.svg"), "Blog", info.blogUrl));
        }
        if (info.npxCardUrl != null) {
            linksPanel.add(createIconLink(loadSvgIcon("/icons/personal/card.svg"), "Card", info.npxCardUrl));
        }
        if (info.chatUrl != null) {
            linksPanel.add(createIconLink(loadSvgIcon("/icons/personal/chat.svg"), "Chat", info.chatUrl));
        }
        if (info.email != null) {
            linksPanel.add(createIconLink(loadSvgIcon("/icons/personal/email.svg"), "Email", "mailto:" + info.email));
        }
        if (info.twitterUrl != null) {
            // Twitter 图标暂时使用 GitHub 图标作为占位符，或者可以添加 twitter.svg
            linksPanel.add(createIconLink(loadSvgIcon("/icons/personal/github.svg"), "Twitter", info.twitterUrl));
        }

        mainPanel.add(linksPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // 底部提示
        if (info.footerGitHubUrl != null) {
            JBLabel footerLabel = new JBLabel(
                "<html><div style='text-align: center; color: #999; font-size: 11px;'>" +
                "If you find this plugin helpful, please give it a ⭐ on " +
                "<a href='" + info.footerGitHubUrl + "'>GitHub</a>" +
                "</div></html>",
                SwingConstants.CENTER
            );
            footerLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            footerLabel.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

            // 添加鼠标监听器来处理链接点击
            footerLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    BrowserUtil.browse(info.footerGitHubUrl);
                }
            });

            mainPanel.add(footerLabel);
        }
    }

    /**
     * 创建可折叠的标题栏
     *
     * @param title 标题文本
     * @return 标题栏面板
     */
    private JPanel createCollapsibleTitle(@NotNull String title) {
        JPanel titlePanel = new JPanel(new BorderLayout());
        // 默认折叠状态，使用右箭头
        TitledBorder titledBorder = BorderFactory.createTitledBorder("▶ " + title);
        SwingUtils.configureTitledBorder(titledBorder);
        titlePanel.setBorder(BorderFactory.createCompoundBorder(
            titledBorder,
            JBUI.Borders.empty(5)
                                                               ));
        titlePanel.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        titlePanel.setOpaque(true);
        titlePanel.setBackground(UIUtil.getPanelBackground());
        return titlePanel;
    }

    /**
     * 更新可折叠标题栏的箭头图标
     *
     * @param titlePanel 标题栏面板
     * @param title      标题文本
     * @param expanded   是否展开
     */
    private void updateCollapsibleTitle(@NotNull JPanel titlePanel, @NotNull String title, boolean expanded) {
        String arrow = expanded ? "▼ " : "▶ ";
        TitledBorder titledBorder = BorderFactory.createTitledBorder(arrow + title);
        SwingUtils.configureTitledBorder(titledBorder);
        titlePanel.setBorder(BorderFactory.createCompoundBorder(
            titledBorder,
            JBUI.Borders.empty(5)
                                                               ));
    }

    /**
     * 创建头像标签（直接使用原始图片，不进行缩放）
     * 支持鼠标悬停时切换图片
     *
     * @param icon      原始图标（已经是 120x120 圆形）
     * @param hoverIcon 悬停时的图标（可选）
     * @return 头像标签
     */
    private JLabel createCircularAvatarLabel(@NotNull ImageIcon icon, ImageIcon hoverIcon) {
        // 直接使用原始图标，不进行任何处理
        JLabel avatarLabel = new JLabel(icon);
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setVerticalAlignment(SwingConstants.CENTER);
        avatarLabel.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        // 如果提供了悬停图标，添加鼠标事件监听器
        if (hoverIcon != null) {
            avatarLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    avatarLabel.setIcon(hoverIcon);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    avatarLabel.setIcon(icon);
                }
            });
        }

        return avatarLabel;
    }

    /**
     * 创建命令行代码块面板
     *
     * @param command 命令文本
     * @return 代码块面板
     */
    private JPanel createCodeBlockPanel(@NotNull String command) {
        JPanel codePanel = new JPanel(new BorderLayout(10, 0));
        codePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(JBColor.border(), 1),
            JBUI.Borders.empty(10, 15)
                                                              ));
        codePanel.setBackground(UIUtil.getPanelBackground());
        codePanel.setMaximumSize(new Dimension(300, 50));

        // 命令文本
        JBLabel codeLabel = new JBLabel(command);
        codeLabel.setFont(new Font("Consolas", Font.PLAIN, 13));
        codeLabel.setForeground(UIUtil.getLabelForeground());
        codePanel.add(codeLabel, BorderLayout.CENTER);

        // 复制按钮
        JButton copyButton = new JButton("Copy");
        copyButton.setFont(copyButton.getFont().deriveFont(Font.PLAIN, 11f));
        copyButton.setPreferredSize(new Dimension(60, 25));
        copyButton.addActionListener(e -> {
            java.awt.datatransfer.StringSelection selection =
                new java.awt.datatransfer.StringSelection(command);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(selection, null);
            copyButton.setText("Copied!");
            javax.swing.Timer timer = new javax.swing.Timer(2000, evt -> copyButton.setText("Copy"));
            timer.setRepeats(false);
            timer.start();
        });
        codePanel.add(copyButton, BorderLayout.EAST);

        return codePanel;
    }

    /**
     * 加载 SVG 图标
     *
     * @param resourcePath 资源路径，例如 "/icons/personal/github.svg"
     * @return Icon 对象，如果加载失败则返回 null
     */
    @Nullable
    private Icon loadSvgIcon(@NotNull String resourcePath) {
        try {
            return IconLoader.getIcon(resourcePath, PersonalInfoPanel.class);
        } catch (Exception e) {
            // 如果加载失败，返回 null，createIconLink 会处理
            return null;
        }
    }

    /**
     * 创建图标链接
     *
     * @param icon    图标（Icon 对象）
     * @param tooltip 提示文本
     * @param url     链接地址
     * @return 链接标签
     */
    private JBLabel createIconLink(@Nullable Icon icon, @NotNull String tooltip, @NotNull String url) {
        JBLabel label;
        final Icon originalIcon = icon;

        if (icon != null) {
            // 使用 SVG 图标
            label = new JBLabel(icon);
        } else {
            // 如果图标加载失败，使用文本占位符
            label = new JBLabel("🔗");
            label.setFont(label.getFont().deriveFont(Font.PLAIN, 20f));
        }

        label.setForeground(UIUtil.getLabelForeground());
        label.setToolTipText(tooltip);
        label.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.browse(url);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (originalIcon != null) {
                    // 对于 SVG 图标，不改变图标本身，只改变前景色（可能影响某些图标）
                    // SVG 图标会根据主题自动调整颜色，这里我们通过改变前景色来提供视觉反馈
                    label.setForeground(new JBColor(new java.awt.Color(102, 126, 234), new java.awt.Color(102, 126, 234)));
                } else {
                    // 对于文本占位符，改变文本颜色
                    label.setForeground(new JBColor(new java.awt.Color(102, 126, 234), new java.awt.Color(102, 126, 234)));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // 恢复文本颜色
                label.setForeground(UIUtil.getLabelForeground());
            }
        });

        return label;
    }

}

