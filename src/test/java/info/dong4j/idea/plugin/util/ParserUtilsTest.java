package info.dong4j.idea.plugin.util;

import org.junit.Test;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-12 22:40
 * @email sjdong3@iflytek.com
 */
@Slf4j
public class ParserUtilsTest {
    @Test
    public void test(){
        Map<String, String> map = ParserUtils.parseImageTag("![xxx](yyy)");
        for(Map.Entry<String, String> result : map.entrySet()){
            log.info("key = {}, value = {}", result.getKey(), result.getValue());
        }
    }
}