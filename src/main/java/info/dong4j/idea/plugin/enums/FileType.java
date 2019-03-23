/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j
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

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @date 2019-03-23 01:14
 * @email sjdong3@iflytek.com
 */
public enum FileType {

    /**
     * JPEG
     */
    JPEG("FFD8FF", "jpg"),

    /**
     * PNG
     */
    PNG("89504E47", "png"),

    /**
     * GIF
     */
    GIF("47494638", "gif"),

    /**
     * TIFF
     */
    TIFF("49492A00"),

    /**
     * Windows bitmap
     */
    BMP("424D"),

    /**
     * CAD
     */
    DWG("41433130"),

    /**
     * Adobe photoshop
     */
    PSD("38425053"),

    /**
     * Rich Text Format
     */
    RTF("7B5C727466"),

    /**
     * XML
     */
    XML("3C3F786D6C"),

    /**
     * HTML
     */
    HTML("68746D6C3E"),

    /**
     * Outlook Express
     */
    DBX("CFAD12FEC5FD746F "),

    /**
     * Outlook
     */
    PST("2142444E"),

    /**
     * doc;xls;dot;ppt;xla;ppa;pps;pot;msi;sdw;db
     */
    OLE2("0xD0CF11E0A1B11AE1"),

    /**
     * Microsoft Word/Excel
     */
    XLS_DOC("D0CF11E0"),

    /**
     * Microsoft Access
     */
    MDB("5374616E64617264204A"),

    /**
     * Word Perfect
     */
    WPB("FF575043"),

    /**
     * Postscript
     */
    EPS_PS("252150532D41646F6265"),

    /**
     * Adobe Acrobat
     */
    PDF("255044462D312E"),

    /**
     * Windows Password
     */
    PWL("E3828596"),

    /**
     * ZIP Archive
     */
    ZIP("504B0304"),

    /**
     * ARAR Archive
     */
    RAR("52617221"),

    /**
     * WAVE
     */
    WAV("57415645"),

    /**
     * AVI
     */
    AVI("41564920"),

    /**
     * Real Audio
     */
    RAM("2E7261FD"),

    /**
     * Real Media
     */
    RM("2E524D46"),

    /**
     * Quicktime
     */
    MOV("6D6F6F76"),

    /**
     * Windows Media
     */
    ASF("3026B2758E66CF11"),

    /**
     * MIDI
     */
    MID("4D546864");

    private String value = "";
    private String ext = "";

    FileType(String value, String ext) {
        this(value);
        this.ext = ext;
    }

    FileType(String value) {
        this.value = value;
    }

    public String getExt() {
        return ext;
    }

    public String getValue() {
        return value;
    }

}