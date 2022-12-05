package info.dong4j.idea.plugin.exception;

import com.intellij.openapi.project.Project;

import java.io.Serial;
import java.util.function.Supplier;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
public class UploadException extends RuntimeException implements Supplier<UploadException> {
    /** serialVersionUID */
    @Serial
    private static final long serialVersionUID = 4076461843028836262L;
    /** Project */
    private Project project;

    /**
     * Instantiates a new Img exception.
     *
     * @since 0.0.1
     */
    public UploadException() {
        super();
    }

    /**
     * Upload exception
     *
     * @param message message
     * @since 0.0.1
     */
    public UploadException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Img exception.
     *
     * @param project project
     * @since 0.0.1
     */
    public UploadException(Project project) {
        super();
        this.project = project;
    }

    /**
     * Instantiates a new Img exception.
     *
     * @param project project
     * @param message the message
     * @since 0.0.1
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
     * @since 0.0.1
     */
    public UploadException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Img exception.
     *
     * @param cause the cause
     * @since 0.0.1
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
     * @since 0.0.1
     */
    protected UploadException(String message,
                              Throwable cause,
                              boolean enableSuppression,
                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Get
     *
     * @return the upload exception
     * @since 0.0.1
     */
    @Override
    public UploadException get() {
        return this;
    }
}
