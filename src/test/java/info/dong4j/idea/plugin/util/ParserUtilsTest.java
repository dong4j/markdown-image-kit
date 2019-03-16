package info.dong4j.idea.plugin.util;

import info.dong4j.idea.plugin.content.ImageContents;

import org.junit.Test;

import java.io.*;
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
    public void test() {
        Map<String, String> map = ParserUtils.parseImageTag("![xxx](yyy)");
        for (Map.Entry<String, String> result : map.entrySet()) {
            log.info("key = {}, value = {}", result.getKey(), result.getValue());
        }

        String filePath = "./imgs/1eefcf26.png";
        String fileName = filePath.substring(filePath.lastIndexOf(File.pathSeparator) + 1);
        log.info(fileName);
    }

    @Test
    public void test1() {
        String str = "<a data-fancybox title='' href='https://dong4j-imgs.oss-cn-hangzhou.aliyuncs.com/blog/JavaKnowledgePoint.png' >![](https://dong4j-imgs.oss-cn-hangzhou.aliyuncs.com/blog/JavaKnowledgePoint.png)</a>";

        log.info("title = {}, path = {}", str.substring(str.indexOf(ImageContents.IMAGE_MARK_PREFIX) + ImageContents.IMAGE_MARK_PREFIX.length(),
                                                        str.indexOf(ImageContents.IMAGE_MARK_MIDDLE)),
                 str.substring(str.indexOf(ImageContents.IMAGE_MARK_MIDDLE) + ImageContents.IMAGE_MARK_MIDDLE.length(),
                               str.indexOf(ImageContents.IMAGE_MARK_SUFFIX)));
    }

    @Test
    public void test2(){
        String path = "./xxx/aa.png";
        String imageName = path.substring(path.lastIndexOf(File.separator) + 1);
        log.info("imagename = {}", imageName);
    }
}