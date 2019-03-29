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

package info.dong4j.idea.plugin.settings;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.SuffixEnum;

import lombok.Data;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3@iflytek.com
 * @since 2019-03-13 14:21
 */
@Data
public class MikState {
    public static final String WEIBOKEY = "ekjgbpiq!g34o@erberb.erbmkv.c;,ergw_.";
    public static final String ALIYUN = "awj7@piq!g3jo@er_erb.erbsrxhc!,wr.w_1";
    public static final String QINIU = "gerb2.erhgds'5yf@4ybtree!43h34hbd4_";
    public static final String OLD_HASH_KEY = "old";
    public static final String NEW_HASH_KEY = "new";
    private WeiboOssState weiboOssState;
    private AliyunOssState aliyunOssState;
    private QiniuOssState qiniuOssState;
    /** 是否替换标签 */
    private boolean changeToHtmlTag = false;
    /** 替换的标签类型 */
    private String tagType = "";
    /** 替换的标签类型 code */
    private String tagTypeCode = "";
    /** 是否压缩图片 */
    private boolean compress = false;
    private int compressBeforeUploadOfPercent = 60;
    /** 图片备份 */
    private boolean backup = false;
    /** 拷贝图片到目录 */
    private boolean copyToDir = false;
    /** 上传图片并替换 */
    private boolean uploadAndReplace = false;
    /** 图片保存路径 */
    private String imageSavePath = "./imgs";
    /** 默认图床 */
    private int cloudType = CloudEnum.WEIBO_CLOUD.index;
    /** 重命名文件 */
    private boolean rename = false;
    /** 文件名后缀 */
    private int suffixIndex = SuffixEnum.FILE_NAME.index;
    public MikState() {
        this.aliyunOssState = new AliyunOssState();
        this.qiniuOssState = new QiniuOssState();
        this.weiboOssState = new WeiboOssState();
    }
}
