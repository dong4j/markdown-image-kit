package info.dong4j.idea.plugin.util;

import org.jetbrains.annotations.Contract;
import org.junit.Test;

import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @version 1.0.0
 * @email "mailto:dong4j@gmail.com"
 * @date 2019.03.13 16:02
 * @since 1.1.0
 */
@Slf4j
public class EnumsUtilsTest {
    /**
     * Test enums utils
     *
     * @since 1.1.0
     */
    @Test
    public void testEnumsUtils() {

        Optional<SuffixSelectType> m = EnumsUtils.getEnumObject(SuffixSelectType.class, e -> e.getName().equals("文件名"));

        log.info(Objects.requireNonNull(m).isPresent() ? m.get().getName() : null);

        Optional<SuffixSelectType> m1 = EnumsUtils.getEnumObject(SuffixSelectType.class, e -> e.getIndex() == 1);

        log.info(Objects.requireNonNull(m1).isPresent() ? m1.get().getIndex() + "" : null);

    }

    /**
     * <p>Description: </p>
     *
     * @author dong4j
     * @version 1.0.0
     * @email "mailto:dong4j@gmail.com"
     * @date 2021.02.14 22:44
     * @since 1.1.0
     */
    enum SuffixSelectType {
        /** File name suffix select type */
        FILE_NAME(1, "文件名"),
        /** Date file name suffix select type */
        DATE_FILE_NAME(2, "日期-文件名"),
        /** Random suffix select type */
        RANDOM(3, "随机");

        /** Index */
        private final int index;
        /** Name */
        private final String name;

        /**
         * Suffix select type
         *
         * @param index index
         * @param name  name
         * @since 1.1.0
         */
        SuffixSelectType(int index, String name) {
            this.index = index;
            this.name = name;
        }

        /**
         * Gets index *
         *
         * @return the index
         * @since 1.1.0
         */
        @Contract(pure = true)
        public int getIndex() {
            return this.index;
        }

        /**
         * Gets name *
         *
         * @return the name
         * @since 1.1.0
         */
        @Contract(pure = true)
        public String getName() {
            return this.name;
        }
    }

    /**
     * Test 1
     *
     * @since 1.1.0
     */
    @Test
    public void test1() {
        String[] allCloud = new String[] {"网易云", "百度云", "京东云", "又拍云", "sm.ms", "Imgur", "Ucloud", "QingCloud"};
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : allCloud) {
            stringBuilder.append("next").append(" ");
            System.out.println(String.format("「%s」 \tsee you %sversion.", s, stringBuilder));
        }
    }
}