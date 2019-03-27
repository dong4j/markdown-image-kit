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

package info.dong4j.idea.plugin.entity;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.enums.InsertEnum;

import java.io.*;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-22 21:01
 */
@Data
@Accessors(chain = true)
public class EventData {
    /** 显示当前处理进度 */
    private ProgressIndicator indicator;
    /** chain size */
    private int size;
    /** 当前执行的节点 */
    private int index;
    private Project project;
    private Editor editor;
    /** 从 clipboard 中解析的文件 */
    private Map<String, File> imageMap;
    private Map<String, VirtualFile> virtualFileMap;
    /** save markdown image mark */
    private List<String> saveMarkList;
    /** upload markdown image mark */
    private List<String> uploadedMarkList;
    /** save image task status */
    private volatile boolean saveImageFinished = false;
    /** upload image task status */
    private volatile boolean uploadImageFinished = false;
    /** markdown image mark 插入的位置 */
    private InsertEnum insertType = InsertEnum.DOCUMENT;
    /** Intention 需要使用到的数据*/
    private MarkdownImage markdownImage;
}
