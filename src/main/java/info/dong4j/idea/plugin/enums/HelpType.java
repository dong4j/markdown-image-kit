package info.dong4j.idea.plugin.enums;

/**
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.25 14:15
 * @since 0.0.1
 */
public enum HelpType {
    /** Setting help type */
    SETTING("setting"),
    /** Nottify help type */
    NOTTIFY("notify"),
    /** Custom help type */
    CUSTOM("custom");

    /** Where */
    public String where;

    /**
     * Help type
     *
     * @param where where
     * @since 0.0.1
     */
    HelpType(String where) {
        this.where = where;
    }
}
