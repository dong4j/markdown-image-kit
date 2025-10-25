package info.dong4j.idea.plugin.weibo.exception;

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
public class Wbp4jException extends RuntimeException {

    private static final long serialVersionUID = 7848372826329873833L;

    /**
     * Instantiates a new Wbp 4 j exception.
     *
     * @since 0.0.1
     */
    Wbp4jException() {
    }

    /**
     * Instantiates a new Wbp 4 j exception.
     *
     * @param message the message
     * @since 0.0.1
     */
    Wbp4jException(String message) {
        super(message);
    }
}
