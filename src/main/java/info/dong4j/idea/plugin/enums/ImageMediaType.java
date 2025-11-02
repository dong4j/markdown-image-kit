package info.dong4j.idea.plugin.enums;

import info.dong4j.idea.plugin.util.StringUtils;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import lombok.Getter;

/**
 * 图像媒体类型枚举
 * <p>
 * 该枚举用于表示各种图像文件的媒体类型，每个枚举值对应一个特定的MIME类型和文件扩展名，常用于网络传输、文件识别等场景。
 * <p>
 * 枚举值包含常见的图像格式，如JPEG、PNG、GIF等，以及一些较为特殊的图像格式，如CGM、FITS等。
 *
 * @author dong4j
 * @version 2.0.0
 * @date 2021.02.14
 * @since 1.1.0
 */
@Getter
public enum ImageMediaType {
    /** CGM 图像媒体类型 */
    CGM("image/cgm", ".cgm"),
    /** 示例图片媒体类型 */
    EXAMPLE("image/example", ".example"),
    /** FITS 图像媒体类型 */
    FITS("image/fits", ".fits"),
    /** G3 fax 图像媒体类型 */
    G3FAX("image/g3fax", ".g3"),
    /** GIF 图像媒体类型 */
    GIF("image/gif", ".gif"),
    /** IEF 图像媒体类型 */
    IEF("image/ief", ".ief"),
    /** JP2 图像媒体类型 */
    JP2("image/jp2", ".jp2"),
    /** JPEG 图像媒体类型 */
    JPEG("image/jpeg", ".jpg"),
    /** JPM 图像媒体类型 */
    JPM("image/jpm", ".jpm"),
    /** JPX 图像媒体类型 */
    JPX("image/jpx", ".jpx"),
    /** NAPLPS 图像媒体类型 */
    NAPLPS("image/naplps", ".naplps"),
    /** PNG 图像媒体类型 */
    PNG("image/png", ".png"),
    /** BMP 图像媒体类型 */
    BMP("image/bmp", ".bmp"),
    /** SVG 图像媒体类型 */
    SVG_XML("image/svg+xml", ".svg"),
    /** WEBP 图像媒体类型 */
    WEBP("image/webp", ".webp"),
    /** AVIF 图像媒体类型 */
    AVIF("image/avif", ".avif"),
    /** HEIC 图像媒体类型 */
    HEIC("image/heic", ".heic"),
    /** HEIF 图像媒体类型 */
    HEIF("image/heif", ".heif"),
    /** x-icon 图标媒体类型 */
    X_ICON("image/x-icon", ".ico"),
    /** PRS_BTIF 媒体类型，表示图像的 PRS.btif 格式 */
    PRS_BTIF("image/prs.btif", ".btif"),
    /** PRS-PTI 图像媒体类型 */
    PRS_PTI("image/prs.pti", ".pti"),
    /** 图像媒体类型，表示 T.38 协议的图像格式 */
    T38("image/t38", ".t38"),
    /** TIFF 图像媒体类型 */
    TIFF("image/tiff", ".tiff"),
    /** TIFF FX 图像媒体类型，表示带有 FX 特效的 TIFF 图像格式 */
    TIFF_FX("image/tiff-fx", ".tfx"),
    /** 图像/vnd.adobe.photoshop 媒体类型，表示 Adobe Photoshop 图像格式 */
    VND_ADOBE_PHOTOSHOP("image/vnd.adobe.photoshop", ".psd"),
    /** VND_CNS_INF2 媒体类型，表示图像格式 */
    VND_CNS_INF2("image/vnd.cns.inf2", ".inf2"),
    /** djvu 格式的图像媒体类型 */
    VND_DJVU("image/vnd.djvu", ".djvu"),
    /** DWG 图像媒体类型 */
    VND_DWG("image/vnd.dwg", ".dwg"),
    /** VND_DXF 图像媒体类型，表示 DXF 格式的图像 */
    VND_DXF("image/vnd.dxf", ".dxf"),
    /** VND_FASTBIDSHEET 图片媒体类型，用于标识 FastBidsheet 格式的图像文件 */
    VND_FASTBIDSHEET("image/vnd.fastbidsheet", ".fbs"),
    /** VND_FPX 图像媒体类型，表示 FXpixmap 格式的图像 */
    VND_FPX("image/vnd.fpx", ".fpx"),
    /** VND_FST 图像媒体类型，表示一种特定的图像格式 */
    VND_FST("image/vnd.fst", ".fst"),
    /** VND_FUJIXEROX_EDMICS_MMR 图像媒体类型，表示富士施乐 EDMICS MMR 格式 */
    VND_FUJIXEROX_EDMICS_MMR("image/vnd.fujixerox.edmics-mmr", ".mmr"),
    /** VND_FUJIXEROX_EDMICS_RLC 图像媒体类型，表示富士施乐 EDMICS RLC 格式 */
    VND_FUJIXEROX_EDMICS_RLC("image/vnd.fujixerox.edmics-rlc", ".rlc"),
    /** Vnd globalgraphics pgb 图像媒体类型 */
    VND_GLOBALGRAPHICS_PGB("image/vnd.globalgraphics.pgb", ".pgb"),
    /** Vnd microsoft icon 图标图像媒体类型，用于标识 Microsoft 图标格式 */
    VND_MICROSOFT_ICON("image/vnd.microsoft.icon", ".ico"),
    /** VND_MIX 表示 image/vnd.mix 媒体类型，用于标识混合图像格式 */
    VND_MIX("image/vnd.mix", ".mix"),
    /** VND_MS_MODI 图像媒体类型，表示 Microsoft MODI 图像格式 */
    VND_MS_MODI("image/vnd.ms-modi", ".mdi"),
    /** VND_NET_FPX 图像媒体类型，表示网络 FPX 图像格式 */
    VND_NET_FPX("image/vnd.net-fpx", ".fpx"),
    /** VND_SEALED_PNG 是一种表示密封 PNG 图像的媒体类型常量 */
    VND_SEALED_PNG("image/vnd.sealed.png", ".spng"),
    /** 图像媒体类型，表示密封媒体软密封 GIF 格式 */
    VND_SEALEDMEDIA_SOFTSEAL_GIF("image/vnd.sealedmedia.softseal.gif", ".sgif"),
    /** image/vnd.sealedmedia.softseal.jpg 媒体类型，表示已密封的媒体软密封JPEG图像 */
    VND_SEALEDMEDIA_SOFTSEAL_JPG("image/vnd.sealedmedia.softseal.jpg", ".sjpg"),
    /** VND_SVF 图片媒体类型，表示 SVF 格式的图像文件 */
    VND_SVF("image/vnd.svf", ".svf"),
    /** WAP WBMP 图像媒体类型 */
    VND_WAP_WBMP("image/vnd.wap.wbmp", ".wbmp"),
    /** Vnd xiff 图像媒体类型，用于标识 XIFF 格式的图像内容 */
    VND_XIFF("image/vnd.xiff", ".xif");


