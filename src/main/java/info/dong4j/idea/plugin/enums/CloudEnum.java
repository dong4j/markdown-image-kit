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

package info.dong4j.idea.plugin.enums;

import org.jetbrains.annotations.Contract;

/**
 * <p>Company: no company</p>
 * <p>Description: 枚举顺序不能改变
 * 后期扩展时, 只需要实现具体的上传逻辑, 需要在此处添加具体的 OssClient</p>
 *
 * @author dong4j
 * @date 2019-03-17 00:15
 * @email dong4j@gmail.com
 */
public enum CloudEnum {
    /** @see info.dong4j.idea.plugin.client.SmmsClient */
    SM_MS_CLOUD(-1, "sm.ms", "info.dong4j.idea.plugin.client.SmmsClient"),
    /** @see info.dong4j.idea.plugin.client.WeiboOssClient */
    WEIBO_CLOUD(0, "Weibo", "info.dong4j.idea.plugin.client.WeiboOssClient"),
    /** @see info.dong4j.idea.plugin.client.AliyunOssClient */
    ALIYUN_CLOUD(1, "Aliyun", "info.dong4j.idea.plugin.client.AliyunOssClient"),
    /** @see info.dong4j.idea.plugin.client.QiniuOssClient */
    QINIU_CLOUD(2, "Qiniu", "info.dong4j.idea.plugin.client.QiniuOssClient"),
    /** @see info.dong4j.idea.plugin.client.TencentOssClient */
    TENCENT_CLOUD(3, "腾讯云", "info.dong4j.idea.plugin.client.TencentOssClient"),
    WANGYI_CLOUD(4, "网易云", ""),
    /** @see info.dong4j.idea.plugin.client.BaiduOssClient */
    BAIDU_CLOUD(5, "百度云", "info.dong4j.idea.plugin.client.BaiduOssClient"),
    JINGDONG_CLOUD(6, "京东云", ""),
    YOUPAI_CLOUD(7, "又拍云", ""),
    IMGUR_CLOUD(8, "Imgur", ""),
    U_CLOUD(9, "Ucloud", ""),
    QING_CLOUD(10, "QingCloud", ""),
    CUSTOMIZE(11, "自定义", "");

    public int index;
    public String title;
    /** 图床特征*/
    public String feature;

    @Contract(pure = true)
    CloudEnum(int index, String title, String feature) {
        this.index = index;
        this.title = title;
        this.feature = feature;
    }

    @Contract(pure = true)
    public int getIndex() {
        return index;
    }

    @Contract(pure = true)
    public String getTitle() {
        return title;
    }

    @Contract(pure = true)
    public String getFeature() {
        return feature;
    }
}
