/*
 * MIT License
 *
 * Copyright (c) 2022 dong4j <dong4j@gmail.com>
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

package info.dong4j.idea.plugin.entity;

import com.intellij.openapi.vfs.VirtualFile;

import info.dong4j.idea.plugin.enums.ImageLocationEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;

import java.io.InputStream;
import java.io.Serializable;

import lombok.Data;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Data
public class MarkdownImage implements Serializable {
    /** serialVersionUID */
    private static final long serialVersionUID = -533088989259774894L;

    /** 对应的 document name */
    private String fileName;
    /** 图片名 */
    private String imageName;
    /** 图片类型 */
    private String extension;
    /** 原始行文本 */
    private String originalLineText;
    /** markdown image mark */
    private String originalMark;
    /** 行数 */
    private int lineNumber;
    /** 行第一个字符偏移量 */
    private int lineStartOffset;
    /** 行最后一个字符偏移量 */
    private int lineEndOffset;
    /** 图片标题 */
    private String title;
    /** 图片地址 (本地全路径, 网路 http/https) */
    private String path;
    /** 图片位置 */
    private ImageLocationEnum location;
    /** 文本标签类型 */
    private ImageMarkEnum imageMarkType;
    /** 图片文件流 */
    private InputStream inputStream;
    /** 最终需要插入到文本的标签 */
    private String finalMark;
    /** 保存当前标签对应的 image file */
    private VirtualFile virtualFile;
}