    /** 内容类型，用于标识数据的格式，如 application/json 或 text/xml */
    private final String contentType;
    /** 文件扩展名 */
    private final String extension;

    /**
     * 根据内容类型和文件扩展名创建图像媒体类型对象
     * <p>
     * 使用指定的内容类型和文件扩展名初始化图像媒体类型实例
     *
     * @param contentType 内容类型
     * @param extension   文件扩展名（包含点号，如 ".png"）
     * @since 2.0.0
     */
    ImageMediaType(@NonNls String contentType, @NonNls String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    /**
     * 获取不包含点号的文件扩展名
     * <p>
     * 例如：PNG 枚举的 extension 为 ".png"，此方法返回 "png"
     *
     * @return 不包含点号的文件扩展名（如 "png", "jpg"）
     * @since 2.0.0
     */
    public String getExtensionWithoutDot() {
        return this.extension.startsWith(".") ? this.extension.substring(1) : this.extension;
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

    // ==================== 工具方法 ====================

    /**
     * 1. 通过 Content-Type 获取枚举
     * <p>
     * 支持完整的 Content-Type（如 "image/jpeg; charset=utf-8"）和简单的 MIME 类型（如 "image/jpeg"）。
     * 也支持常见的别名，如 "image/jpg" 会被识别为 JPEG。
     *
     * @param contentType Content-Type 字符串
     * @return 对应的枚举值，如果无法识别则返回 null
     * @since 2.0.0
     */
    @Nullable
    public static ImageMediaType fromContentType(@Nullable String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return null;
        }

        // 处理可能包含 charset 等信息的 Content-Type
        String mimeType = contentType.split(";")[0].trim().toLowerCase();

        // 处理常见别名
        mimeType = switch (mimeType) {
            case "image/jpg" -> "image/jpeg";
            case "image/svg" -> "image/svg+xml";
            default -> mimeType;
        };

        // 查找匹配的枚举
        String finalMimeType = mimeType;
        return Arrays.stream(values())
            .filter(type -> type.contentType.equalsIgnoreCase(finalMimeType))
            .findFirst()
            .orElse(null);
    }

    /**
     * 2. 通过文件后缀获取枚举
     * <p>
     * 支持传入包含或不包含点号的后缀（例如 ".png" 或 "png"）。
     * 支持常见的别名扩展名，如 ".jpeg", ".jfif", ".pjpeg" 都会被识别为 JPEG。
     *
     * @param extension 文件扩展名（可以包含点号）
     * @return 对应的枚举值，如果无法识别则返回 null
     * @since 2.0.0
     */
    @Nullable
    public static ImageMediaType fromExtension(@Nullable String extension) {
        if (extension == null || extension.isEmpty()) {
            return null;
        }

        // 标准化扩展名：转小写，确保有点号
        String ext = extension.trim().toLowerCase();
        if (!ext.startsWith(".")) {
            ext = "." + ext;
        }

        // 处理常见别名
        ext = switch (ext) {
            case ".jpeg", ".jfif", ".pjpeg", ".jpe" -> ".jpg";
            case ".tif" -> ".tiff";
            default -> ext;
        };

        // 直接匹配
        final String finalExt = ext;

        return Arrays.stream(values())
            .filter(t -> t.extension.equalsIgnoreCase(finalExt))
            .findFirst()
            .orElse(null);

        // 特殊处理：某些格式可能有多个扩展名
    }

    /**
     * 3. 通过 Content-Type 获取文件扩展名
     * <p>
     * 根据 Content-Type 返回对应的标准文件扩展名（包含点号）。
     *
     * @param contentType Content-Type 字符串
     * @return 文件扩展名（包含点号，如 ".jpg"），如果无法识别则返回 null
     * @since 2.0.0
     */
    @Nullable
    public static String getExtensionByContentType(@Nullable String contentType) {
        ImageMediaType type = fromContentType(contentType);
        return type != null ? type.extension : null;
    }

    /**
     * 4. 通过文件后缀获取 Content-Type
     * <p>
     * 根据文件扩展名返回对应的 Content-Type（MIME 类型）。
     *
     * @param extension 文件扩展名（可以包含点号）
     * @return Content-Type 字符串，如果无法识别则返回 null
     * @since 2.0.0
     */
    @Nullable
    public static String getContentTypeByExtension(@Nullable String extension) {
        ImageMediaType type = fromExtension(extension);
        return type != null ? type.contentType : null;
    }

    /**
     * 5. 通过 Content-Type 获取不包含点号的文件扩展名
     * <p>
     * 根据 Content-Type 返回对应的文件扩展名（不包含点号）。
     *
     * @param contentType Content-Type 字符串
     * @return 文件扩展名（不包含点号，如 "jpg"），如果无法识别则返回 null
     * @since 2.0.0
     */
    @Nullable
    public static String getExtensionWithoutDotByContentType(@Nullable String contentType) {
        ImageMediaType type = fromContentType(contentType);
        return type != null ? type.getExtensionWithoutDot() : null;
    }

    /**
     * 6. 通过文件后缀获取标准的不包含点号的扩展名
     * <p>
     * 根据文件扩展名返回标准化的扩展名（不包含点号）。
     * 例如：输入 ".jpeg" 返回 "jpg"，输入 "png" 返回 "png"
     *
     * @param extension 文件扩展名（可以包含点号）
     * @return 标准扩展名（不包含点号，如 "jpg"），如果无法识别则返回 null
     * @since 2.0.0
     */
    @Nullable
    public static String getStandardExtensionWithoutDot(@Nullable String extension) {
        ImageMediaType type = fromExtension(extension);
        return type != null ? type.getExtensionWithoutDot() : null;
    }

    // ==================== 兼容旧方法 ====================

    /**
     * 根据文件后缀名获取 Content-Type（兼容旧代码）
     * <p>
     * 支持传入包含或不包含点号的后缀（例如 ".png" 或 "png"）。
     * 建议使用 {@link #getContentTypeByExtension(String)} 和空值合并运算符
     *
     * @param extension          文件后缀
     * @param defaultContentType 默认 Content-Type，当无法识别时返回
     * @return 对应的 Content-Type，无法识别时返回 defaultContentType
     */
    public static String fromExtension(String extension, String defaultContentType) {
        String contentType = getContentTypeByExtension(extension);
        return StringUtils.isBlank(contentType)
               ? (StringUtils.isBlank(defaultContentType) ? "" : defaultContentType)
               : contentType;
    }

    /**
     * 根据文件名获取 Content-Type（兼容旧代码）
     * 建议使用 {@link #fromFileName(String, String)} 或 {@link #getContentTypeByExtension(String)}
     *
     * @param filename 文件名
     * @return 对应的 Content-Type，无法识别时返回空字符串
     */
    public static String fromFileName(String filename) {
        return fromFileName(filename, "");
    }

    /**
     * 根据文件名获取 Content-Type（兼容旧代码）
     * 建议使用 {@link #getContentTypeByExtension(String)} 和空值合并运算符
     *
     * @param filename           文件名
     * @param defaultContentType 默认 Content-Type
     * @return 对应的 Content-Type，无法识别时返回 defaultContentType
     */
    public static String fromFileName(String filename, String defaultContentType) {
        if (filename == null || filename.isEmpty()) {
            return StringUtils.isBlank(defaultContentType) ? "" : defaultContentType;
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return StringUtils.isBlank(defaultContentType) ? "" : defaultContentType;
        }
        String ext = filename.substring(dot);
        return fromExtension(ext, defaultContentType);
    }
}