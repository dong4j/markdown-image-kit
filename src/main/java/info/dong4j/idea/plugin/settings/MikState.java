package info.dong4j.idea.plugin.settings;

import info.dong4j.idea.plugin.enums.CloudEnum;
import info.dong4j.idea.plugin.enums.ImageMarkEnum;
import info.dong4j.idea.plugin.enums.InsertImageActionEnum;
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

    //region 插入图片时
    private InsertImageActionEnum insertImageAction = InsertImageActionEnum.NONE;
    private String currentInsertPath = "";
    /** 保存的自定义路径值，用于持久化用户输入的自定义路径，即使当前选择的不是"复制到指定路径"也保留 */
    private String savedCustomInsertPath = "";
    /** 图片保存路径，默认为 "./imgs" 目录，用于存储上传或生成的图片文件 */
    private String imageSavePath = "./imgs";
    /** 是否应用到本地图片 */
    private boolean applyToLocalImages = false;
    /** 是否应用到网络图片 */
    private boolean applyToNetworkImages = false;
    /** 是否优先使用相对路径: 关闭后会使用绝对路径 */
    private boolean preferRelativePath = false;
    /** 添加斜杠标志，用于控制是否在路径末尾添加斜杠 */
    private boolean addDotSlash = false;
    /** 自动转义图片 URL 标志，为 true 时会对图片 URL 进行转义处理 */
    private boolean autoEscapeImageUrl = false;
    //endregion

    //region 图片处理
    /** 重命名文件标志，用于指示是否需要对文件进行重命名操作 */
    private boolean rename = false;

    /**
     * 重命名模板，支持占位符格式：
     * <ul>
     *   <li>${datetime:format} - 日期时间格式化，如 ${datetime:yyyyMMdd}</li>
     *   <li>${string:length} - 随机字符串，如 ${string:6}</li>
     *   <li>${number:length} - 随机数字，如 ${number:6}</li>
     *   <li>${filename} - 原文件名（不含扩展名）</li>
     * </ul>
     * 默认值：${filename}（保持原文件名）
     *
     * @since 2.2.0
     */
    private String renameTemplate = "${filename}";

    /** 是否压缩图片 */
    private boolean compress = false;
    /** 压缩上传前的百分比，表示图片压缩到原始大小的百分比 */
    private int compressBeforeUploadOfPercent = 60;

    /** 是否将图片转换为 webp 格式 */
    private boolean convertToWebp = false;
    private int webpQuality = 60;

    /** 水印开关，用于控制是否显示水印 */
    private boolean watermark = false;
    /** 水印文本，用于在界面中显示的标识信息 */
    private String watermarkText = "@MIK";

    /** 是否替换标签 */
    private boolean changeToHtmlTag = false;
    /** 标签类型枚举，用于标识图片标签的类型（原始、正常、点击放大、自定义） */
    private ImageMarkEnum imageMarkEnum = ImageMarkEnum.ORIGINAL;
    /** 自定义标签代码，仅当 imageMarkEnum 为 CUSTOM 时使用 */
    private String customTagCode = "";
    //endregion

    //region 上传服务设定
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
    /** 是否启用自定义默认图床功能 */
    private boolean defaultCloudCheck = true;
    /** 默认图床类型，取值为 CloudEnum.ALIYUN_CLOUD 的 getIndex(), 注意: 不是枚举的 index */
    private int defaultCloudType = DEFAULT_CLOUD.getIndex();
    /** 用于保存未勾选自定义默认图床时需要保存的下拉列表选项，仅在 setting 页面使用 */
    private int tempCloudType = DEFAULT_CLOUD.getIndex();
    //endregion

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
