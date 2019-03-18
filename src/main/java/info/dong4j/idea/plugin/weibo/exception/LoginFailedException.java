package info.dong4j.idea.plugin.weibo.exception;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018 -06-14 22:31
 */
public class LoginFailedException extends RuntimeException {

    /**
     * Instantiates a new Login failed exception.
     */
    public LoginFailedException() {
    }

    /**
     * Instantiates a new Login failed exception.
     *
     * @param message the message
     */
    public LoginFailedException(String message) {
        super(message);
    }
}
