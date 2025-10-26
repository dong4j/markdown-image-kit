package info.dong4j.idea.plugin.enums;

import lombok.Getter;

/**
 * 文件类型枚举
 * <p>
 * 用于表示常见的文件类型及其对应的魔数和扩展名，支持通过魔数或扩展名识别文件类型。
 * 枚举值包含文件的魔数（用于文件识别）和扩展名（用于文件命名）。
 * <p>
 * 支持的文件类型包括：JPEG、PNG、GIF、TIFF、BMP、DWG、PSD、RTF、XML、HTML、DBX、PST、OLE2、XLS_DOC、MDB、WPB、EPS_PS、PDF、PWL、ZIP、RAR、WAV、AVI、RAM、RM、MOV、ASF、MID 等。
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2019.03.23
 * @since 0.0.1
 */
@Getter
public enum FileType {
    /** JPEG 格式标识符 */
    JPEG("FFD8FF", "jpg"),
    /** PNG 格式标识符 */
    PNG("89504E47", "png"),
    /** GIF 图片格式标识符 */
    GIF("47494638", "gif"),
    /** TIFF 格式标识符 */
    TIFF("49492A00"),
    /** Windows bitmap 格式标识符 */
    BMP("424D"),
    /** CAD 文件格式标识符 */
    DWG("41433130"),
    /** PSD 文件类型标识符 */
    PSD("38425053"),
    /** RTF 格式，用于表示富文本格式的文件类型 */
    RTF("7B5C727466"),
    /** XML 标记常量 */
    XML("3C3F786D6C"),
    /** HTML 字符串常量，表示 HTML 标记的十六进制编码 */
    HTML("68746D6C3E"),
    /** Outlook Express 配置信息 */
    DBX("CFAD12FEC5FD746F "),
    /** Outlook 文件格式标识符 */
    PST("2142444E"),
    /** OLE2 文件头标识，用于标识文件类型 */
    OLE2("0xD0CF11E0A1B11AE1"),
    /** Microsoft Word/Excel 文档类型标识符 */
    XLS_DOC("D0CF11E0"),
    /** Microsoft Access 数据库连接配置 */
    MDB("5374616E64617264204A"),
    /** Word Perfect 编码值 */
    WPB("FF575043"),
    /** 用于表示 Postscript 的常量值 */
    EPS_PS("252150532D41646F6265"),
    /** PDF 文件类型标识符 */
    PDF("255044462D312E"),
    /** Windows 密码配置项 */
    PWL("E3828596"),
    /** ZIP 文件格式标识符，用于识别压缩文件类型 */
    ZIP("504B0304"),
    /** ARAR Archive 格式的文件标识符 */
    RAR("52617221"),
    /** WAVE 格式的数据类型，用于表示特定编码格式的音频文件 */
    WAV("57415645"),
    /** AVI 文件格式标识符 */
    AVI("41564920"),
    /** Real Audio 格式标识符 */
    RAM("2E7261FD"),
    /** RM 标识符，用于表示 Real Media 类型 */
    RM("2E524D46"),
    /** MOV 编码，表示 Quicktime 格式 */
    MOV("6D6F6F76"),
    /** Windows Media 格式标识符 */
    ASF("3026B2758E66CF11"),
    /** MIDI 格式标识符 */
    MID("4D546864");

    /**
     * 值字段，用于存储字符串类型的值
     * -- GETTER --
     * 获取当前对象的值
     * <p>
     * 返回该对象内部存储的值属性
     *
     */
    private final String value;

    /**
     * 扩展字段，用于存储额外信息
     * -- GETTER --
     * 获取扩展信息
     * <p>
     * 返回当前对象的扩展字段值
     */
    private String ext = "";

    /**
     * 文件类型枚举类
     * <p>
     * 表示文件的类型，包含文件值和扩展名信息
     *
     * @param value 文件值
     * @param ext   文件扩展名
     * @since 0.0.1
     */
    FileType(String value, String ext) {
        this(value);
        this.ext = ext;
    }

    /**
     * 文件类型枚举构造函数
     * <p>
     * 用于初始化文件类型枚举实例，将传入的字符串值赋给枚举的value字段
     *
     * @param value 文件类型对应的字符串值
     */
    FileType(String value) {
        this.value = value;
    }
}
