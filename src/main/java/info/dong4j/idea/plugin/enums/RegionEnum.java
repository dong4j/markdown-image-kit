package info.dong4j.idea.plugin.enums;

import com.qiniu.common.Zone;

import org.jetbrains.annotations.Contract;

/**
 * <p>Company: no company</p>
 * <p>Description: 腾讯云存储位置</p>
 *
 * @author dong4j
 * @email dong4j@gmail.com
 * @since 2019-07-08 16:47
 */
public enum RegionEnum {
    // 区域名称：z0 华东  z1 华北  z2 华南  na0 北美  as0 东南亚
    EAST_CHINA(0, "华东", Zone.zone0()),
    NORT_CHINA(1, "华北", Zone.zone1()),
    SOUTH_CHINA(2, "华南", Zone.zone2()),
    NORTH_AMERIA(3, "北美", Zone.zoneNa0()),
    SOUTHEAST_ASIA(4, "东南亚", Zone.zoneAs0());

    public int index;
    public String name;
    public Zone zone;

    RegionEnum(int index, String name, Zone zone) {
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
