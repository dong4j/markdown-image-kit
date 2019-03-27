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

package info.dong4j.idea.plugin.action.image;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.chain.ActionManager;
import info.dong4j.idea.plugin.chain.ImageCompressHandler;
import info.dong4j.idea.plugin.chain.ImageLabelChangeHandler;
import info.dong4j.idea.plugin.chain.ImageLabelInsertHandler;
import info.dong4j.idea.plugin.chain.paste.ImageUploadHandler;
import info.dong4j.idea.plugin.entity.EventData;
import info.dong4j.idea.plugin.enums.InsertEnum;
import info.dong4j.idea.plugin.task.ChainBackgroupTask;

import org.jetbrains.annotations.Contract;

import java.io.*;
import java.util.Map;

import javax.swing.Icon;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 图片右键直接上传</p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-26 15:36
 */
public final class ImageUploadAction extends ImageActionBase {
    @Contract(pure = true)
    @Override
    protected Icon getIcon() {
        return AllIcons.Debugger.Overhead;
    }

    @Override
    protected void execute(Map<String, File> imageMap, Map<String, VirtualFile> virtualFileMap, Project project) {
        EventData data = new EventData()
            .setProject(project)
            .setImageMap(imageMap)
            .setInsertType(InsertEnum.CLIPBOADR);

        ActionManager manager = new ActionManager(data)
            .addHandler(new ImageCompressHandler())
            .addHandler(new ImageUploadHandler())
            .addHandler(new ImageLabelChangeHandler())
            .addHandler(new ImageLabelInsertHandler());

        new ChainBackgroupTask(project,
                               "Image Upload Task",
                               manager).queue();
    }
}
