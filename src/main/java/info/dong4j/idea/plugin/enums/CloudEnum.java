/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
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

package info.dong4j.idea.plugin.enums;

import info.dong4j.idea.plugin.MikBundle;

import org.jetbrains.annotations.Contract;

/**
 * <p>Company: no company</p>
 * <p>Description: 枚举顺序不能改变
 * 后期扩展时, 只需要实现具体的上传逻辑, 需要在此处添加具体的 OssClient</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.17 00:15
 * @since 0.0.1
 */
public enum CloudEnum {
    /** Sm ms cloud  @see  info.dong4j.idea.plugin.client.SmmsClient */
    SM_MS_CLOUD(-1, "sm.ms", "info.dong4j.idea.plugin.client.SmmsClient"),
    /** Weibo cloud  @see  info.dong4j.idea.plugin.client.WeiboOssClient */
    WEIBO_CLOUD(0, MikBundle.message("oss.title.weibo"), "info.dong4j.idea.plugin.client.WeiboOssClient"),
    /** Aliyun cloud  @see  info.dong4j.idea.plugin.client.AliyunOssClient */
    ALIYUN_CLOUD(1, MikBundle.message("oss.title.aliyun"), "info.dong4j.idea.plugin.client.AliyunOssClient"),
    /** Qiniu cloud  @see  info.dong4j.idea.plugin.client.QiniuOssClient */
    QINIU_CLOUD(2, MikBundle.message("oss.title.qiniu"), "info.dong4j.idea.plugin.client.QiniuOssClient"),
    /** Tencent cloud  @see  info.dong4j.idea.plugin.client.TencentOssClient */
    TENCENT_CLOUD(3, MikBundle.message("oss.title.tencent"), "info.dong4j.idea.plugin.client.TencentOssClient"),
    /** Wangyi cloud cloud enum */
    WANGYI_CLOUD(4, MikBundle.message("oss.title.wangyi"), ""),
    /** Baidu cloud  @see  info.dong4j.idea.plugin.client.BaiduBosClient */
    BAIDU_CLOUD(5, MikBundle.message("oss.title.baidu"), "info.dong4j.idea.plugin.client.BaiduBosClient"),
    /** Jingdong cloud cloud enum */
    JINGDONG_CLOUD(6, MikBundle.message("oss.title.jingdong"), ""),
    /** Youpai cloud cloud enum */
    YOUPAI_CLOUD(7, MikBundle.message("oss.title.upyun"), ""),
    /** Imgur cloud cloud enum */
    IMGUR_CLOUD(8, "Imgur", ""),
    /** U cloud cloud enum */
    U_CLOUD(9, "Ucloud", ""),
    /** Qing cloud cloud enum */
    QING_CLOUD(10, MikBundle.message("oss.title.qingcloud"), ""),
    /** Github cloud enum */
    GITHUB(11, "GitHub", "info.dong4j.idea.plugin.client.GithubClient"),
    /** Gitee cloud enum */
    GITEE(12, "Gitee", "info.dong4j.idea.plugin.client.GiteeClient"),
    /** Customize cloud enum */
    CUSTOMIZE(13, MikBundle.message("oss.title.custom"), "info.dong4j.idea.plugin.client.CustomOssClient");

    /** Index */
    public int index;
    /** Title */
    public String title;
    /** 图床特征 */
    public String feature;

    /**
     * Cloud enum
     *
     * @param index   index
     * @param title   title
     * @param feature feature
     * @since 0.0.1
     */
    @Contract(pure = true)
    CloudEnum(int index, String title, String feature) {
        this.index = index;
        this.title = title;
        this.feature = feature;
    }

    /**
     * Gets index *
     *
     * @return the index
     * @since 0.0.1
     */
    @Contract(pure = true)
    public int getIndex() {
        return this.index;
    }

    /**
     * Gets title *
     *
     * @return the title
     * @since 0.0.1
     */
    @Contract(pure = true)
    public String getTitle() {
        return this.title;
    }

    /**
     * Gets feature *
     *
     * @return the feature
     * @since 0.0.1
     */
    @Contract(pure = true)
    public String getFeature() {
        return this.feature;
    }
}
