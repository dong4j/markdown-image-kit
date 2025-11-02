package info.dong4j.idea.plugin.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import lombok.extern.slf4j.Slf4j;

/**
 * 枚举工具类
 * <p>
 * 提供对枚举类的通用操作，如根据筛选条件获取枚举对象。
 * 该类使用缓存机制存储已加载的枚举数组，以提高性能。
 * </p>
 *
 * @author dong4j
 * @version 0.0.1
 * @date 2025.10.24
 * @since 0.0.1
 */
@Slf4j
@SuppressWarnings("unchecked")
public final class EnumsUtils {
    /** map 缓存类与对应实例的映射关系，用于快速查找 */
    private static final Map<Class, Object> map = new ConcurrentHashMap<>();

    /**
     * 根据条件获取枚举对象
     * <p>
     * 该方法根据指定的枚举类和筛选条件，查找符合条件的枚举对象。如果类不是枚举类或未找到符合条件的对象，则返回空Optional。
     *
     * @param className 枚举类的Class对象
     * @param predicate 用于筛选枚举对象的条件谓词
     * @return 符合条件的枚举对象，若未找到则返回空Optional
     * @since 0.0.1
     */
    public static <T> Optional<T> getEnumObject(Class<T> className, Predicate<T> predicate) {
        if (!className.isEnum()) {
            log.trace("Class 不是枚举类");
            return Optional.empty();
        }
        Object obj = map.get(className);
        T[] ts;
        if (obj == null) {
            ts = className.getEnumConstants();
            map.put(className, ts);
        } else {
            ts = (T[]) obj;
        }
        return Arrays.stream(ts).filter(predicate).findAny();
    }
}
