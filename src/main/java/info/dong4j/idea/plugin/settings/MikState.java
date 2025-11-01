package info.dong4j.idea.plugin.settings;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.SuffixEnum;
import info.dong4j.idea.plugin.settings.oss.AliyunOssState;
import info.dong4j.idea.plugin.settings.oss.BaiduBosState;
import info.dong4j.idea.plugin.settings.oss.CustomOssState;
import info.dong4j.idea.plugin.settings.oss.GiteeOssState;
import info.dong4j.idea.plugin.settings.oss.GithubOssState;
import info.dong4j.idea.plugin.settings.oss.PicListOssState;
import info.dong4j.idea.plugin.settings.oss.QiniuOssState;
import info.dong4j.idea.plugin.settings.oss.SmmsOssState;
import info.dong4j.idea.plugin.settings.oss.TencentOssState;

import lombok.Data;

/**
 * 图床配置状态类
 * <p>
 * 用于保存和管理图片上传时的各种配置状态，包括 OSS 类型选择、是否替换标签、压缩设置、备份选项、水印配置、文件重命名等。
 * 该类主要用于处理图片上传前的参数配置，支持多种云存储平台（如 Aliyun、Qiniu 等）的适配。
 * <p>
 * 包含多个静态常量用于表示不同的 OSS 类型，以及多个私有字段用于存储具体的配置信息。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 0.0.1
 */
@Data
public class MikState {
    /** 默认云服务类型，表示使用 SM_MS_CLOUD 云服务 */
    public static final CloudEnum DEFAULT_CLOUD = CloudEnum.SM_MS_CLOUD;
    /** 旧哈希键，用于标识旧的哈希配置 */
    public static final String OLD_HASH_KEY = "old";
    /** 新的哈希键，用于标识特定的哈希配置 */
    public static final String NEW_HASH_KEY = "new";
    /** smms oss 状态 */
    private SmmsOssState smmsOssState;
    /** AliyunOssState 对象，用于存储阿里云 OSS 的状态信息 */
    private AliyunOssState aliyunOssState;
    /** Baidu Bos 状态信息，用于记录与 Baidu Bos 服务交互的状态 */
    private BaiduBosState baiduBosState;
    /** QiniuOssState 对象，用于存储七牛云对象存储的相关状态信息 */
    private QiniuOssState qiniuOssState;
    /** TencentOssState 对象，用于存储腾讯云对象存储服务的相关状态信息 */
    private TencentOssState tencentOssState;
    /** GitHub OSS 状态信息，用于表示与 GitHub 仓库相关的 OSS 操作状态 */
    private GithubOssState githubOssState;
    /** Gitee 云服务 OSS 状态信息 */
    private GiteeOssState giteeOssState;
    /** 自定义 OSS 状态信息，用于表示与 OSS 相关的特定状态 */
    private CustomOssState customOssState;
    /** PicList 图床状态信息 */
    private PicListOssState picListOssState;
    /** 是否替换标签 */
    private boolean changeToHtmlTag = false;
    /** 替换的标签类型 */
    private String tagType = "";
    /** 替换的标签类型代码 */
    private String tagTypeCode = "";
    /** 是否压缩图片 */
    private boolean compress = false;
    /** 压缩上传前的百分比，表示图片压缩到原始大小的百分比 */
    private int compressBeforeUploadOfPercent = 60;
    /** 图片备份标志，表示是否启用图片备份功能 */
    private boolean backup = false;
    /** 水印开关，用于控制是否显示水印 */
    private boolean watermark = false;
    /** 水印文本，用于在界面中显示的标识信息 */
    private String watermarkText = "@MIK";
    /** 拷贝图片到目录标志，用于指示是否将图片复制到指定目录 */
    private boolean copyToDir = false;
    /** 上传图片并替换标志位，用于控制是否执行图片上传及替换操作 */
    private boolean uploadAndReplace = false;
    /** 图片保存路径，默认为 "./imgs" 目录，用于存储上传或生成的图片文件 */
    private String imageSavePath = "./imgs";
    /** 是否启用自定义默认图床功能 */
    private boolean defaultCloudCheck = true;
    /** 默认图床类型，取值为 CloudEnum.ALIYUN_CLOUD 的 getIndex(), 注意: 不是枚举的 index */
    private int defaultCloudType = DEFAULT_CLOUD.getIndex();
    /** 用于保存未勾选自定义默认图床时需要保存的下拉列表选项，仅在 setting 页面使用 */
    private int tempCloudType = DEFAULT_CLOUD.getIndex();
    /** 重命名文件标志，用于指示是否需要对文件进行重命名操作 */
    private boolean rename = false;
    /** 文件名后缀索引，用于标识当前文件的后缀类型 */
    private int suffixIndex = SuffixEnum.FILE_NAME.index;
    /** 是否将图片转换为 webp 格式 */
    private boolean convertToWebp = false;

    /**
     * 初始化MikState对象，用于管理各种对象存储服务的状态
     * <p>
     * 构造函数会初始化所有支持的对象存储服务状态对象，包括阿里云OSS、百度BOS、
     * 七牛OSS、腾讯云OSS、GitHub、Gitee、自定义OSS以及网易云OSS的状态。
     *
     * @since 0.0.1
     */
    public MikState() {
        this.smmsOssState = new SmmsOssState();
        this.aliyunOssState = new AliyunOssState();
        this.baiduBosState = new BaiduBosState();
        this.qiniuOssState = new QiniuOssState();
        this.tencentOssState = new TencentOssState();
        this.githubOssState = new GithubOssState();
        this.giteeOssState = new GiteeOssState();
        this.customOssState = new CustomOssState();
        this.picListOssState = new PicListOssState();
    }
}
