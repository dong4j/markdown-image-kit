package info.dong4j.idea.plugin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: </p>
 *
 * @author dong4j
 * @date 2019 -03-13 16:00
 * @email sjdong3 @iflytek.com
 */
public class EnumsUtils {

    private static Logger logger = LoggerFactory.getLogger(EnumsUtils.class);

    private static Map<Class, Object> map = new ConcurrentHashMap<>();

    /**
     * 根据条件获取枚举对象
     *
     * @param <T>       the type parameter
     * @param className 枚举类
     * @param predicate 筛选条件
     * @return enum object
     */
    public static <T> Optional<T> getEnumObject(Class<T> className, Predicate<T> predicate) {
        if (!className.isEnum()) {
            logger.info("Class 不是枚举类");
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