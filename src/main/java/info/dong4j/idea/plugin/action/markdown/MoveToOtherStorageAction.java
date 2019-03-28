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
import com.intellij.ui.JBColor;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.ImageLabelJoinHandler;
import info.dong4j.idea.plugin.chain.ImageUploadHandler;
import info.dong4j.idea.plugin.chain.OptionClientHandler;
import info.dong4j.idea.plugin.chain.ReplaceToDocument;
import info.dong4j.idea.plugin.chain.ResolveMarkdownFileHandler;
import info.dong4j.idea.plugin.client.OssClient;
import info.dong4j.idea.plugin.content.MarkdownContents;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.entity.MarkdownImage;
import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.settings.JTextFieldHintListener;
import info.dong4j.idea.plugin.settings.MikPersistenComponent;
import info.dong4j.idea.plugin.settings.MoveToOtherOssSettingsDialog;
import info.dong4j.idea.plugin.settings.OssState;
import info.dong4j.idea.plugin.task.ActionTask;
import info.dong4j.idea.plugin.util.ActionUtils;
import info.dong4j.idea.plugin.util.ClientUtils;
import info.dong4j.idea.plugin.util.ConvertUtil;
import info.dong4j.idea.plugin.util.ImageUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.*;
import java.util.Iterator;
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
    private static final int CONNECTION_TIMEOUT = 5 * 1000;
    private static final int READ_TIMEOUT = 10 * 1000;

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
            if (!OssState.getStatus(dialog.getCloudComboBox().getSelectedIndex())) {
                return;
            }

            int cloudIndex = dialog.getCloudComboBox().getSelectedIndex();
            CloudEnum cloudEnum = OssState.getCloudType(cloudIndex);

            EventData data = new EventData()
                .setActionEvent(event)
                .setProject(project)
                .setClient(ClientUtils.getClient(cloudEnum))
                // client 有可能为 null, 使用 cloudEnum 安全点
                .setClientName(cloudEnum.title);

            // 过滤掉 LOCAL 和用户输入不匹配的标签
            ResolveMarkdownFileHandler resolveMarkdownFileHandler = new ResolveMarkdownFileHandler();
            resolveMarkdownFileHandler.setFileFilter(waitingProcessMap -> {
                if (waitingProcessMap != null && waitingProcessMap.size() > 0) {
                    for (Map.Entry<Document, List<MarkdownImage>> entry : waitingProcessMap.entrySet()) {
                        log.trace("old waitingProcessMap = {}", waitingProcessMap);

                        Iterator<MarkdownImage> iterator = entry.getValue().iterator();
                        while (iterator.hasNext()) {
                            MarkdownImage markdownImage = iterator.next();
                            OssClient client = data.getClient();
                            // 排除 LOCAL 和用户输入不匹配的标签和
                            if (markdownImage.getLocation().equals(ImageLocationEnum.LOCAL)
                                || !markdownImage.getPath().contains(domain)
                                || markdownImage.getPath().contains(client.getCloudType().feature)) {

                                iterator.remove();
                            } else {
                                // 将 URL 图片转成 inputstream
                                File temp = ImageUtils.buildTempFile(markdownImage.getImageName());
                                try {
                                    FileUtils.copyURLToFile(new URL(markdownImage.getPath()), temp, CONNECTION_TIMEOUT, READ_TIMEOUT);
                                } catch (IOException e) {
                                    log.trace("", e);
                                    iterator.remove();
                                    temp.deleteOnExit();
                                    continue;
                                }

                                try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(temp))) {
                                    markdownImage.setInputStream(ConvertUtil.parse(outputStream));
                                } catch (Exception e) {
                                    log.trace("", e);
                                } finally {
                                    temp.deleteOnExit();
                                }
                            }
                        }
                        log.trace("new waitingProcessMap = {}", waitingProcessMap);
                    }
                }
            });

            ActionManager manager = new ActionManager(data)
                // 解析 markdown 文件
                .addHandler(resolveMarkdownFileHandler)
                // 处理 client
                .addHandler(new OptionClientHandler())
                // 图片上传
                .addHandler(new ImageUploadHandler())
                // 拼接标签
                .addHandler(new ImageLabelJoinHandler())
                // 标签转换
                .addHandler(new ImageLabelChangeHandler())
                // 写入标签
                .addHandler(new ReplaceToDocument());

            new ActionTask(project, MikBundle.message("mik.action.move.process", cloudEnum.title), manager).queue();
        }
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

        int index = MikPersistenComponent.getInstance().getState().getCloudType();
        dialog.getCloudComboBox().setSelectedIndex(index);
        showMessage(builder, dialog, index);

        dialog.getCloudComboBox().addActionListener(e -> {
            int selectedIndex = dialog.getCloudComboBox().getSelectedIndex();
            showMessage(builder, dialog, selectedIndex);
        });

        dialog.getDomain().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                boolean isValidInput = StringUtils.isNotBlank(dialog.getDomain().getText()) && !DOMAIN_DEFAULT_MESSAGE.equals(dialog.getDomain().getText());
                boolean isClientEnable = OssState.getStatus(dialog.getCloudComboBox().getSelectedIndex());
                builder.setOkActionEnabled(isValidInput && isClientEnable);
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
    private static void showMessage(@NotNull DialogBuilder builder, @NotNull MoveToOtherOssSettingsDialog dialog, int index) {
        boolean isClientEnable = OssState.getStatus(index);
        boolean isValidInput = StringUtils.isNotBlank(dialog.getDomain().getText()) && !DOMAIN_DEFAULT_MESSAGE.equals(dialog.getDomain().getText());

        dialog.getMessage().setText(isClientEnable ? "" : "当前 OSS 不可用");
        dialog.getMessage().setForeground(isClientEnable ? JBColor.WHITE : JBColor.RED);
        builder.setOkActionEnabled(isClientEnable && isValidInput);
    }
}
