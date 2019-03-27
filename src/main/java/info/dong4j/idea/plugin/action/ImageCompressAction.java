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

package info.dong4j.idea.plugin.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.MikBundle;
import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.BaseActionHandler;
import info.dong4j.idea.plugin.chain.ImageCompressHandler;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.enums.InsertEnum;
import info.dong4j.idea.plugin.task.ChainBackgroupTask;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Contract;

import java.io.*;
import java.util.Map;

import javax.swing.Icon;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 图像压缩 </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-15 20:43
 */
@Slf4j
public final class ImageCompressAction extends ImageActionBase {

    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return AllIcons.Debugger.ShowCurrentFrame;
    }

    @Override
    protected void execute(Map<String, File> imageMap, Map<String, VirtualFile> virtualFileMap, Project project) {
        EventData data = new EventData()
            .setProject(project)
            .setImageMap(imageMap)
            .setVirtualFileMap(virtualFileMap)
            .setInsertType(InsertEnum.CLIPBOADR);

        ActionManager manager = new ActionManager(data)
            .addHandler(new ImageCompressHandler())
            .addHandler(new BaseActionHandler() {
                @Override
                public boolean isEnabled(EventData data) {
                    return true;
                }

                @Override
                public boolean execute(EventData data) {
                    int size = data.getSize();
                    ProgressIndicator indicator = data.getIndicator();
                    indicator.setText2(MikBundle.message("mik.chain.compress.progress"));
                    int totalCount = data.getImageMap().size();
                    int totalProcessed = 0;

                    // todo-dong4j : (2019年03月26日 19:40) [覆盖原文件]
                    Map<String, File> imageMap = data.getImageMap();
                    Map<String, VirtualFile> virtualFileMap = data.getVirtualFileMap();

                    for (Map.Entry<String, File> entry : imageMap.entrySet()) {
                        try {
                            FileUtils.copyFile(entry.getValue(), new File(virtualFileMap.get(entry.getKey()).getPath()));
                        } catch (IOException e) {
                            log.trace("", e);
                        }
                        indicator.setFraction(((++totalProcessed * 1.0) + data.getIndex() * size) / totalCount * size);
                    }
                    return true;
                }
            });

        new ChainBackgroupTask(project,
                               "Image Compress Task",
                               manager).queue();
    }

}
