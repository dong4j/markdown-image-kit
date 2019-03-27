/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j <dong4j@gmail.com>
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
 *
 */

package info.dong4j.idea.plugin.action.markdown;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DocumentAdapter;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.settings.ImageManagerPersistenComponent;
import info.dong4j.idea.plugin.settings.JTextFieldHintListener;
import info.dong4j.idea.plugin.settings.MoveToOtherOssSettingsDialog;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.util.ActionUtils;
import info.dong4j.idea.plugin.util.MarkdownUtils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import javax.swing.event.DocumentEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 图床迁移计划 这里是批量迁移处理, 处理对象为 markdown 或者 目录</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-15 20:41
 */
@Slf4j
public final class MoveToOtherStorageAction extends AnAction {
    private static final String DOMAIN_DEFAULT_MESSAGE = MikBundle.message("mik.panel.message.domain-field");

    @Override
    public void update(@NotNull AnActionEvent event) {
        ActionUtils.isAvailable(event, AllIcons.Actions.Lightning, MarkdownContents.MARKDOWN_TYPE_NAME);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        final Project project = event.getProject();
        if (project != null) {
            MoveToOtherOssSettingsDialog dialog = showDialog();
            if (dialog == null) {
                return;
            }

            String domain = dialog.getDomain().getText().trim();

            if (StringUtils.isBlank(domain)) {
                return;
            }

            CloudEnum cloudEnum = OssState.getCloudType(dialog.getCloudComboBox().getSelectedIndex());

            if (!OssState.getStatus(dialog.getCloudComboBox().getSelectedIndex())) {
                return;
            }

            Map<Document, List<MarkdownImage>> waitingForMoveMap = MarkdownUtils.getProcessMarkdownInfo(event,
                                                                                                        project);

            for (Map.Entry<Document, List<MarkdownImage>> entry : waitingForMoveMap.entrySet()) {
                // 排除 LOCAL 和用户输入不匹配的标签
                entry.getValue().removeIf(markdownImage -> markdownImage.getLocation().equals(ImageLocationEnum.LOCAL)
                                                           || !markdownImage.getPath().contains(domain));
            }

            log.trace("waitingForMoveMap = {}", waitingForMoveMap);

            execute(waitingForMoveMap);
        }
    }

    private void execute(Map<Document, List<MarkdownImage>> waitingForMoveMap) {
        // 1. 通过 URL 上传图片
        // 2. 替换标签
        // 3. 插入到原来的位置
    }

    /**
     * 初始化 dialog
     *
     * @return the move to other oss settings dialog
     */
    @Nullable
    private static MoveToOtherOssSettingsDialog showDialog() {
        DialogBuilder builder = new DialogBuilder();
        MoveToOtherOssSettingsDialog dialog = new MoveToOtherOssSettingsDialog();

        int index = ImageManagerPersistenComponent.getInstance().getState().getCloudType();
        dialog.getCloudComboBox().setSelectedIndex(index);
        showMessage(builder, dialog, index);

        dialog.getCloudComboBox().addActionListener(e -> {
            int selectedIndex = dialog.getCloudComboBox().getSelectedIndex();
            showMessage(builder, dialog, selectedIndex);
        });

        dialog.getDomain().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                boolean isInput = StringUtils.isNotBlank(dialog.getDomain().getText()) && !DOMAIN_DEFAULT_MESSAGE.equals(dialog.getDomain().getText());
                if (isInput && OssState.getStatus(dialog.getCloudComboBox().getSelectedIndex())) {
                    builder.setOkActionEnabled(true);
                } else {
                    builder.setOkActionEnabled(false);
                }
            }
        });

        dialog.getDomain().addFocusListener(new JTextFieldHintListener(dialog.getDomain(), DOMAIN_DEFAULT_MESSAGE));

        // http://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html
        PropertiesComponent propComp = PropertiesComponent.getInstance();

        builder.setOkActionEnabled(false);
        builder.setCenterPanel(dialog.getContentPane());
        builder.setTitle("图床迁移计划");
        builder.removeAllActions();
        builder.addOkAction();
        builder.addCancelAction();
        builder.setPreferredFocusComponent(dialog.getCloudComboBox());
        builder.setOkOperation((() -> {
            log.trace("自定义 ok 操作");
            builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
        }));

        if (builder.show() != DialogWrapper.OK_EXIT_CODE) {
            return null;
        }
        return dialog;
    }

    /**
     * 初始化 message 监听更新 ok 按钮可用状态
     *
     * @param builder the builder
     * @param dialog  the dialog
     * @param index   the index
     */
    private static void showMessage(DialogBuilder builder, MoveToOtherOssSettingsDialog dialog, int index) {
        if (!OssState.getStatus(index)) {
            dialog.getMessage().setForeground(Color.RED);
            dialog.getMessage().setText("当前 OSS 不可用");
            builder.setOkActionEnabled(false);
        } else if (!DOMAIN_DEFAULT_MESSAGE.equals(dialog.getDomain().getText()) && StringUtils.isNotBlank(dialog.getDomain().getText())) {
            dialog.getMessage().setText("   ");
            builder.setOkActionEnabled(true);
        }
    }
}
