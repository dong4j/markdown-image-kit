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

package info.dong4j.idea.plugin.strategy;

import info.dong4j.idea.plugin.client.OssClient;

import java.io.*;

import javax.swing.JPanel;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: 从测试按钮发起的上传请求</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-22 13:14
 */
public class UploadFromTest implements UploadWay {
    private OssClient client;
    private InputStream inputStream;
    private String fileName;
    private JPanel jPanel;

    /**
     * Instantiates a new Upload from test.
     *
     * @param client      the client
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     */
    public UploadFromTest(OssClient client, InputStream inputStream, String fileName, JPanel jPanel) {
        this.client = client;
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.jPanel = jPanel;
    }

    @Override
    public String upload() {
        return client.upload(inputStream, fileName, jPanel);
    }
}
