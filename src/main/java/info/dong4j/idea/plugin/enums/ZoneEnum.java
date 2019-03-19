package info.dong4j.idea.plugin.enums;

import com.qiniu.common.Zone;

import org.jetbrains.annotations.Contract;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-19 20:26
 * @email sjdong3@iflytek.com
 */
public enum ZoneEnum {
    // 区域名称：z0 华东  z1 华北  z2 华南  na0 北美  as0 东南亚
    EAST_CHINA(0, "华东", Zone.zone0()),
    NORT_CHINA(1, "华北", Zone.zone1()),
    SOUTH_CHINA(2, "华南", Zone.zone2()),
    NORTH_AMERIA(3, "北美", Zone.zoneNa0()),
    SOUTHEAST_ASIA(4, "东南亚", Zone.zoneAs0());

    public int index;
    public String name;
    public Zone zone;

    ZoneEnum(int index, String name, Zone zone) {
        this.index = index;
        this.name = name;
        this.zone = zone;
    }

    @Contract(pure = true)
    public int getIndex() {
        return index;
    }

    @Contract(pure = true)
    public String getName() {
        return name;
    }

    @Contract(pure = true)
    public Zone getZone() {
        return zone;
    }
}
