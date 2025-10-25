package info.dong4j.idea.plugin.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>Description: </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @email "mailto:dong4j@gmail.com"
 * @date 2021.02.14 18:40
 * @since 0.0.1
 */
@Data
public class HelpResult implements Serializable {
    /** serialVersionUID */
    private static final long serialVersionUID = 2341371341825471102L;
    /** Code */
    private String code;
    /** Url */
    private String url;
}
