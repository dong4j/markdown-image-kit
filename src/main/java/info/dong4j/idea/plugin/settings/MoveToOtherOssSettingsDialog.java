/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
 * <p>Company: no company</p>
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
