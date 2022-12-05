package info.dong4j.idea.plugin.enums;

import org.jetbrains.annotations.NonNls;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 20:36
 * @since 1.1.0
 */
public enum ImageMediaType {
    /** Cgm image media type */
    CGM("image/cgm"),
    /** Example image media type */
    EXAMPLE("image/example"),
    /** Fits image media type */
    FITS("image/fits"),
    /** G 3 fax image media type */
    G3FAX("image/g3fax"),
    /** Gif image media type */
    GIF("image/gif"),
    /** Ief image media type */
    IEF("image/ief"),
    /** Jp 2 image media type */
    JP2("image/jp2"),
    /** Jpeg image media type */
    JPEG("image/jpeg"),
    /** Jpm image media type */
    JPM("image/jpm"),
    /** Jpx image media type */
    JPX("image/jpx"),
    /** Naplps image media type */
    NAPLPS("image/naplps"),
    /** Png image media type */
    PNG("image/png"),
    /** Prs btif image media type */
    PRS_BTIF("image/prs.btif"),
    /** Prs pti image media type */
    PRS_PTI("image/prs.pti"),
    /** T 38 image media type */
    T38("image/t38"),
    /** Tiff image media type */
    TIFF("image/tiff"),
    /** Tiff fx image media type */
    TIFF_FX("image/tiff-fx"),
    /** Vnd adobe photoshop image media type */
    VND_ADOBE_PHOTOSHOP("image/vnd.adobe.photoshop"),
    /** Vnd cns inf 2 image media type */
    VND_CNS_INF2("image/vnd.cns.inf2"),
    /** Vnd djvu image media type */
    VND_DJVU("image/vnd.djvu"),
    /** Vnd dwg image media type */
    VND_DWG("image/vnd.dwg"),
    /** Vnd dxf image media type */
    VND_DXF("image/vnd.dxf"),
    /** Vnd fastbidsheet image media type */
    VND_FASTBIDSHEET("image/vnd.fastbidsheet"),
    /** Vnd fpx image media type */
    VND_FPX("image/vnd.fpx"),
    /** Vnd fst image media type */
    VND_FST("image/vnd.fst"),
    /** Vnd fujixerox edmics mmr image media type */
    VND_FUJIXEROX_EDMICS_MMR("image/vnd.fujixerox.edmics-mmr"),
    /** Vnd fujixerox edmics rlc image media type */
    VND_FUJIXEROX_EDMICS_RLC("image/vnd.fujixerox.edmics-rlc"),
    /** Vnd globalgraphics pgb image media type */
    VND_GLOBALGRAPHICS_PGB("image/vnd.globalgraphics.pgb"),
    /** Vnd microsoft icon image media type */
    VND_MICROSOFT_ICON("image/vnd.microsoft.icon"),
    /** Vnd mix image media type */
    VND_MIX("image/vnd.mix"),
    /** Vnd ms modi image media type */
    VND_MS_MODI("image/vnd.ms-modi"),
    /** Vnd net fpx image media type */
    VND_NET_FPX("image/vnd.net-fpx"),
    /** Vnd sealed png image media type */
    VND_SEALED_PNG("image/vnd.sealed.png"),
    /** Vnd sealedmedia softseal gif image media type */
    VND_SEALEDMEDIA_SOFTSEAL_GIF("image/vnd.sealedmedia.softseal.gif"),
    /** Vnd sealedmedia softseal jpg image media type */
    VND_SEALEDMEDIA_SOFTSEAL_JPG("image/vnd.sealedmedia.softseal.jpg"),
    /** Vnd svf image media type */
    VND_SVF("image/vnd.svf"),
    /** Vnd wap wbmp image media type */
    VND_WAP_WBMP("image/vnd.wap.wbmp"),
    /** Vnd xiff image media type */
    VND_XIFF("image/vnd.xiff");

    /** Content type */
    private final String contentType;

    /**
     * Image media type
     *
     * @param contentType content type
     * @since 1.1.0
     */
    ImageMediaType(@NonNls String contentType) {
        this.contentType = contentType;
    }

    /**
     * To string
     *
     * @return the string
     * @since 1.1.0
     */
    @Override
    public String toString() {
        return this.contentType;
    }
}