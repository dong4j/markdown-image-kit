package info.dong4j.idea.plugin.enums;

import org.jetbrains.annotations.NonNls;

/**
 * 图像媒体类型枚举
 * <p>
 * 该枚举用于表示各种图像文件的媒体类型，每个枚举值对应一个特定的MIME类型，常用于网络传输、文件识别等场景。
 * <p>
 * 枚举值包含常见的图像格式，如JPEG、PNG、GIF等，以及一些较为特殊的图像格式，如CGM、FITS等。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2021.02.14
 * @since 1.1.0
 */
public enum ImageMediaType {
    /** CGM 图像媒体类型 */
    CGM("image/cgm"),
    /** 示例图片媒体类型 */
    EXAMPLE("image/example"),
    /** FITS 图像媒体类型 */
    FITS("image/fits"),
    /** G3 fax 图像媒体类型 */
    G3FAX("image/g3fax"),
    /** GIF 图像媒体类型 */
    GIF("image/gif"),
    /** IEF 图像媒体类型 */
    IEF("image/ief"),
    /** JP2 图像媒体类型 */
    JP2("image/jp2"),
    /** JPEG 图像媒体类型 */
    JPEG("image/jpeg"),
    /** JPM 图像媒体类型 */
    JPM("image/jpm"),
    /** JPX 图像媒体类型 */
    JPX("image/jpx"),
    /** NAPLPS 图像媒体类型 */
    NAPLPS("image/naplps"),
    /** PNG 图像媒体类型 */
    PNG("image/png"),
    /** PRS_BTIF 媒体类型，表示图像的 PRS.btif 格式 */
    PRS_BTIF("image/prs.btif"),
    /** PRS-PTI 图像媒体类型 */
    PRS_PTI("image/prs.pti"),
    /** 图像媒体类型，表示 T.38 协议的图像格式 */
    T38("image/t38"),
    /** TIFF 图像媒体类型 */
    TIFF("image/tiff"),
    /** TIFF FX 图像媒体类型，表示带有 FX 特效的 TIFF 图像格式 */
    TIFF_FX("image/tiff-fx"),
    /** 图像/vnd.adobe.photoshop 媒体类型，表示 Adobe Photoshop 图像格式 */
    VND_ADOBE_PHOTOSHOP("image/vnd.adobe.photoshop"),
    /** VND_CNS_INF2 媒体类型，表示图像格式 */
    VND_CNS_INF2("image/vnd.cns.inf2"),
    /** djvu 格式的图像媒体类型 */
    VND_DJVU("image/vnd.djvu"),
    /** DWG 图像媒体类型 */
    VND_DWG("image/vnd.dwg"),
    /** VND_DXF 图像媒体类型，表示 DXF 格式的图像 */
    VND_DXF("image/vnd.dxf"),
    /** VND_FASTBIDSHEET 图片媒体类型，用于标识 FastBidsheet 格式的图像文件 */
    VND_FASTBIDSHEET("image/vnd.fastbidsheet"),
    /** VND_FPX 图像媒体类型，表示 FXpixmap 格式的图像 */
    VND_FPX("image/vnd.fpx"),
    /** VND_FST 图像媒体类型，表示一种特定的图像格式 */
    VND_FST("image/vnd.fst"),
    /** VND_FUJIXEROX_EDMICS_MMR 图像媒体类型，表示富士施乐 EDMICS MMR 格式 */
    VND_FUJIXEROX_EDMICS_MMR("image/vnd.fujixerox.edmics-mmr"),
    /** VND_FUJIXEROX_EDMICS_RLC 图像媒体类型，表示富士施乐 EDMICS RLC 格式 */
    VND_FUJIXEROX_EDMICS_RLC("image/vnd.fujixerox.edmics-rlc"),
    /** Vnd globalgraphics pgb 图像媒体类型 */
    VND_GLOBALGRAPHICS_PGB("image/vnd.globalgraphics.pgb"),
    /** Vnd microsoft icon 图标图像媒体类型，用于标识 Microsoft 图标格式 */
    VND_MICROSOFT_ICON("image/vnd.microsoft.icon"),
    /** VND_MIX 表示 image/vnd.mix 媒体类型，用于标识混合图像格式 */
    VND_MIX("image/vnd.mix"),
    /** VND_MS_MODI 图像媒体类型，表示 Microsoft MODI 图像格式 */
    VND_MS_MODI("image/vnd.ms-modi"),
    /** VND_NET_FPX 图像媒体类型，表示网络 FPX 图像格式 */
    VND_NET_FPX("image/vnd.net-fpx"),
    /** VND_SEALED_PNG 是一种表示密封 PNG 图像的媒体类型常量 */
    VND_SEALED_PNG("image/vnd.sealed.png"),
    /** 图像媒体类型，表示密封媒体软密封 GIF 格式 */
    VND_SEALEDMEDIA_SOFTSEAL_GIF("image/vnd.sealedmedia.softseal.gif"),
    /** image/vnd.sealedmedia.softseal.jpg 媒体类型，表示已密封的媒体软密封JPEG图像 */
    VND_SEALEDMEDIA_SOFTSEAL_JPG("image/vnd.sealedmedia.softseal.jpg"),
    /** VND_SVF 图片媒体类型，表示 SVF 格式的图像文件 */
    VND_SVF("image/vnd.svf"),
    /** WAP WBMP 图像媒体类型 */
    VND_WAP_WBMP("image/vnd.wap.wbmp"),
    /** Vnd xiff 图像媒体类型，用于标识 XIFF 格式的图像内容 */
    VND_XIFF("image/vnd.xiff");

    /** 内容类型 */
    private final String contentType;

    /**
     * 根据内容类型创建图像媒体类型对象
     * <p>
     * 使用指定的内容类型初始化图像媒体类型实例
     *
     * @param contentType 内容类型
     * @since 1.1.0
     */
    ImageMediaType(@NonNls String contentType) {
        this.contentType = contentType;
    }

    /**
     * 返回对象的字符串表示形式
     * <p>
     * 该方法重写 Object 类的 toString 方法，返回当前对象的 contentType 属性值。
     *
     * @return 对象的字符串表示，即 contentType 字段的值
     * @since 1.1.0
     */
    @Override
    public String toString() {
        return this.contentType;
    }
}