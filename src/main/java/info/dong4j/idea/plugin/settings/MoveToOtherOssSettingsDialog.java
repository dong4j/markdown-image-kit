package info.dong4j.idea.plugin.settings;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import lombok.Getter;

/**
 * <p>Description: tip: 设置 dialog 大小, 使用 PreferredSize </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.27 14:26
 * @since 0.0.1
 */
@Getter
public class MoveToOtherOssSettingsDialog extends JDialog {
    private static final long serialVersionUID = -8961973066677674627L;
    /** Content pane */
    private JPanel contentPane;
    /** Domain */
    private JTextField domain;
    /** Cloud combo box */
    private JComboBox<?> cloudComboBox;
    /** Move panel */
    private JPanel movePanel;
    /** Domain label */
    private JLabel domainLabel;
    /** Move label */
    private JLabel moveLabel;
    /** Message */
    private JLabel message;
    /** Tip lable */
    private JLabel tipLable;

    /**
     * Move to other oss settings dialog
     *
     * @since 0.0.1
     */
    public MoveToOtherOssSettingsDialog() {
        this.setContentPane(this.contentPane);
        this.setModal(true);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                MoveToOtherOssSettingsDialog.this.onCancel();
            }
        });

        // call onCancel() on ESCAPE
        this.contentPane.registerKeyboardAction(e -> this.onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * On cancel
     *
     * @since 0.0.1
     */
    private void onCancel() {
        // add your code here if necessary
        this.dispose();
    }
}
