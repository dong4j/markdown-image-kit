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

package info.dong4j.idea.plugin.client;

import info.dong4j.idea.plugin.enums.CloudEnum;

import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: oss client 实现步骤:
 * 1. 初始化配置: 从持久化配置中初始化 client
 * 2. 静态内部类获取 client 单例
 * 3. 实现 OssClient 接口
 * 4. 自定义 upload 逻辑</p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019-03-20 11:52
 */
public interface OssClient {
    /** 用于反射调用时的缓存 <className, client>, 容量为实现类个数 */
    Map<CloudEnum, OssClient> INSTANCES = new ConcurrentHashMap<>(12);

    /**
     * The constant getName.
     *
     * @return the name
     */
    default String getName(){
        return getCloudType().title;
    }

    /**
     * Gets cloud type.
     *
     * @return the cloud type
     */
    CloudEnum getCloudType();

    /**
     * 全部通过此接口上传
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @return the string
     */
    String upload(InputStream inputStream, String fileName);

    /**
     * "Upload Test" 按钮反射调用
     *
     * @param inputStream the input stream
     * @param fileName    the file name
     * @param jPanel      the j panel
     * @return the string
     */
    String upload(InputStream inputStream, String fileName, JPanel jPanel);

    /**
     * 需要设置 JTextField 的 name 属性
     *
     * @param jPanel the jpanel
     * @return the test field text
     */
    @NotNull
    default Map<String, String> getTestFieldText(JPanel jPanel) {
        Map<String, String> fieldMap = new HashMap<>(10);
        Component[] components = jPanel.getComponents();
        for (Component c : components) {
            if (c instanceof JTextField) {
                JTextField textField = (JTextField) c;
                fieldMap.put(textField.getName(), textField.getText());
            }
        }
        return fieldMap;
    }
}
