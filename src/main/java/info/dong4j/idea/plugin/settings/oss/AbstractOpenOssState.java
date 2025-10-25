package info.dong4j.idea.plugin.settings.oss;

import info.dong4j.idea.plugin.settings.OssState;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>Description:  </p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@fkhwl.com"
 * @date 2021.02.17 17:22
 * @since 1.4.0
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractOpenOssState extends OssState {
    /** Repos */
    private String repos = "";
    /** Branch */
    protected String branch = "";
    /** Token */
    private String token = "";
    /** Filedir */
    private String filedir = "";
    /** Custom endpoint */
    private String customEndpoint = "";
    /** Is custom endpoint */
    private Boolean isCustomEndpoint = false;
}
