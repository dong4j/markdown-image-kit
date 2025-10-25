package info.dong4j.idea.plugin.weibo.entity;

import lombok.Data;

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
@Data
public class PreLogin {
    /** Retcode */
    private Integer retcode;
    /** Servertime */
    private Long servertime;
    /** Pcid */
    private String pcid;
    /** Nonce */
    private String nonce;
    /** Pubkey */
    private String pubkey;
    /** Rsakv */
    private String rsakv;
    /** Is openlock */
    private Integer is_openlock;
    /** Showpin */
    private Integer showpin;
    /** Exectime */
    private Integer exectime;
}
