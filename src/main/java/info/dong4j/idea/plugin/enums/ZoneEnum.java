/*
 * MIT License
 *
 * Copyright (c) 2022 dong4j <dong4j@gmail.com>
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

import org.jetbrains.annotations.Contract;

/**
 * <p>Description: 七牛云存储位置 </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.19 20:26
 * @since 0.0.1
 */
public enum ZoneEnum {
    /** East china zone enum */
    // 区域名称：z0 华东  z1 华北  z2 华南  na0 北美  as0 东南亚
    EAST_CHINA(0, "华东", "upload.qiniup.com"),
    /** Nort china zone enum */
    NORT_CHINA(1, "华北", "upload-z1.qiniup.com"),
    /** South china zone enum */
    SOUTH_CHINA(2, "华南", "upload-na0.qiniup.com"),
    /** North ameria zone enum */
    NORTH_AMERIA(3, "北美", "upload-as0.qiniup.com"),
    /** Southeast asia zone enum */
    SOUTHEAST_ASIA(4, "东南亚", "upload-as0.qiniup.com");

    /** Index */
    public int index;
    /** Name */
    public String name;
    /** Zone */
    public String host;

    /**
     * Zone enum
     *
     * @param index index
     * @param name  name
     * @param host  host
     * @since 0.0.1
     */
    ZoneEnum(int index, String name, String host) {
        this.index = index;
        this.name = name;
        this.host = host;
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
     * Gets name *
     *
     * @return the name
     * @since 0.0.1
     */
    @Contract(pure = true)
    public String getName() {
        return this.name;
    }

    /**
     * Gets zone *
     *
     * @return the zone
     * @since 0.0.1
     */
    @Contract(pure = true)
    public String getHost() {
        return this.host;
    }
}
