package info.dong4j.idea.plugin.util;

import org.jetbrains.annotations.Contract;
import org.junit.Test;

import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-13 16:02
 * @email sjdong3@iflytek.com
 */
@Slf4j
public class EnumsUtilsTest {
    @Test
    public void testEnumsUtils() {

        Optional<SuffixSelectType> m = EnumsUtils.getEnumObject(SuffixSelectType.class, e -> e.getName().equals("文件名"));

        log.info(Objects.requireNonNull(m).isPresent() ? m.get().getName() : null);

        Optional<SuffixSelectType> m1 = EnumsUtils.getEnumObject(SuffixSelectType.class, e -> e.getIndex() == 1);

        log.info(Objects.requireNonNull(m1).isPresent() ? m1.get().getIndex() + "" : null);

    }

    enum SuffixSelectType {
        FILE_NAME(1, "文件名"),
        DATE_FILE_NAME(2, "日期-文件名"),
        RANDOM(3, "随机");

        private int index;
        private String name;

        SuffixSelectType(int index, String name) {
            this.index = index;
            this.name = name;
        }

        @Contract(pure = true)
        public int getIndex() {
            return index;
        }

        @Contract(pure = true)
        public String getName() {
            return name;
        }
    }

    @Test
    public void test1(){
        String[] allCloud = new String[] {"网易云", "百度云", "京东云", "又拍云", "sm.ms", "Imgur", "Ucloud", "QingCloud"};
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : allCloud) {
            stringBuilder.append("next").append(" ");
            System.out.println(String.format("「%s」 \tsee you %sversion.", s, stringBuilder));
        }
    }
}