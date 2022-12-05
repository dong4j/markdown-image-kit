package info.dong4j.idea.plugin.settings.oss;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.16 20:49
 * @since 1.4.0
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GiteeOssState extends AbstractOpenOssState {

    /** Branch */
    protected String branch = "master";
}
