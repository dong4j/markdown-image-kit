/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j
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

package info.dong4j.idea.plugin.content;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-16 12:18
 */
public interface MarkdownContents {
    /** markdown 文件后缀 */
    String MARKDOWN_FILE_SUFIX = ".md";
    /** markdown file type, 必须安装了 markdown 插件才能识别为此类型 */
    String MARKDOWN_FILE_TYPE = "Markdown";
    /** nodejs 库 */
    String NODE_MODULES_FILE = "node_modules";
}
