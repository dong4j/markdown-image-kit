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
public class WbpUploadResponse implements UploadResponse {
    /** Result status */
    private ResultStatus resultStatus;
    /** Message */
    private String message;
    /** Image info */
    private ImageInfo imageInfo;

    /**
     * Gets result *
     *
     * @return the result
     * @since 0.0.1
     */
    @Override
    public ResultStatus getResult() {
        return this.resultStatus;
    }

    /**
     * Sets result *
     *
     * @param rs rs
     * @since 0.0.1
     */
    @Override
    public void setResult(ResultStatus rs) {
        this.resultStatus = rs;
    }

    /**
     * Gets message *
     *
     * @return the message
     * @since 0.0.1
     */
    @Override
    public String getMessage() {
        return this.message;
    }

    /**
     * Sets message *
     *
     * @param message message
     * @since 0.0.1
     */
    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets image info *
     *
     * @return the image info
     * @since 0.0.1
     */
    @Override
    public ImageInfo getImageInfo() {
        return this.imageInfo;
    }

    /**
     * Sets image info *
     *
     * @param imageInfo image info
     * @since 0.0.1
     */
    @Override
    public void setImageInfo(ImageInfo imageInfo) {
        this.imageInfo = imageInfo;
    }
}
