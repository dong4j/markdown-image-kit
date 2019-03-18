package info.dong4j.idea.plugin.singleton;

import org.junit.Test;

import java.io.*;

/**
 * <p>Company: 科大讯飞股份有限公司-四川分公司</p>
 * <p>Description: ${description}</p>
 *
 * @author dong4j
 * @date 2019-03-18 16:13
 * @email sjdong3@iflytek.com
 */
public class AliyunOssClientTest {
    @Test
    public void test(){
        AliyunOssClient aliyunOssClient = AliyunOssClient.getInstance();
        aliyunOssClient.upload(new File("/Users/dong4j/Downloads/我可要开始皮了.png"));
    }

}