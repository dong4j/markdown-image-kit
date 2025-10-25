package info.dong4j.idea.plugin.settings;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import lombok.Getter;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.17 23:39
 * @since 1.5.0
 */
@Getter
public class CustomUploadErrorDialog extends JDialog {
    /** serialVersionUID */
    private static final long serialVersionUID = 4813169387139926948L;
    /** Content pane */
    private JPanel contentPane;
    /** Response */
    private JTextArea response;

    /**
     * Custom upload error dialog
     *
     * @since 1.5.0
     */
    public CustomUploadErrorDialog() {
        this.setContentPane(this.contentPane);
        this.setModal(true);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CustomUploadErrorDialog.this.onCancel();
            }
        });

        // call onCancel() on ESCAPE
        this.contentPane.registerKeyboardAction(e -> this.onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * On cancel
     *
     * @since 1.5.0
     */
    private void onCancel() {
        // 必要时在此处添加您的代码
        this.dispose();
    }

}
