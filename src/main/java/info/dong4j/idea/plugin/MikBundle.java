package info.dong4j.idea.plugin;

import com.intellij.CommonBundle;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.21 23:49
 * @since 0.0.1
 */
public class MikBundle {

    /** BUNDLE */
    @NonNls
    private static final String BUNDLE = "messages.MikBundle";
    /** Our bundle */
    private static Reference<ResourceBundle> ourBundle;

    /**
     * Mik bundle
     *
     * @since 0.0.1
     */
    private MikBundle() {
    }

    /**
     * Visibility presentation
     *
     * @param modifier modifier
     * @return the string
     * @since 0.0.1
     */
    @NotNull
    public static String visibilityPresentation(@NotNull String modifier) {
        return message(modifier + ".visibility.presentation");
    }

    /**
     * Message
     *
     * @param key    key
     * @param params params
     * @return the string
     * @since 0.0.1
     */
    @NotNull
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
        return CommonBundle.message(getBundle(), key, params);
    }

    /**
     * Gets bundle *
     *
     * @return the bundle
     * @since 0.0.1
     */
    private static ResourceBundle getBundle() {
        ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(ourBundle);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE);
            ourBundle = new SoftReference<>(bundle);
        }
        return bundle;
    }
}
