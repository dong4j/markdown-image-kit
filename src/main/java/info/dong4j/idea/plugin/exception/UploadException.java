/*
 * MIT License
 *
 * Copyright (c) 2019 dong4j <dong4j@gmail.com>
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

package info.dong4j.idea.plugin.exception;

import com.intellij.openapi.project.Project;

import java.util.function.Supplier;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-13 10:54
 */
public class UploadException extends RuntimeException implements Supplier<UploadException> {
    private static final long serialVersionUID = 4076461843028836262L;
    private Project project;

    /**
     * Instantiates a new Img exception.
     */
    public UploadException() {
        super();
    }

    public UploadException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Img exception.
     */
    public UploadException(Project project) {
        super();
        this.project = project;
    }

    /**
     * Instantiates a new Img exception.
     *
     * @param message the message
     */
    public UploadException(Project project, String message) {
        super(message);
        this.project = project;
    }

    /**
     * Instantiates a new Img exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public UploadException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Img exception.
     *
     * @param cause the cause
     */
    public UploadException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Img exception.
     *
     * @param message            the message
     * @param cause              the cause
     * @param enableSuppression  the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    protected UploadException(String message,
                              Throwable cause,
                              boolean enableSuppression,
                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public UploadException get() {
        return this;
    }
}
