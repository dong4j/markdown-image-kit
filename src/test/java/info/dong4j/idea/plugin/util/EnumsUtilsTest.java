package info.dong4j.idea.plugin.util;

import org.jetbrains.annotations.Contract;
import org.junit.Test;

import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * 枚举工具类测试类
 * <p>
 * 用于测试枚举相关工具类的功能，包括枚举对象的获取、枚举值的定义和使用等。
 * 包含多个测试方法，用于验证枚举类型 {@link SuffixSelectType} 的行为和功能。
 *
 * @author dong4j
 * @version 1.0.0
 * @date 2025.10.24
 * @since 1.1.0
 */
@Slf4j
public class EnumsUtilsTest {
    /**
     * 测试枚举工具类的功能
     * <p>
     * 测试场景：验证通过名称和索引获取枚举对象的正确性
     * 预期结果：应成功获取对应的枚举值并输出其名称和索引
     * <p>
     * 该测试通过 {@link EnumsUtils#getEnumObject(Class, java.util.function.Predicate)} 方法实现枚举对象的查找
     */
    @Test
    public void testEnumsUtils() {

        Optional<SuffixSelectType> m = EnumsUtils.getEnumObject(SuffixSelectType.class, e -> e.getName().equals("文件名"));

        log.info(Objects.requireNonNull(m).isPresent() ? m.get().getName() : null);

        Optional<SuffixSelectType> m1 = EnumsUtils.getEnumObject(SuffixSelectType.class, e -> e.getIndex() == 1);

        log.info(Objects.requireNonNull(m1).isPresent() ? m1.get().getIndex() + "" : null);

    }

    /**
     * 后缀选择类型枚举
     * <p>
     * 定义了文件名后缀选择的三种类型，用于标识不同的后缀生成策略。
     * 包括基于文件名的后缀、基于日期-文件名的后缀以及随机后缀。
     * </p>
     *
     * @author dong4j
     * @version 1.0.0
     * @date 2021.02.14
     * @since 1.1.0
     */
    enum SuffixSelectType {
        /** 文件名后缀选择类型 */
        FILE_NAME(1, "文件名"),
        /** 日期-文件名类型，用于指定文件名后缀选择类型 */
        DATE_FILE_NAME(2, "日期-文件名"),
        /** 随机后缀选择类型 */
        RANDOM(3, "随机");
        /** 索引位置 */
        private final int index;
        /** 名称 */
        private final String name;

        /**
         * 构造一个后缀选择类型对象
         * <p>
         * 根据给定的索引和名称初始化后缀选择类型
         *
         * @param index 索引值
         * @param name  名称
         * @since 1.1.0
         */
        SuffixSelectType(int index, String name) {
            this.index = index;
            this.name = name;
        }

        /**
         * 获取当前对象的索引值
         * <p>
         * 返回该对象内部维护的索引属性值。
         *
         * @return 当前对象的索引值
         * @since 1.1.0
         */
        @Contract(pure = true)
        public int getIndex() {
            return this.index;
        }

        /**
         * 获取名称
         * <p>
         * 返回当前对象的名称属性值
         *
         * @return 名称
         * @since 1.1.0
         */
        @Contract(pure = true)
        public String getName() {
            return this.name;
        }
    }

    /**
     * 测试字符串拼接与格式化输出功能
     * <p>
     * 测试场景：遍历预定义的云服务名称数组，进行字符串拼接和格式化输出
     * 预期结果：控制台输出应包含每个云服务名称及其对应的拼接字符串
     * <p>
     * 注意：该测试方法主要用于验证字符串拼接逻辑和格式化输出的正确性
     */
    @Test
    public void test1() {
        String[] allCloud = new String[] {"网易云", "百度云", "京东云", "又拍云", "sm.ms", "Imgur", "Ucloud", "QingCloud"};
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : allCloud) {
            stringBuilder.append("next").append(" ");
            System.out.printf("「%s」 \tsee you %sversion.%n", s, stringBuilder);
        }
    }
}