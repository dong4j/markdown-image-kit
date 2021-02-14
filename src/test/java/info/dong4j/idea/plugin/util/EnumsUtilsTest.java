/*
 * MIT License
 *
 * Copyright (c) 2021 dong4j <dong4j@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package info.dong4j.idea.plugin.util;

import org.jetbrains.annotations.Contract;
import org.junit.Test;

import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: no company</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019.03.13 16:02
 * @email "mailto:dong4j@gmail.com"
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

        private final int index;
        private final String name;

        SuffixSelectType(int index, String name) {
            this.index = index;
            this.name = name;
        }

        @Contract(pure = true)
        public int getIndex() {
            return this.index;
        }

        @Contract(pure = true)
        public String getName() {
            return this.name;
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