package info.dong4j.idea.plugin.exception;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @email sjdong3 @iflytek.com
 * @since 2019 -03-13 10:54
 */
public class ImgException extends RuntimeException {
    private static final long serialVersionUID = 4076461843028836262L;

    /**
     * Instantiates a new Img exception.
     */
    public ImgException() {
        super();
    }

    /**
     * Instantiates a new Img exception.
     *
     * @param message the message
     */
    public ImgException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Img exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ImgException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Img exception.
     *
     * @param cause the cause
     */
    public ImgException(Throwable cause) {
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
    protected ImgException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
