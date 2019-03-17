package info.dong4j.idea.plugin.weibo.entity;

import lombok.Data;

/**
 * <p>Description: </p>
 *
 * @author echisan
 * @update dong4j
 * @date 2018-06-14 22:31
 */
@Data
public class PreLogin {
    private Integer retcode;
    private Long servertime;
    private String pcid;
    private String nonce;
    private String pubkey;
    private String rsakv;
    private Integer is_openlock;
    private Integer showpin;
    private Integer exectime;
}
