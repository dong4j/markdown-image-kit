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

package info.dong4j.idea.plugin.weibo;

import info.dong4j.idea.plugin.weibo.entity.ImageInfo;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2018.06.14 22:31
 * @update dong4j
 * @since 0.0.1
 */
public interface UploadResponse {

    /**
     * Gets result.
     *
     * @return the result
     * @since 0.0.1
     */
    ResultStatus getResult();

    /**
     * Sets result.
     *
     * @param rs the rs
     * @since 0.0.1
     */
    void setResult(ResultStatus rs);

    /**
     * Gets message.
     *
     * @return the message
     * @since 0.0.1
     */
    String getMessage();

    /**
     * Sets message.
     *
     * @param message the message
     * @since 0.0.1
     */
    void setMessage(String message);

    /**
     * Gets image info.
     *
     * @return the image info
     * @since 0.0.1
     */
    ImageInfo getImageInfo();

    /**
     * Sets image info.
     *
     * @param imageInfo the image info
     * @since 0.0.1
     */
    void setImageInfo(ImageInfo imageInfo);

    /**
     * The enum Result status.
     *
     * @author dong4j
     * @version 0.0.1
     * @email "mailto:dong4j@gmail.com"
     * @date 2021.02.14 18:40
     * @since 0.0.1
     */
    enum ResultStatus {
        /**
         * Success result status.
         */
        SUCCESS,
        /**
         * Failed result status.
         */
        FAILED
    }
}
