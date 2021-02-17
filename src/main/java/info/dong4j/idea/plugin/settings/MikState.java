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

package info.dong4j.idea.plugin.settings;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.SuffixEnum;
import info.dong4j.idea.plugin.settings.oss.AliyunOssState;
import info.dong4j.idea.plugin.settings.oss.BaiduBosState;
import info.dong4j.idea.plugin.settings.oss.CustomOssState;
import info.dong4j.idea.plugin.settings.oss.GiteeOssState;
import info.dong4j.idea.plugin.settings.oss.GithubOssState;
import info.dong4j.idea.plugin.settings.oss.QiniuOssState;
import info.dong4j.idea.plugin.settings.oss.TencentOssState;
import info.dong4j.idea.plugin.settings.oss.WeiboOssState;

import lombok.Data;

/**
 * <p>Company: no company</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Data
public class MikState {
    /** 加密盐值 */
    public static final String WEIBOKEY = "ekjgbpiq!g34o@erberb.erbmkv.c;,ergw_.";
    /** ALIYUN */
    public static final String ALIYUN = "awj7@piq!g3jo@er_erb.erbsrxhc!,wr.w_1";
    /** BAIDU */
    public static final String BAIDU = "efweg23142f!@23q4t=23rtfw_23tr1!2r.123";
    /** QINIU */
    public static final String QINIU = "gerb2.erhgds'5yf@4ybtree!43h34hbd4_";
    /** TENCENT */
    public static final String TENCENT = "xg13g143fvsdklo)2,m.we_123vds12e!#41c";
    /** GITHUB */
    public static final String GITHUB = "ewrg23e!3t@2i_324ugfvz2r29_hfvgdfbd23";
    /** GITEE */
    public static final String GITEE = "ewgkrblkjgbj@T*&!B_wlkjvbjhavt87112fsd";

    /** OLD_HASH_KEY */
    public static final String OLD_HASH_KEY = "old";
    /** NEW_HASH_KEY */
    public static final String NEW_HASH_KEY = "new";

    /** Weibo oss state */
    private WeiboOssState weiboOssState;
    /** Aliyun oss state */
    private AliyunOssState aliyunOssState;
    /** Baidu bos state */
    private BaiduBosState baiduBosState;
    /** Qiniu oss state */
    private QiniuOssState qiniuOssState;
    /** Tencent oss state */
    private TencentOssState tencentOssState;
    /** Git hub oss state */
    private GithubOssState githubOssState;
    /** Gitee oss state */
    private GiteeOssState giteeOssState;
    /** Custom oss state */
    private CustomOssState customOssState;

    /** 是否替换标签 */
    private boolean changeToHtmlTag = false;
    /** 替换的标签类型 */
    private String tagType = "";
    /** 替换的标签类型 code */
    private String tagTypeCode = "";
    /** 是否压缩图片 */
    private boolean compress = false;
    /** Compress before upload of percent */
    private int compressBeforeUploadOfPercent = 60;
    /** 图片备份 */
    private boolean backup = false;
    /** 拷贝图片到目录 */
    private boolean copyToDir = false;
    /** 上传图片并替换 */
    private boolean uploadAndReplace = false;
    /** 图片保存路径 */
    private String imageSavePath = "./imgs";
    /** 是否自定义默认图床 */
    private boolean defaultCloudCheck = false;
    /** 默认图床 */
    private int cloudType = CloudEnum.SM_MS_CLOUD.index;
    /** 这个只是 setting 页面用, 用于保存未勾选自定义默认图床时需要保存的下拉列表选项 */
    private int tempCloudType = CloudEnum.WEIBO_CLOUD.index;
    /** 重命名文件 */
    private boolean rename = false;
    /** 文件名后缀 */
    private int suffixIndex = SuffixEnum.FILE_NAME.index;

    /**
     * Mik state
     *
     * @since 0.0.1
     */
    public MikState() {
        this.aliyunOssState = new AliyunOssState();
        this.baiduBosState = new BaiduBosState();
        this.qiniuOssState = new QiniuOssState();
        this.weiboOssState = new WeiboOssState();
        this.tencentOssState = new TencentOssState();
        this.githubOssState = new GithubOssState();
        this.giteeOssState = new GiteeOssState();
        this.customOssState = new CustomOssState();
    }
}
