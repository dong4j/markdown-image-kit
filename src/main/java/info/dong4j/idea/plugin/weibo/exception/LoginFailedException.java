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
public class LoginFailedException extends RuntimeException {

    private static final long serialVersionUID = -1080553329276240278L;

    /**
     * Instantiates a new Login failed exception.
     *
     * @since 0.0.1
     */
    public LoginFailedException() {
    }

    /**
     * Instantiates a new Login failed exception.
     *
     * @param message the message
     * @since 0.0.1
     */
    public LoginFailedException(String message) {
        super(message);
    }
}
