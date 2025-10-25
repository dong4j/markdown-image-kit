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
 * @date 2021.02.16 13:16
 * @since 1.1.0
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractExtendOssState extends OssState {

    /** Endpoint */
    private String endpoint = "";
    /** Access key */
    private String accessKey = "";
    /** Access secret key */
    private String accessSecretKey = "";
    /** Bucket name */
    private String bucketName = "";
    /** Filedir */
    private String filedir = "";
    /** Custom endpoint */
    private String customEndpoint = "";
    /** Is custom endpoint */
    private Boolean isCustomEndpoint = false;
}
